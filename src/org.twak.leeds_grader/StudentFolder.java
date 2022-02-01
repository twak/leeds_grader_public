package org.twak.leeds_grader;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twak.leeds_grader.Grader.xStream;

public class StudentFolder {

    public File folder;

    public String group = null;
    public List<String> users;
    public Date date;

    public StudentFolder(File f, String user, int day, int month, int year, int hour, int min) {
        users = new ArrayList<>();
        users.add(user);
        this.folder = f;

        this.date = new Date( year - 1900, month-1, day, hour, min, 0 );

    }

    public StudentFolder(File f, String groupName, List<String> users) {
        this.folder = f;
        this.users = users;
        this.group = groupName;
    }

    public StudentFolder(File f, String groupName, List<String> users, int day, int month, int year, int hour, int min) {
        this.folder = f;
        this.users = users;
        this.group = groupName;

        this.date = new Date( year - 1900, month-1, day, hour, min, 0 );
    }

    public StudentGrade readGrade() {

        File sgf = new File ( folder, Grader.GRADE_FILE );
        StudentGrade sg = null;
        if (sgf.exists()) {
            sg = (StudentGrade) xStream.fromXML(sgf);
            return sg;
        }

        return null;
    }

    @Override
    public String toString() {
        return group != null ? group : String.join("_", users);
    }


    public static StudentFolder findFor(String usr, Vector<StudentFolder> data) {

        for (StudentFolder sf : data)
            for (String u : sf.users)
                if (u.equals(usr))
                    return sf;

        return null;
    }

    public static Vector<StudentFolder> find(File loc, Assignment cw) {

        Vector<StudentFolder> toGrade = new Vector();

        Pattern usernamePattern = Pattern.compile( "[^_]*_([a-zA-Z0-9 ]*)_attempt_([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)_([a-zA-Z0-9_]*)");

        for (File f : loc.listFiles())
            if ( cw.fileIsSubmission ( f ) ) {

                Matcher m  = usernamePattern.matcher( f.getName() );
                if ( m.find() ) {

                    String group = m.group( 1 ); // or username if not a group project
                    String day = m.group( 4 ), month = m.group(3), year = m.group( 2 );
                    String hour = m.group( 5 ), min = m.group( 6 ), sec = m.group(7);

                    File folder = new File (loc, group).getAbsoluteFile();

                    List<String> users = new ArrayList<>();
                    String[] vals = m.group(8).split("_");
                    for (int i = 0; i < vals.length; i++)
                        users.add(vals[i]);

                    if (users.size() > 1 || group.equals("cw3 68")) {

                        toGrade.add(new StudentFolder( f, group, users,
                            Integer.valueOf( day   ),
                            Integer.valueOf( month ),
                            Integer.valueOf( year  ),
                            Integer.valueOf( hour  ),
                            Integer.valueOf( min   ) ) );
                    } else {

                        toGrade.add(new StudentFolder(folder, group,
                            Integer.valueOf( day ),
                            Integer.valueOf( month ),
                            Integer.valueOf( year ),
                            Integer.valueOf( hour ),
                            Integer.valueOf( min )) );
                    }
                }
            }

        Collections.sort(toGrade, Comparator.comparing(w -> w.toString()));

        return toGrade;
    }

}
