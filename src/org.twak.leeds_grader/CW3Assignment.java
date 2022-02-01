package org.twak.leeds_grader;

import org.apache.commons.io.FileUtils;
import org.twak.leeds_grader.StudentGrade.Response;
import org.twak.utils.Pair;
import org.twak.utils.ui.Rainbow;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CW3Assignment extends Assignment {

	public CW3Assignment() {

		add( new GradeItem("PACT analysis",        "Scoping the possible range of People, Activities, Context, and Technologies that might apply to this project.", 2, 1) );
		add( new GradeItem("Scenario",             "Three detailed scenarios matching market segment. Problem context and motivations explained. Sensible target platform identified.", 3) );
		add( new GradeItem("Ethics documentation", "Show appropriate information and consent forms.", 0.5, 0.5 ) );
		add( new GradeItem("English/structure",    "Spelling, grammar, structure of arguments, & document structure as specified.", 2.5, 1) );
		add( new GradeItem("Ambition",             "Developing significant and creative improvements to the Tomeo application in final demo video.", 1.5 ) );
		add( new GradeItem("Code",                 "Provided code is of high quality, runs, consistently formatted, and nicely commented. Matches demo video. Very long functions are have been refactored. Code should run on the School's CentOS lab machines.", 3, 3) );

		for (int i = 1; i <= 5; i++) {

			String cycle = ""+i;
			if ( i == 5 )
				cycle += " (extension)";

			Color c = Rainbow.getColour( i );
			Color cd = c.darker();

			add( new GradeItem("Cycle "+cycle+": prototype",  "Goal, technique, design paragraphs which justify this stage. Application of UI theory, prior evaluation, and use-cases to design. Clear evidence of prototype.", 3.50 ).setColor(c) );
			add( new GradeItem("Cycle "+cycle+": code",       "Video evidence, attention to detail, ambition, differences paragraph.", 1.50 ).setColor(c) );
			add( new GradeItem("Cycle "+cycle+": evaluation", "Choice and outcome paragraphs. Application of UI theory. Clear evidence of evaluation. (HE: heuristic evaluation; CW: cognitive walkthrough", 2.50 ).setColor(c) );

// 20-21: this took far too long to complete

//			add( new GradeItem("Cycle "+i+" prototype: goal",                "One paragraph describing the goal of this cycle and the reason this was selected as the highest priority.", 0.5).setColor(c) );
//			add( new GradeItem("Cycle "+i+" prototype: technique",           "Tried out a new prototyping technique.", 0.25 ).setColor(c) );
//			add( new GradeItem("Cycle "+i+" prototype: design paragraph",    "Paragraph motivating the design shown in your prototype. For example, build persuasive arguments with UX theory, scenarios, or prior evaluations.", 1).setColor(c) );
//			add( new GradeItem("Cycle "+i+" prototype: technique paragraph", "Paragraph giving the reason for the chosen technique. For example, discuss advantages/disadvantages of prototyping technique relative to other approaches.", 0.5).setColor(c) );
//			add( new GradeItem("Cycle "+i+" prototype: quality",             "Quality of prototype evidenced.", 1).setColor(c) );
//
//			add( new GradeItem("Cycle "+i+" code: evidence",    "Video evidence given.", 1).setColor(c) );
//			add( new GradeItem("Cycle "+i+" code: description", "Paragraph describing any differences between the prototype and code..", 0.5).setColor(c) );
//
//			add( new GradeItem("Cycle "+i+" evaluation: technique", "Tried out a new evaluation technique (CW: cognitive walkthrough; HE: heuristic evaluation).", 0.25  ).setColor(cd) );
//			add( new GradeItem("Cycle "+i+" evaluation: choice",    "Paragraph describing why this technique was chosen. For example, discuss advantages of evaluation approach relative to other approaches, scenario, and UX theory.", 1).setColor(cd) );
//			add( new GradeItem("Cycle "+i+" evaluation: outcomes",  "Paragraph describing outcome of this evaluation.", 0.5).setColor(cd) );
//			add( new GradeItem("Cycle "+i+" evaluation: quality",   "Quality of evaluation evidenced.", 1 ).setColor(cd) );
		}

		add( new GradeItem("Comments", "", 0) );
	}

	public File findHTMLForSubmission(StudentFolder sf) {

//		File f = new File (sf.folder, "pdf_shim");
//
//		if (f.exists())
//			return new File (f, "index.html");
//
//		try {
//			FileUtils.copyDirectory(new File("C:\\Users\\twak\\Documents\\GitHub\\leeds_grader"), new File (sf.folder, "pdf_shim") );
//			Files.copy( findReport( sf.folder, "report.pdf" ).toPath(), sf.folder.toPath());
//		} catch ( IOException e) {
//			e.printStackTrace();
//		}

		return null;
	}

	@Override public void init( StudentGrade sg,StudentFolder sf, File htmlForSubmission ) {
		super.init( sg, sf, htmlForSubmission );

		for (String s : sf.users)
			sg.contributionsClaimed.put( s, 100 );

	}

	public boolean showContribs() {
		return true;
	}

	public List<String> hardCodedComments() {

		List<String> out = new ArrayList<>(  );

		out.add("Limited discussion of scenario and UX theory.");
		out.add("Good English spelling and grammar.");
		out.add("Good English spelling and grammar. Weaker arguments/supporting statements.");
		out.add("Weak use of UI theory.");
		out.add("Excellent use of to UI theory.");
		out.add("Shallow evaluation - insufficient depth and detail to create actionable outcomes.");

		return out;
	}

	public boolean fileIsSubmission(File f) {
		return f.isDirectory();
	}

	@Override
	public Runnable getShow(StudentFolder selectedItem) {
		return new Runnable() {
			@Override
			public void run() {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(new File( selectedItem.folder, "report.pdf"));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		};
	}

	/******************************************************************************************************************************/
	/******************************************************************************************************************************/
	/******************************************************************************************************************************/
	/******************************************************************************************************************************/



	public static void processOut(File gradeCentreDownload, File markRoot, Assignment cw ) {

		File returnToStudents = new File( gradeCentreDownload.getParent(), "cw_3_results_2022" );
		returnToStudents.mkdirs();


		List<StudentFolder> groups = StudentFolder.find(markRoot, cw);

		Map<StudentFolder, StudentGrade> gradesAndDirs = readAllGrades(groups); // use applyLate here

//		applyCodeFromJialin(gradesAndDirs); - hack for TA's to insert coding comments in 21/22.

		Map<String, Pair<String, StudentGrade>> userToGroupFile = new HashMap<>();

		for (Map.Entry<StudentFolder, StudentGrade>  psf: gradesAndDirs.entrySet()) {

			String groupResultFolder = processGroup (psf.getValue(), returnToStudents,  psf.getKey(), cw );

			for (String user : psf.getKey().users)
				userToGroupFile.put(user, new Pair(groupResultFolder, psf.getValue()));
		}

		Set<String> seenUsers = new HashSet<>(), lostSubmissions = new HashSet<>(  );
		for (StudentFolder sf : groups)
			seenUsers.addAll(sf.users);

		try {
			List<String> lines = Files.readAllLines( gradeCentreDownload.toPath() );

			FileWriter fw = new FileWriter( new File( gradeCentreDownload.getParent(), "upload_to_minerva.csv" ) );

			for ( int i = 0; i < lines.size(); i++ ) {

				String[] csv = lines.get( i ).split( "," );
				String username = csv[ 2 ].replace( "\"", "" ), name = csv[ 0 ].replace( "\"", "" );

				Pair<String, StudentGrade> resultFolderAndGrade = userToGroupFile.get(username);

				if ( resultFolderAndGrade != null ) {

					double score = resultFolderAndGrade.second().score();

					if (resultFolderAndGrade.second().contributionsClaimed.keySet().contains( username ))
						score *= resultFolderAndGrade.second().contributionsClaimed.get( username ) / 100.;
					else
						System.err.println("no contribution given for " + username +"   :: " + resultFolderAndGrade.first() );

					score = Math.min( score, cw.getMaxScore() );

					seenUsers.remove(username);

					csv[ 6 ] = "\"" + Grader.formatScore( score ) + "\"";
					csv[ 9 ] = "\"" + name + ", your mark in Minerva (" + Grader.formatScore( score ) + ") takes your contribution (and special circumstances) into account. See OneDrive link sent via email for whole-group feedback and further information.\"";
				}
				else {
					if ( csv[ 6 ].compareTo( "\"Needs Grading\"")==0 ) {
						lostSubmissions.add(username);
					}
				}


				fw.append( join(csv) );
			}

			fw.flush();
			fw.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}

		if (!seenUsers.isEmpty()) {
			System.out.println("couldn't find in minerva's input:");
			for (String s : seenUsers)
				System.out.println(s);
		}


		if (!lostSubmissions.isEmpty()) {
			System.out.println("couldn't find grades for work that's submitted in Minerva:");
			for (String s : lostSubmissions)
				System.out.println(s);
		}
	}


	private static Map<StudentFolder, StudentGrade> readAllGrades(List<StudentFolder> groups) {

		Map<StudentFolder, StudentGrade> out = new HashMap<>();
		for (StudentFolder sf : groups) {
			StudentGrade sg = sf.readGrade();
			if (sg != null)
				out.put(sf, sg);
		}
		return out;

	}

	private static String join( String[] csv ) {

		StringJoiner sj = new StringJoiner( ",", "", "\n" );

		for ( String c : csv )
			sj.add( c );

		return sj.toString();
	}

	public boolean usefulContent(StudentGrade sg) {

		for (GradeItem gi : this) {
			Response r = sg.get( gi.name );
			if (r.mark != gi.def || r.comment.trim().length() > 0)
				return true;
		}

		return false;
	}

	private static String processGroup( StudentGrade sg, File forWeb, StudentFolder sf, Assignment cw1 ) {

		File gradeFile = sf.folder;


		String outputFolder = String.join("_", sf.users );

		File outputLocation = new File( forWeb, outputFolder );
		outputLocation.mkdirs();

		try {

			try {

				File f = findReport( sf.folder, "report.pdf" );


				if (f == null)
					throw new Error("missing report for " + sf.folder);

				FileUtils.copyFile( f, new File (outputLocation, "report.pdf") );

			} catch (Throwable th) {th.printStackTrace(); }

			FileWriter fw = new FileWriter( new File ( outputLocation, "index.html" ) );

			fw.write("<h3>Group: "+sf.group+"</h3>");
			fw.write( "<h3>Usernames: "+String.join(", ", sf.users )+"</h3><h3>Group-score: "+ sg.score() +
					" / " + cw1.getMaxScore() +"</h3><br/>\n");

			fw.write("<p>Submitted <a href=\"report.pdf\">report. Your feedback is in the table below.</a></p> <p>This is your group score, check Minerva for your personal score. If you submitted a contribution form (or have special circumstances) your personal score will be different in Minerva.</p>");

			fw.write("<p>If you wish to discuss your grade:<ul>" +
					"<li>read your results, below, carefully. Sometimes I wrote something under 'comments' at the bottom.</li>" +
					"<li>regrade requests may move your grade up or down.</li>" +
					"<li>to request a regrade, discuss with your group before having one member email <a href=\"mailto:scstke@leeds.ac.uk\">tom</a> before Friday 28th January.</li>" +
					"<li>this was a month-long project for five person groups, contributing 50% of the final mark. However, many submissions seemed rushed. For example,</li><ul>" +
					"<li>several groups hadn't followed the instructions - the mark scheme was closely based on these - so these groups did very poorly.</li>" +
					"<li>several groups hadn't followed the lectures - there were lots of hints on structured writing (lecture 17), the different types of evaluation (lectures 12 and 13), and UI theory (lectures 1-19).</li></ul>" +
					"<li>there were also some very good submissions! - thanks for your hard work.</li>" +
					"</ul></p>");

			fw.write( "<table border=\"2px\" style=\"border-collapse:collapse;\"> <tr><td><b>category</b></td><td><b>description</b></td><td><b>available</b></td><td><b>mark</b></td><td><b>comments</b></td></tr>" );

			for (GradeItem gi : cw1) {
				StudentGrade.Response resp=  sg.get( gi.name );
				fw.write( "<tr><td>" + gi.name +"</td><td>" + gi.studentDescription +"</td><td>" + gi.maxScore+
						" </td><td>" + Grader.formatScore( resp.mark ) +"</td><td>"+ resp.comment +"</td></tr>" );
				fw.write( "</td></tr>" );
			}

			String maxScore = Grader.formatScore( cw1.getMaxScore()), score = Grader.formatScore( sg.score() );

			if (cw1.getMaxScore() == 0) { // comments
				maxScore = "";
				score = "";
			}

			fw.write( "<tr><td><b>Total:</b></td><td></td><td></td><td><b>"+score+"</b></td><td></td></tr>" );
			fw.write( "</table>" );


			fw.flush();
			fw.close();


			File zips = new File (forWeb, "zips");
			zips.mkdirs();
			zip( outputLocation.getAbsolutePath(),  new File (zips, String.join("_", sf.users )+".zip").getAbsolutePath() );

		} catch ( Throwable e ) {
			e.printStackTrace();
		}

		return outputFolder;
	}

	public static void zip(String sourceDirPath, String zipFilePath) throws IOException { // https://stackoverflow.com/a/32052016/708802

		if (new File (zipFilePath).exists())
			Files.delete( new File ( zipFilePath ).toPath()) ;

		Path p = Files.createFile( Paths.get(zipFilePath));
		try ( ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp)
					.filter(path -> !Files.isDirectory(path))
					.forEach(path -> {
						ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
						try {
							zs.putNextEntry(zipEntry);
							Files.copy(path, zs);
							zs.closeEntry();
						} catch (IOException e) {
							System.err.println(e);
						}
					});
		}
	}

	public static String makePDFLinksClickable(String bodgedReport) {

		final String regex = "\\{\"page\":[^\\}]*\\}";
		String subst = "<a href=''>link</a>";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(bodgedReport);

		StringBuffer resultString = new StringBuffer();


		while (matcher.find()) {
			matcher.appendReplacement(resultString, "<a style='color:#0000ff;' onclick=jsonHighlights(\\'"+matcher.group(0)+"\\');>(eg)</a>" );
		}

		matcher.appendTail(resultString);

		return resultString.toString();
	}

	public static void main (String[] args) { // processout

		CW3Assignment.processOut (
				new File ( "path-to-minerva-csv"),
				new File ( "path-to-submission-root"),
				new CW3Assignment());
	}

}
