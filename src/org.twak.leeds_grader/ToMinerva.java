package org.twak.leeds_grader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ToMinerva {

	public ToMinerva( File src, File gradeRoot, int cw ) {


		File forWeb = new File( src.getParent(), "cw1_results" );
		forWeb.mkdirs();

		try {
			List<String> lines = Files.readAllLines( src.toPath() );

			FileWriter fw = new FileWriter( new File( src.getParent(), "upload_to_minerva.csv" ) );
			FileWriter mm = new FileWriter( new File( src.getParent(), "mailmerge.csv" ) );

			for ( int i = 0; i < lines.size(); i++ ) {

				String[] csv = lines.get( i ).split( "," );
				String username = csv[ 2 ].replace( "\"", "" ), name = csv[ 1 ].replace( "\"", "" );

				File gradeFile = new File( new File( gradeRoot, username ), Grader.GRADE_FILE );

				String[] mmCSV = new String[] {username, name, username+"@leeds.ac.uk", ""};

				if ( gradeFile.exists() ) {
					StudentGrade sg = (StudentGrade) Grader.xStream.fromXML( gradeFile );
					mmCSV[3] = processStudent (sg, forWeb, gradeFile, new CW2Assignment());
					csv[ 6 + cw ] = "\"" + Grader.formatScore( sg.score() ) + "\"";
				}

				fw.append( join(csv) );
				mm.append( join(mmCSV) );
//				if (i ==5)
//					break;
			}

			mm.flush();
			mm.close();
			fw.flush();
			fw.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	private static String join( String[] csv ) {

		StringJoiner sj = new StringJoiner( ",", "", "\n" );

		for ( String c : csv )
			sj.add( c );

		return sj.toString();
	}

	private String processStudent( StudentGrade sg, File forWeb, File gradeFile, Assignment cw1 ) {


		String secretKey = UUID.randomUUID().toString()+"-" +UUID.randomUUID().toString() ;

		File outputLocation = new File( forWeb, secretKey );
		outputLocation.mkdirs();




		try {
			FileWriter fw = new FileWriter( new File ( outputLocation, "index.html" ) );

			fw.write( /*"<h3>Username: "+gradeFile.getParentFile().getName()+"</h3>*/"<h3>Score: "+ sg.score() +
					" / " + cw1.getMaxScore() +"</h3><br/>\n");
			fw.write( "<table border=\"2px\" style=\"border-collapse:collapse;\"> <tr><td><b>category</b></td><td><b>description</b></td><td><b>available</b></td><td><b>mark</b></td><td><b>comments</b></td></tr>" );

			for (GradeItem gi : cw1) {
				StudentGrade.Response resp=  sg.get( gi.name );
				fw.write( "<tr><td>" + gi.name +"</td><td>" + gi.studentDescription +"</td><td>" + gi.maxScore+" </td><td>" + resp.mark +"</td><td>"+resp.comment+"</td></tr>" );
				fw.write( "</td></tr>" );
			}

			fw.write( "<tr><td></td><td></td><td><b>"+cw1.getMaxScore()+"</b></td><td><b>"+sg.score()+"</b></td><td></td></tr>" );
			fw.write( "<table>" );

			fw.write("<br/><br/><p>What follows is the result of a program designed to test your code. It is mostly correct, but not always. For example I rendered your layouts at some different sizes, and ignored the <i>Missing space before {</i> message from the static C++ analysis. I read the all output before manually grading.</p> </hr>");

			String bodgedReport = Files.readString( new File ( gradeFile.getParent(), "mark/index.html" ).toPath() );
			bodgedReport = bodgedReport.replaceAll( "../report/images/", "report_images/" );
			bodgedReport = bodgedReport.replaceAll( "<link href='../../prism-line-highlight.css' rel='stylesheet'/>", "" ); // oops - now inlined css
			bodgedReport = bodgedReport.replaceAll( gradeFile.getParentFile().getName(), "user" );

			fw.append( bodgedReport );
			fw.flush();
			fw.close();

			// copy images.
			try {
				FileUtils.copyDirectory( new File( gradeFile.getParent(), "mark/images" ), new File( outputLocation, "images" ) );
			} catch (Throwable th) {th.printStackTrace(); }

			try {
			FileUtils.copyDirectory( new File ( gradeFile.getParent(), "report/images" ), new File (outputLocation, "report_images") );
			} catch (Throwable th) {th.printStackTrace(); }

		} catch ( Throwable e ) {
			e.printStackTrace();
		}

		return secretKey;
	}

	public static void main (String[] args) {
//		new ToMinerva(new File ( ""),
//				new File ( ""), 0 );
	}
}
