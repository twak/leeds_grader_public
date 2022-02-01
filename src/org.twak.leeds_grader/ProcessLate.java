package org.twak.leeds_grader;

import org.twak.leeds_grader.StudentGrade.Response;
import org.twak.utils.Pair;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProcessLate {

	public ProcessLate(File gradeCentreDownload, File markRoot, List<String> mitigating, Assignment cw) {


		File returnToStudents = new File( gradeCentreDownload.getParent(), "cw_2_results" );
		returnToStudents.mkdirs();


		List<StudentFolder> groups = StudentFolder.find(markRoot, cw);


		Map<StudentFolder, StudentGrade> gradesAndDirs;

		gradesAndDirs = applyLate ( readAllGrades( groups, cw), cw);

		Map<String, Pair<String, StudentGrade>> userToGroupFile = new HashMap<>();

		for (Map.Entry<StudentFolder, StudentGrade>  psf: gradesAndDirs.entrySet()) {

			for (String user : psf.getKey().users)
				userToGroupFile.put(user, new Pair("shouldnt need this", psf.getValue() ) );
		}

		Set<String> seenUsers = new HashSet<>();
		List<String> lateUsers = new ArrayList<>();

		for (StudentFolder sf : groups)
			seenUsers.addAll(sf.users);

		try {
			List<String> lines = Files.readAllLines( gradeCentreDownload.toPath() );

			FileWriter fw = new FileWriter( new File( gradeCentreDownload.getParent(), "upload_to_minerva.csv" ) );

			for ( int i = 0; i < lines.size(); i++ ) {

				String[] csv = lines.get( i ).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				if (csv.length <= 10) {
					System.out.println("bad line in csv " + lines.get(i) );
					continue;
				}

				String username = csv[ 2 ].replace( "\"", "" ), name = csv[ 1 ].replace( "\"", "" );

				String[] mmCSV = new String[] {username, name, username+"@leeds.ac.uk", ""};
				Pair<String, StudentGrade> resultFolderAndGrade = userToGroupFile.get(username);

				if ( resultFolderAndGrade != null ) {

					Response lateR = resultFolderAndGrade.second().get("late");
					double mult = lateR.mark;

					if (mult != 0 ) {

						if (mitigating.contains(username)) {
							System.out.println("mitigating " + username );
						} else {

							lateUsers.add(username);

							double grade = Math.ceil(Double.parseDouble(csv[6].replace("\"", "")));
							csv[6] = "\"" + (grade + grade * mult) + "\"";
							csv[10] = csv[8] = "SMART_TEXT";
							csv[9] = csv[9].replaceAll("\"", "");
							csv[9] = "\"" + csv[9] + ". " + lateR.comment + " Late penalty: " + (15 * mult) + "\"";
						}

					}

					seenUsers.remove(username);
				}

				csv[9] = csv[9].replaceAll("&nbsp;", "&");
				fw.append( join(csv) );
			}

			fw.flush();
			fw.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}

		if (!lateUsers.isEmpty()) {

			System.out.println("\nlate penalty applied to (paste into outlook):");
			for (String tardy : lateUsers)
				System.out.print(tardy+"; ");
			System.out.println();
		}

		if (!seenUsers.isEmpty()) {
			System.out.println("\ncouldn't find graded submissions for:");
			for (String s : seenUsers)
				System.out.print(s+"; ");
			System.out.println();
		}
	}

	private Map<StudentFolder, StudentGrade> applyLate( Map<StudentFolder, StudentGrade> readAllGrades, Assignment cw ) {

		for (Map.Entry<StudentFolder, StudentGrade> g : readAllGrades.entrySet()) {

			Date submitted = new Date ( g.getKey().date.getTime() - 1 * 60*60*1000 ); // grace period.

			if (submitted.after( cw.dueDate() )) {

				long diffInMillies = Math.abs( cw.dueDate().getTime() - submitted.getTime() );
				long daysLate = TimeUnit.DAYS.convert( diffInMillies, TimeUnit.MILLISECONDS ) + 1;

				// https://ses.leeds.ac.uk/info/22165/coursework/897/coursework_submission_overview
				double latePenalty = Math.min (0, -0.05 * daysLate);


				Response comment = g.getValue().get( "late" );

				if (comment == null) {
					comment = new Response();
					g.getValue().put("late", comment);
				}

				comment.comment = String.format( "Penalty for late submission ("+daysLate+" days). " );
				comment.mark = latePenalty;
			}
		}

		return readAllGrades;
	}


	private Map<StudentFolder, StudentGrade> readAllGrades(List<StudentFolder> groups, Assignment cw) {

		Map<StudentFolder, StudentGrade> out = new HashMap<>();
		for (StudentFolder sf : groups) {
			StudentGrade sg = sf.readGrade();
			out.put(sf, new StudentGrade(cw));
		}
		return out;

	}

	private static String join( String[] csv ) {

		StringJoiner sj = new StringJoiner( ",", "", "\n" );

		for ( String c : csv )
			sj.add( c );

		return sj.toString();
	}

	public static void main (String[] args) {

		List<String> mitigating = new ArrayList<>();

		for (String s : "sc10xx;sc11xy;sc12xz".split(";") )
			mitigating.add(s);

		new ProcessLate(

				new File ( "path-to-minerva-csv"), // the CSV download (grade centre, top right, download...) of the assignment
				new File ( "path-to-submission-root"), // all of the xxx_attempt_date.txt that come down from Minerva with the assignment file downloads
				mitigating, // as above, list of usernames to ignore due to mit. circs.
				new CW1Assignment());
	}
}
