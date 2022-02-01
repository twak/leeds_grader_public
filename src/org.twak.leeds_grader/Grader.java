package org.twak.leeds_grader;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import com.sun.javafx.webkit.WebConsoleListener;
import com.thoughtworks.xstream.XStream;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

public class Grader extends JFrame {

	public static Grader grader;
	public static final String GRADE_FILE = "grade.xml";
	Vector<StudentFolder> toGrade;
	Assignment assignment;
	WebView html;
	DefaultComboBoxModel<StudentFolder> studentModel;
	private long lastClick = System.currentTimeMillis();

	JPanel gradeUI = new JPanel(new BorderLayout( 1, 1 ));

	static XStream xStream = new XStream();

	StudentGrade sg;
	int selStudent = 0;
	StudentFolder sff = null;
	File sgf;
	File root;

	Set<StudentFolder> doneList = new HashSet<>();

	public static CommentDB commentDB;

	public Grader( Assignment cw, String loc, boolean doStats ) {

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		grader = this;

		root = new File (loc);
		commentDB = CommentDB.create( root );

		this.assignment = cw;
        toGrade = StudentFolder.find( new File ( loc), cw);

        if (toGrade.isEmpty()) {
        	System.out.println("didn't find any submission in " + new File(loc).getAbsolutePath() );
        	return;
		}

        StudentFolder toSelect = null;

		System.out.println("complete:");

		double meanTotal = 0;
		int meanCount = 0;

		for (StudentFolder sf : toGrade) {

			if (!new File(sf.folder, GRADE_FILE).exists()) {
				if (toSelect == null)
					toSelect = sf;
			} else {
				sgf = new File(sf.folder, GRADE_FILE);
				if (doStats && sgf.exists()) {
					StudentGrade sg = (StudentGrade) xStream.fromXML(sgf);

					meanTotal += sg.score();
					meanCount++;

				}

				System.out.println(sf.folder);
				doneList.add(sf);
			}

		}

			if (meanCount > 0) {
				System.out.println("done " + doneList.size() + " / " + toGrade.size());
				System.out.println("mean score is " + meanTotal / meanCount +" / " + cw.getMaxScore() +" ("+( meanTotal * 100 / (meanCount * cw.getMaxScore() ) ) +"%)");
			}

		studentModel = new DefaultComboBoxModel<>( toGrade );

		final JFXPanel fxPanel = new JFXPanel();

		JPanel browser = new JPanel(new BorderLayout());
		browser.add( fxPanel, BorderLayout.CENTER );
		browser.add( createForwardsBackwards(), BorderLayout.NORTH);
		browser.setPreferredSize(new Dimension(900,800));

		JSplitPane top = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		top.setDividerLocation(10);
		top.setLeftComponent( browser);
		top.setRightComponent( createSwingUI() );


		studentModel.setSelectedItem( toSelect == null ? toGrade.get( 0 ) : toSelect );

		Platform.runLater(() -> {

			html = new WebView();

			Scene scene = new Scene(html,300,300);
			fxPanel.setScene( scene );
			setFolder( 0 );

			WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
				if (message.startsWith("0xdeadbeef")) {
					processJSMessage(message.replaceAll("0xdeadbeef", ""));
				}
			});


			html.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>()
			{
				String prev = "";
				@Override
				public void changed( ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue)
				{
					String toBeopen = html.getEngine().getLoadWorker().getMessage().trim();
					System.out.println("tobeopen: " + toBeopen);

					if (toBeopen.startsWith("Loading http://") || toBeopen.startsWith("Loading https://") && newValue.toString() == "SCHEDULED") {

						html.getEngine().getLoadWorker().cancel();

						if (prev.compareTo(toBeopen) == 0)
							return;


						prev = toBeopen;

							new Thread() {
								public void run() {

									try {
										Runtime.getRuntime().exec( "xdg-open "+toBeopen.replace("Loading ", "" ) );
									} catch ( IOException e ) {
										e.printStackTrace();
									}
								}
							}.start();
					}
					else
						prev = "";
				}
			});

		});



		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing( WindowEvent e)
			{
//				System.out.println("Closed");
				trySave();
				commentDB.save();
			}
		});

		setContentPane( top );
		setSize( 2000,2000 );
		setVisible( true );
	}


    public static void actionForSelection(String selectedText) {
		grader.runJS("jsonHighlights('"+selectedText+"');");
	}

	private void processJSMessage(String jsonMessage) {
		JCommentBox.insert(jsonMessage);
	}

	private void runJS (String js) {
		Platform.runLater(() -> {
			html.getEngine().executeScript(js);
		});
	}

	private JComponent createForwardsBackwards() {

		JPanel out = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton b = new JButton("back");
		b.addActionListener(e -> {
			Platform.runLater(() -> {
				html.getEngine().executeScript("history.back()");
			});
		});

		out.add(b);

		return out;

	}

	private JComponent createSwingUI() {

		JPanel out = new JPanel();

		out.setLayout( new BorderLayout() );

		out.add(createSelector(), BorderLayout.NORTH);
		out.add(createSelector(), BorderLayout.SOUTH);

		out.add ( gradeUI, BorderLayout.CENTER );

		return out;
	}

	private JPanel createSelector() {

		JButton left = new JButton("<"), right = new JButton( ">" );
		JComboBox<StudentFolder> folders = new JComboBox<>(  );
		folders.setModel( studentModel );
		folders.addActionListener( x -> setFolder( (StudentFolder) studentModel.getSelectedItem() ) );

		left .addActionListener( x -> setFolder( -1 ) );
		right.addActionListener( x -> setFolder(  1 ) );

		JTextField typeToLoad = new JTextField("type to load...");
		typeToLoad.getDocument().addDocumentListener( new DocumentListener() {
			@Override public void insertUpdate( DocumentEvent e ) {
				changedUpdate( e );
			}

			@Override public void removeUpdate( DocumentEvent e ) {
				changedUpdate( e );
			}

			@Override public void changedUpdate( DocumentEvent e ) {

				for (StudentFolder sf : toGrade)
					for (String u : sf.users)
					if (typeToLoad.getText().equals( u )) {
						setFolder( sf );
						return;
					}
			}
		} );

		JButton explorer = new JButton("folder");
		explorer.addActionListener( x -> browse ( ((StudentFolder) studentModel.getSelectedItem()).folder ));

		int gridCount = 3;

		JButton run = null, show = null;

		if (assignment.getRun() != null) {
			gridCount++;
			run = new JButton( "run" );
			run.addActionListener( x -> run( ((StudentFolder) studentModel.getSelectedItem()).folder , assignment.getRun() ));
		}

		if (assignment.getShow((StudentFolder) studentModel.getSelectedItem()) != null) {
			gridCount++;
			show = new JButton( "show" );
			show.addActionListener( x -> assignment.getShow( ((StudentFolder) studentModel.getSelectedItem()) ).run() );
		}

		JLabel clock = new JLabel();
		gridCount++;
		new Timer(1000,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent actionEvent) {
						long ms = System.currentTimeMillis() - lastClick;
						clock.setText( String.format("%02d:%02d", ms / ( 60 * 1000), (ms / 1000)%60) );
					}
				}
				).start();

		JButton backup = new JButton("backup"); // time for a menu?
		if (true) {
			gridCount ++;
			backup = new JButton("backup");
			backup.addActionListener(e -> doBackup() );
		}

		JPanel selector = new JPanel( new GridLayout( 1, gridCount ) );
		selector.add(left);
		selector.add(clock);

		selector.add(folders);
		selector.add(typeToLoad);
		selector.add( explorer );


		if (run != null)
			selector.add( run );

		if (show != null)
			selector.add( show );

		if (backup != null)
			selector.add( backup );

		selector.add(right);

		return selector;
	}

	private void doBackup() {

		File folder = new File (root.getParent(), "backup_" + System.currentTimeMillis() );
		folder.mkdirs();

		for (StudentFolder sf : toGrade) {

			if (new File(sf.folder, GRADE_FILE).exists()) {

				File saveFile = new File(sf.folder, GRADE_FILE);

				try {
					Files.copy( saveFile.toPath(), new File (folder, "grade_xml_"+sf.folder.getName()+".xml").toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		browse(folder);
	}

	private void run( File folder, String run ) {

		try {
			System.out.println(new File (folder, run).getAbsolutePath().toString());
			Runtime.getRuntime().exec( new File (folder, run).getAbsolutePath()+" " + folder.getAbsolutePath() );
		} catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

	private void browse(File folder) {

		if (System.getProperty("os.name").toLowerCase().startsWith("win"))
			try {
				Desktop.getDesktop().open(folder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else if (Desktop.isDesktopSupported() && ! System.getProperty("os.name").toLowerCase().contains("linux"))
			Desktop.getDesktop().browseFileDirectory(folder);
		else {

			ProcessBuilder pb = new ProcessBuilder();
			pb.command("xdg-open", folder.getPath());
			try {
				Process p = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setFolder(StudentFolder sf) {

		lastClick = System.currentTimeMillis();

		studentModel.setSelectedItem( sf );
		selStudent = toGrade.indexOf( sf );

		setFolder();
	}

	public void setFolder (int i) {

		selStudent = (selStudent + i + toGrade.size() ) % toGrade.size();
		studentModel.setSelectedItem( toGrade.get( selStudent ));

		setFolder();
	}

	public synchronized void setFolder () {

		trySave();

		StudentFolder sf= toGrade.get( selStudent );

		Platform.runLater( () -> {

			File f = assignment.findHTMLForSubmission(sf);

			if ( html == null || f == null)
				return;

			if (f.exists() && f.toPath() != null)
				html.getEngine().load( "file://" + f.toPath().toString() );
			else
				html.getEngine().loadContent( "<html>not found <br/> "+f+"</html>" );

		} );

		gradeUI.removeAll();
		gradeUI.setLayout( new GridLayout( 1,1 ) );

		sgf = new File ( sf.folder, GRADE_FILE );
		if (sgf.exists())
			sg = (StudentGrade) xStream.fromXML(sgf);
		else {
			sg = new StudentGrade( assignment );
			assignment.init(sg, sf, assignment.findHTMLForSubmission(sf));
		}

		sff= sf;

		gradeUI.add( assignment.buildUI( sg, sf ));
		gradeUI.invalidate();
		gradeUI.revalidate();
	}

	private void trySave() {

		if (sg != null) {
			if (assignment.usefulContent ( sg ) ) {
				try {
					xStream.toXML( sg, new FileOutputStream( sgf ) );
					doneList.add( sff );
					setTitle( doneList.size() +" / "+ toGrade.size() );
				} catch ( Throwable e ) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String formatScore(double score) {
		return String.format( "%1.2f ", score );
	}


	public static void main (String[] args) {

//		Assignment cw = new CW1Assignment ();
//		Assignment cw = new CW2Assignment();
		Assignment cw = new CW3Assignment();

		//		SwingUtilities.invokeLater( () -> new Grader(cw, "/home/twak/Downloads/cw2_submissions") );
		SwingUtilities.invokeLater( () -> new Grader(cw, "c:\\Users\\twak\\Downloads\\gradebook_202122_32871_COMP2811_CW33a20The20Process_2021-12-16-19-15-57", true ) );
	}
}
