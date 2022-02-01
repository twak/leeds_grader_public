package org.twak.leeds_grader;

import org.twak.utils.ui.ListDownLayout;
import org.twak.utils.ui.SimplePopup2;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCommentBox extends JPanel  {

	static JCommentBox lastUsed = null;
	JTextArea commentLabel;
	String selectedText = "";
	Assignment assignment;

	public JCommentBox( GradeItem item, StudentGrade.Response response, ActionListener onChange, Assignment assignment ) {

		super( new BorderLayout(  ));
		lastUsed = this;
		this.assignment = assignment;

		commentLabel = new JTextArea( response.comment );
		commentLabel.setLineWrap(true);
		commentLabel.setWrapStyleWord(true);

		commentLabel.getDocument().addDocumentListener( new DocumentListener() {

			@Override
			public void removeUpdate( DocumentEvent e ) {
				response.comment  = commentLabel.getText(); onChange.actionPerformed( null );
			}

			@Override
			public void insertUpdate( DocumentEvent e ) {
				response.comment  = commentLabel.getText();  onChange.actionPerformed( null );
			}

			@Override
			public void changedUpdate( DocumentEvent e ) {
				response.comment  = commentLabel.getText();  onChange.actionPerformed( null );
			}
		} );

		commentLabel.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				selectedText = commentLabel.getSelectedText();
//				System.out.println(selectedText);
				lastUsed = JCommentBox.this;
			}
		});

		commentLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() != 1)
					Grader.actionForSelection(commentLabel.getSelectedText());
			}
		});


		JButton saveComment = new JButton( "+" );
		saveComment.setPreferredSize( new Dimension( 16,16 ) );
		saveComment.addActionListener( x -> insert( item.name, commentLabel.getText() ) );
		saveComment.setToolTipText("Insert into comment DB. Insert with <");

		JButton insertComment = new JButton( "<" );
		insertComment.setPreferredSize( new Dimension( 16,16 ) );
		insertComment.addMouseListener( new MouseAdapter() {
			@Override public void mouseReleased( MouseEvent e ) {
				showComments( commentLabel, item, e );
			}
		} );
		insertComment.setToolTipText("insert comment. Ctrl + click here to remove from list.");

		JPanel rightPanel = new JPanel( new ListDownLayout() );
		rightPanel.add( insertComment );
		rightPanel.add( saveComment );

		add(rightPanel, BorderLayout.EAST);
		add(commentLabel, BorderLayout.CENTER);
	}

	private void insert(String name, String text) {
		Grader.commentDB.insert(trimCommentKey(name), selectedText != null && selectedText.trim().length() > 0 ? selectedText : text );
	}

	private static String trimCommentKey(String name) {
		if (name.contains(":"))
			name = name.substring(name.indexOf(":")+1, name.length());
		return name;
	}

	private void showComments( JTextArea commentLabel, GradeItem gi, MouseEvent x ) {

		SimplePopup2 popup2 = new SimplePopup2( x );

		if (x.isControlDown())
			popup2.add("DELETE WHICH?", () -> System.out.println() );

		final String key = trimCommentKey( gi.name );
		List<String> items =  new ArrayList<>( Grader.commentDB.get( key ) );

		Collections.sort( items );

		items.add("---");

		for (String s : assignment.hardCodedComments())
			if (!items.contains( s ))
				items.add(s);

		for (String s : items)
			popup2.add(s, new Runnable() {
				@Override
				public void run() {
					if (x.isControlDown())
						Grader.commentDB.remove(key, s);
					else
						commentLabel.setText(commentLabel.getText()+" " + s.trim() );
				}
			});

		popup2.show();
	}

	public static void insert(String jsonMessage) {
		if (lastUsed != null) {
			JTextArea a = lastUsed.commentLabel;
			try {
				a.getDocument().insertString( a.getCaret().getDot(), jsonMessage, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}
