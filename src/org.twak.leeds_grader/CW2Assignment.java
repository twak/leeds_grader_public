package org.twak.leeds_grader;

import org.apache.commons.io.FileUtils;
import org.twak.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CW2Assignment extends Assignment {

	GradeItem tbs, comm, cap, flen, llen, seventeen, all, fewer, extra, ex1, ex2, compiles, runs, tomsComments, size, spacing, gaps;

	public CW2Assignment() {

		//widgets which do not overlap and do not extend beyond the edge of the window.

		add( compiles = new GradeItem("Code compiles", "0.5 marks for compiling code. "
				+ "0.5 marks no/few compiler errors in code.", 1, 1) );
		add( runs = new GradeItem("Code runs", "0.5 mark for not crashing. "
				+ "0.5 marks for successfully displaying layouts at a range of different sizes.", 1, 1) );
		add( comm = new GradeItem("Code comments", "Single line only using \"//\".", 1) );
		add( flen = new GradeItem("Function Length", "No function longer than 50 lines.", 1, 1) );
		add( llen = new GradeItem("Line length", "No function longer than 50 lines.", 1, 1) );
		add( cap = new GradeItem("Capitalisation", "As style guide. lose 0.5 marks for each error.", 1, 1) );
		add( tbs = new GradeItem("Braces and Indent", "As style guide.", 2, 2) );
		add( new GradeItem("Patch file", "Report contains report.html and associated images. "
				+ "Report matches your code is run.", 1, 1) );
		add( new GradeItem("Labels:selection", "Appropriate labels in response to varying window sizes ", 2, 2) );
		add( all = new GradeItem("Labels:all at large", "All labels shown at largest size ", 1, 1) );
		add( fewer = new GradeItem("Labels:fewer when smaller", "Sensible selection of labels shown at smaller sizes", 1, 1) );
		add( extra = new GradeItem("Labels: two extra", "Two extra labels created", 1, 1 ) );
		add( size = new GradeItem("Labels: size and shape", "Appropriate to emulated device size. Appropriate label aspect ratio.", 2, 1) );
		add( new GradeItem("Labels: discrete layouts", "4 discrete layouts (2 marks each)."
				+ "e.g. adding/removing/moving labels or rows of labels.", 8, 4) );

		add( spacing = new GradeItem("Spacing: consistent", "Beautiful, non-zero, and appropriate spacing - "
				+ "unless there's a good reason otherwise (scroll area, alignment etc..)."
				+ " For example: always 5px horizontal and vertical space between elements.", 2, 2) );
		add( gaps = new GradeItem("Spacing: gaps", "No large empty areas unless typical for such an app. ", 1, 1) );
		add( ex1  = new GradeItem("QtScrollArea: correct use", "Correct use of QtScrollArea", 2).ignoreWarn0Warn() );
		add( ex2  = new GradeItem("QtScrollArea: not fullscreen", "Search results are scrollable with minimal bugs."
				+ "The whole screen does not scroll, only results.", 1).ignoreWarn0Warn() );
		add( seventeen = new GradeItem("QtScrollArea: seventeen", "17 search results in a grid (1 mark). ", 1).ignoreWarn0Warn() );
		add( new GradeItem("QtScrollArea: search result layout", "Final row well nicely layed-out(1 mark). \n" +
				"Responsive number of columns or rows (1 mark)", 2).ignoreWarn0Warn() );
		add( new GradeItem("QtScrollArea: square search results (combined image and text should be square)", "Search results in scroll area should be square.", 2).ignoreWarn0Warn() );


		add( tomsComments = new GradeItem("Comments", "", 0) );
	}

	@Override public void init( StudentGrade sg, StudentFolder sf, File htmlForSubmission ) {

		sg.get( tomsComments.name ).mark = 0;
		sg.get(size.name).comment = "mostly well researched and appropriate to physical size.";
		sg.get(spacing.name).comment = "mostly good horizontally and vertically, occasionally inconsistent in places";
		sg.get(all.name).comment = "All labels present at largest resolution.";
		sg.get(gaps.name).comment = "no large gaps except where appropriate";

		try {
			String html = Files.readString( htmlForSubmission.toPath() );
			Pattern vn;
			Matcher m;

			boolean extension = false;

			sg.get( compiles.name ).comment = "Good. Insignificant warnings.";
			sg.get( runs.name ).comment = "good :)";

			// attempted extension
//			vn = Pattern.compile( "QScrollArea" );
			vn = Pattern.compile( "new QScrollArea" );
			m = vn.matcher( html );
			if (m.find()) {
				extension = true;
				sg.get( ex1.name ).comment = "Nice working QScrollArea.";
				sg.get( ex1.name ).mark = ex1.maxScore;
				sg.get( ex2.name ).comment = "Sensible area scrolled.";
				sg.get( ex2.name ).mark = ex2.maxScore;
			}

			// max number of labels
			vn = Pattern.compile( "<br/> used: ([0-9]+) results: ([0-9]+) </td>", Pattern.DOTALL );
			m = vn.matcher( html );
			int min = Integer.MAX_VALUE, max = -Integer.MAX_VALUE;
			int non17Results=0;

			while (m.find()) {
				int labelTypes = Integer.parseInt( m.group( 1 ) );
				min = Math.min (min, labelTypes);
				max = Math.max (max, labelTypes);

				if (Integer.parseInt( m.group( 2 ) ) != 17) {
					non17Results++;
				}
			}

			// label counts

			if (min < max -3) {
				sg.get( fewer.name ).comment = "good";
				sg.get( fewer.name ).mark = fewer.maxScore;
			}
			else if (min != max) {
				sg.get( fewer.name ).comment = "small change";
				sg.get( fewer.name ).mark = 0.5;
			}
			else {
				sg.get( fewer.name ).comment = "did not reduce number of labels on smaller layouts.";
				sg.get( fewer.name ).mark = 0;
			}

			if (max < 13) {
				sg.get( all.name ).comment = "did not find all label types.";
				sg.get( all.name ).mark = 0;
			}
			else
				sg.get( all.name ).comment = "good.";

			if (max >= 15) {
				sg.get( extra.name ).comment = "two additional labels created.";
				sg.get( extra.name ).mark = 1;
			}
			else if (max == 14) {
				sg.get( extra.name ).comment = "one additional label created.";
				sg.get( extra.name ).mark = 0.5;
			}
			else {
				sg.get(extra.name).comment = "no additional labels.";
				sg.get( extra.name ).mark = 0;
			}

			if (extension) {
				if ( non17Results > 0 ) {

					sg.get( seventeen.name ).mark = 0;
					sg.get( seventeen.name ).comment = "Did not have 17 result labels.";
				} else {
					sg.get( seventeen.name ).comment = "good :)";
					sg.get( seventeen.name ).mark = seventeen.maxScore;
				}
			}
			else {
				sg.get(ex1.name).comment = "not attempted";
			}

			// braces
			{
				vn = Pattern.compile( "should almost always be at the end of the previous line|An else should appear on the same line as the preceding");
				m = vn.matcher( html );

				int count = 0;
				while ( m.find() )
					count++;

				if ( count > 5 ) {
					sg.get( tbs.name ).mark = 0;
					sg.get( tbs.name ).comment = "some problems with indentation/braces.";
				}
				else if ( count > 2 ) {
					sg.get( tbs.name ).mark = 1;
					sg.get( tbs.name ).comment = "occasional problems with indentation/braces";
				}
			}

			// comments
			{
				vn = Pattern.compile( "/*!( our layout should always fit inside)", Pattern.DOTALL ); //([.*?])</p>
				boolean multi = vn.matcher( html ).find();

				vn = Pattern.compile( "//", Pattern.DOTALL ); //([.*?])</p>
				m = vn.matcher( html );

				int count = 0;
				while ( m.find() )
					count++;

				if (multi) {
					sg.get( comm.name ).mark = 0;
					sg.get( comm.name ).comment = "multi-line commnets used :(";
				} else if (count > 60) {
					sg.get( comm.name ).mark = 1;
					sg.get( comm.name ).comment = "Strong commenting (" + count+" comment(s))";
				} else if (count > 52) {
					sg.get( comm.name ).mark = 0.5;
					sg.get( comm.name ).comment = "Some comments.";
				} else {
					sg.get( comm.name ).mark = 0.5;
					sg.get( comm.name ).comment = "Poor comments.";
				}

			}



			// function length errors
			vn = Pattern.compile( "Small and focused functions are preferred", Pattern.DOTALL ); //([.*?])</p>
			m = vn.matcher( html );
			if (m.find()) {
				sg.get( flen.name ).mark = 0;
				sg.get( flen.name ).comment = "Long function(s) found. (Comments and empty lines excluded).";
			}
			else
				sg.get( flen.name ).comment = "good";

			// line-length errors
			vn = Pattern.compile( "Lines should be <= [0-9]* characters long", Pattern.DOTALL );

			m = vn.matcher( html );
			if (m.find()) {
				int count = 1;
				while (m.find())
					count++;

				if (count > 0) {
					sg.get( llen.name ).mark = 0;
					sg.get( llen.name ).comment = count + " long line(s) found. (Comments excluded, but not whitespace).";
				}
				else
					sg.get( llen.name ).comment = "good";
			}
			else
				sg.get( llen.name ).comment = "good";


			// variable name/capitalisation errors?
			vn = Pattern.compile( "<h4>functions</h4><p>(.*?)</p>", Pattern.DOTALL ); //([.*?])</p>

			m = vn.matcher( html );
			int fnCount = 0;
			String fnErrorString = "";

			if (false) { // didn't specify lower-case functions in 2020
				if ( m.find() ) {

					String[] vars = m.group( 1 ).split( ", " );

					for ( String v : vars ) {
						v = v.replace( "\n", "" );
						if ( !Character.isLowerCase( v.charAt( 0 ) ) && !v.contains( "ResponsiveLabel" ) && !v.contains( "ResponsiveWindow" ) && !v.contains( "ResponsiveLayout" ) && !v.startsWith( "~" ) ) {

							if ( html.contains( "class " + v ) )
								continue;

							fnErrorString += v + ", ";
							fnCount++;
						}
					}
				} else
					sg.get( cap.name ).comment = "good";
			}

			int varCount = 0;
			String varErrorString = "";

			vn = Pattern.compile( "<h4>(most) vars</h4><p>(.*?)</p>", Pattern.DOTALL ); //([.*?])</p>
			m = vn.matcher( html );
			if (m.find()) {

				String[] vars = m.group( 1 ).split( ", " );
				for ( String v : vars ) {
					v = v.replace( "\n", "" );
					if ( !Character.isLowerCase( v.charAt( 0 ) ) ) {
						varCount++;
						varErrorString += v+", ";
					}
				}
			}

			int klassCount = 0;
			String klassErrorString = "";

			vn = Pattern.compile( "<h4>classes</h4><p>(.*?)</p>", Pattern.DOTALL ); //([.*?])</p>
			m = vn.matcher( html );
			if (m.find()) {

				String[] vars = m.group( 1 ).split( ", " );
				for ( String v : vars ) {
					v = v.replace( "\n", "" );
					if ( v.length() == 0 || Character.isLowerCase( v.charAt( 0 ) ) ) {
						klassCount++;
						klassErrorString += v+", ";
					}
				}
			}

			if (fnCount > 0 || varCount > 0 || klassCount > 0) {
				sg.get( cap.name ).mark = 0;
				sg.get( cap.name ).comment = "Poorly capitalised functions/variables/classes: " + fnErrorString + varErrorString + klassErrorString;
			}
			else
				sg.get( cap.name ).comment = "good";


		} catch ( IOException e ) {
			e.printStackTrace();
		}

	}

	@Override public Date dueDate() {

		return new Date(2021-1900, 11-1, 15, 12, 0, 0);
	}

	@Override public String getRun() {

		return "responsive";
	}


	/******************************************************************************************************************************/
	/******************************************************************************************************************************/
	/******************************************************************************************************************************/
	/******************************************************************************************************************************/



	public static void processOut(File gradeCentreDownload, File markRoot, Assignment cw ) {


		File returnToStudents = new File( gradeCentreDownload.getParent(), "cw_2_results_2021" );
		returnToStudents.mkdirs();


		List<StudentFolder> groups = StudentFolder.find(markRoot, cw);

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

			FileWriter fw = new FileWriter( new File( gradeCentreDownload.getParent(), "upload_to_minerva.csv" ) );

			for ( int i = 0; i < lines.size(); i++ ) {

				String[] csv = lines.get( i ).split( "," );
				String username = csv[ 2 ].replace( "\"", "" ), name = csv[ 0 ].replace( "\"", "" );

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

	private static Map<StudentFolder, StudentGrade> applyLate( Map<StudentFolder, StudentGrade> readAllGrades, Assignment cw ) {

		for (Map.Entry<StudentFolder, StudentGrade> g : readAllGrades.entrySet()) {

			Date submitted = new Date ( g.getKey().date.getTime() - 1 * 60*60*1000 ); // grace period.

			if (submitted.after( cw.dueDate() )) {

				long diffInMillies = Math.abs( cw.dueDate().getTime() - submitted.getTime() );
				long daysLate = TimeUnit.DAYS.convert( diffInMillies, TimeUnit.MILLISECONDS ) + 1;

				// https://ses.leeds.ac.uk/info/22165/coursework/897/coursework_submission_overview
				double latePenalty = -0.05 * daysLate * cw.getMaxScore();

				StudentGrade.Response comment = g.getValue().get( "Comments" );
				comment.comment = String.format( "Penalty for late submission ("+daysLate+" days). If you have mitigating circumstances, Tom will remove this in Minerva: %.2f. %s", latePenalty, comment.comment );
				comment.mark = latePenalty;
			}
		}

		return readAllGrades;
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

	private static String processGroup( StudentGrade sg, File forWeb, StudentFolder sf, Assignment cw1 ) {

		File gradeFile = sf.folder;


		String outputFolder = String.join("_", sf.users );

		File outputLocation = new File( forWeb, outputFolder );
		outputLocation.mkdirs();

		try {

			try {

				FileUtils.copyDirectory( new File ( gradeFile, "images" ), new File (outputLocation, "images" ) );
				FileUtils.copyDirectory( new File ( gradeFile, "report" ), new File (outputLocation, "report" ) );


			} catch (Throwable th) {th.printStackTrace(); }

			FileWriter fw = new FileWriter( new File ( outputLocation, "index.html" ) );

			fw.write( "<h3>Username: "+String.join(", ", sf.users )+"</h3><h3>Score: "+ sg.score() +
					" / " + cw1.getMaxScore() +"</h3><br/>\n");
			fw.write( "<table border=\"2px\" style=\"border-collapse:collapse;\"> <tr><td><b>category</b></td><td><b>description</b></td><td><b>available</b></td><td><b>mark</b></td><td><b>comments</b></td></tr>" );

			for (GradeItem gi : cw1) {
				StudentGrade.Response resp=  sg.get( gi.name );
				fw.write( "<tr><td>" + gi.name +"</td><td>" + gi.studentDescription +"</td><td>" + gi.maxScore+
						" </td><td>" + resp.mark +"</td><td>"+ resp.comment +"</td></tr>" );
				fw.write( "</td></tr>" );
			}

			String maxScore = cw1.getMaxScore()+"", score = sg.score() +"";

			if (cw1.getMaxScore() == 0) { // comments
				maxScore = "";
				score = "";
			}

			fw.write( "<tr><td></td><td></td><td><b>"+maxScore+"</b></td><td><b>"+score+"</b></td><td></td></tr>" );
			fw.write( "</table>" );

			String bodgedReport = Files.readString( new File ( gradeFile, "submission.html" ).toPath() );
			fw.append( bodgedReport );

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


		//		String bodgedReport = "Test{\"page\":Test1}{\"page\":foobar}Test3\n";

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

		processOut(
				new File ( "path-to-minerva-csv"),
				new File ( "path-to-submission-root"),
				new CW2Assignment());
	}

}
