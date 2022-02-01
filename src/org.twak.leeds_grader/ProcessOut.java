package org.twak.leeds_grader;

import org.apache.commons.io.FileUtils;
import org.twak.utils.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProcessOut {

	public ProcessOut(File gradeCentreDownload, File markRoot, Assignment cw ) {


		File returnToStudents = new File( gradeCentreDownload.getParent(), "cw_2_results_late_2021" );
		returnToStudents.mkdirs();


		List<StudentFolder> groups = StudentFolder.find(markRoot, cw);

//		Iterator<StudentFolder>sit = groups.iterator();
//		while (sit.hasNext()) {
//			if (!sit.next().folder.getName().equals("CW2 The process_cw2 36_attempt_2019-12-18-02-05-21_el18ikr_el18on_sc18pmb_sc18hsr_sc18ts"))
//				sit.remove();
//		}

		Map<StudentFolder, StudentGrade> gradesAndDirs = applyLate ( readAllGrades(groups), cw);

		Map<String, Pair<String, StudentGrade>> userToGroupFile = new HashMap<>();

		for (Map.Entry<StudentFolder, StudentGrade>  psf: gradesAndDirs.entrySet()) {

			String groupResultFolder = processGroup (psf.getValue(), returnToStudents,  psf.getKey(), new CW2Assignment() );

			for (String user : psf.getKey().users)
				userToGroupFile.put(user, new Pair(groupResultFolder, psf.getValue() ) );
		}

		Set<String> seenUsers = new HashSet<>();
		for (StudentFolder sf : groups)
			seenUsers.addAll(sf.users);

		try {
			List<String> lines = Files.readAllLines( gradeCentreDownload.toPath() );

			FileWriter fw = new FileWriter( new File( gradeCentreDownload.getParent(), "upload_to_minerva_lates.csv" ) );

			for ( int i = 0; i < lines.size(); i++ ) {

				String[] csv = lines.get( i ).split( "," );
				String username = csv[ 2 ].replace( "\"", "" ), name = csv[ 1 ].replace( "\"", "" );

				String[] mmCSV = new String[] {username, name, username+"@leeds.ac.uk", ""};
				Pair<String, StudentGrade> resultFolderAndGrade = userToGroupFile.get(username);

				if ( resultFolderAndGrade != null ) {
					seenUsers.remove(username);
					mmCSV[3] = resultFolderAndGrade.first();
					csv[ 6 ] = "\"" + Grader.formatScore( resultFolderAndGrade.second().score() ) + "\"";
					csv[ 9 ] = "\"See shared OneDrive folder (link in email) for feedback.\"";
				}

				fw.append( join(csv) );
			}

			fw.flush();
			fw.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}

		if (!seenUsers.isEmpty()) {
			System.out.println("couldn't find graded submissions for:");
			for (String s : seenUsers)
				System.out.println(s);
		}
	}

	private Map<StudentFolder, StudentGrade> applyLate( Map<StudentFolder, StudentGrade> readAllGrades, Assignment cw ) {

		for (Map.Entry<StudentFolder, StudentGrade> g : readAllGrades.entrySet()) {

			Date submitted = new Date ( g.getKey().date.getTime() - 1 * 60*60*1000 ); // grace period.

			if (submitted.after( cw.dueDate() )) {

				long diffInMillies = Math.abs( cw.dueDate().getTime() - submitted.getTime() );
				long daysLate = TimeUnit.DAYS.convert( diffInMillies, TimeUnit.MILLISECONDS ) + 1;

				// https://ses.leeds.ac.uk/info/22165/coursework/897/coursework_submission_overview
				double latePenalty = -0.05 * daysLate * cw.getMaxScore();

				StudentGrade.Response comment = g.getValue().get( "Comments" );
				comment.comment = String.format( "Penalty for late submission ("+daysLate+" days). If you have mitigating circumstances, Tom will remove this in Minerva: %.2f. %s. (grade without late penalty: %.2f)", latePenalty, comment.comment, g.getValue().score() );
				comment.mark = latePenalty;
			}
		}

		return readAllGrades;
	}

	private Map<StudentFolder, StudentGrade> readAllGrades(List<StudentFolder> groups) {

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

	private String processGroup( StudentGrade sg, File forWeb, StudentFolder sf, Assignment cw1 ) {

		File gradeFile = sf.folder;


		String outputFolder = String.join("_", sf.users );

		File outputLocation = new File( forWeb, outputFolder );
		outputLocation.mkdirs();

		try {

			try {
//				FileUtils.copyDirectory( new File ( gradeFile, "mark" ), outputLocation );

				FileUtils.copyDirectory( new File ( gradeFile, "images" ), new File (outputLocation, "images" ) );
				FileUtils.copyDirectory( new File ( gradeFile, "report" ), new File (outputLocation, "report" ) );


			} catch (Throwable th) {th.printStackTrace(); }

			FileWriter fw = new FileWriter( new File ( outputLocation, "index.html" ) );


//			fw.write("<table>\n" + // outer table
//					"  <tr>\n" +
//					"    <td style=\"width: 50%;\">");

			fw.write( "<h3>Username: "+String.join(", ", sf.users )+"</h3><h3>Score: "+ Grader.formatScore(sg.score()) +
					" / " + cw1.getMaxScore() +"</h3><br/>\n");
			fw.write( "<table border=\"2px\" style=\"border-collapse:collapse;\"> <tr><td><b>category</b></td><td><b>description</b></td><td><b>available</b></td><td><b>mark</b></td><td><b>comments</b></td></tr>" );

			for (GradeItem gi : cw1) {
				StudentGrade.Response resp=  sg.get( gi.name );
				fw.write( "<tr><td>" + gi.name +"</td><td>" + gi.studentDescription +"</td><td>" + gi.maxScore+
						" </td><td>" + Grader.formatScore( resp.mark ) +"</td><td>"+ resp.comment +"</td></tr>" );
//						" </td><td>" + resp.mark +"</td><td>"+ makePDFLinksClickable( resp.comment )+"</td></tr>" );
				fw.write( "</td></tr>" );
			}

			String maxScore = cw1.getMaxScore()+"", score = Grader.formatScore( sg.score() );

			if (cw1.getMaxScore() == 0) { // comments
				maxScore = "";
				score = "";
			}

			fw.write( "<tr><td></td><td></td><td><b>"+maxScore+"</b></td><td><b>"+ score+"</b></td><td></td></tr>" );
			fw.write( "</table>" );

//			fw.write("<br/>The graded report is shown to the right. Clicking the above blue (eg) links should highlight regions in your report. (This was only tested on Chrome.)</hr>");
//			fw.write("</td><td style=\"vertical-align: top; padding:10px; height: 512px;\">"); // outer table;

			String bodgedReport = Files.readString( new File ( gradeFile, "submission.html" ).toPath() );
//			String bodgedReport = Files.readString( new File ( gradeFile, "mark/index.html" ).toPath() );

//			bodgedReport = bodgedReport.replaceAll( "../report/images/", "report_images/" );
//			bodgedReport = bodgedReport.replaceAll( "<link href='../../prism-line-highlight.css' rel='stylesheet'/>", "" ); // oops - now inlined css
//			bodgedReport = bodgedReport.replaceAll( gradeFile.getParentFile().getName(), "user" );


//			if (sg.get( "Use of QtScrollArea" ).mark == 0 && (bodgedReport.contains( "QScrollArea " ) || bodgedReport.contains( "QScrollArea*" )  )) {
//				System.out.println(gradeFile.getParentFile().getName() );
//			}

			fw.append( bodgedReport );

//			fw.append("</td> </tr></table>");

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

		File zf = new File ( zipFilePath );
		if ( zf.exists() )
			Files.delete( zf.toPath() );

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

	public static void main (String[] args) {

		new ProcessOut(
				new File ( "path-to-minerva-csv"),
				new File ( "path-to-submission-root"),
				new CW2Assignment());
	}
}
