package org.twak.leeds_grader;

import org.twak.leeds_grader.StudentGrade.Response;
import org.twak.utils.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

import static org.twak.leeds_grader.Grader.formatScore;

public class Assignment extends ArrayList<GradeItem> {

	public JComponent buildUI(StudentGrade sGrade, StudentFolder studentFolder) {

		JPanel out = new JPanel(new ListDownLayout() );

		JLabel score = new JLabel( toplabel( sGrade ) );
		score.setFont( score.getFont().deriveFont( Font.BOLD, 20 ) );
		ActionListener bumpScore = x -> score.setText( toplabel( sGrade ) );
		score.setPreferredSize( new Dimension( 100, 100 ) );

		out.add(score);


		for (int i = 0; i < size(); i++) {
			GradeItem gi = get(i);
			Component comp = gi.buildUI( sGrade.get( gi.name ), bumpScore, this );
//			if (i%2 == 0)
//				comp.setBackground( new Color(200,200, 255) );
			out.add(comp);
		}


		if (showContribs())
			out.add( buildContribs( sGrade, studentFolder ) );

		JScrollPane scroll = new JScrollPane(out);
		scroll.getVerticalScrollBar().setUnitIncrement( 50 );
		scroll.getVerticalScrollBar().setValue( 0 );

		return scroll;
	}

	private JPanel buildContribs( StudentGrade sGrade, StudentFolder studentFolder ) {

		return new MapEditor(sGrade.contributionsClaimed);
	}

	private String toplabel( StudentGrade sGrade ) {

		return formatScore( sGrade.score() ) + " / " + formatScore( getMaxScore() ) + "   " + String.format( "%.0f", sGrade.score() * 100 / getMaxScore() )+"%";
	}

	public double getMaxScore() {
		return this.stream().mapToDouble( x -> x.maxScore ).sum();
	}

	public void init( StudentGrade sg, StudentFolder sf, File htmlForSubmission ) {
		//override me
	}

	public String getRun() {
		return null;
	}

	public Runnable getShow(StudentFolder selectedItem) {
		return null;
	}

	public Date dueDate() {
		return new Date(2020-1900, 11-1, 16, 23, 59 );
	}

	public File findHTMLForSubmission(StudentFolder sf) {
		return new File (sf.folder, "submission.html");
	}

	protected static File findReport (File start, String name) {

		for (File f : start.listFiles())
			if (f.getName().contains("__MACOSX"))
				continue;
			else if (!f.isDirectory()) {
				if (f.getName().compareTo( name ) == 0)
					return f;
			}
			else {
				File out = findReport( f, name );
				if (out != null)
					return out;
			}

		return null;
	}

	public boolean showContribs() {
		return false;
	}

	public List<String> hardCodedComments() {
		return Collections.emptyList();
	}

	public boolean usefulContent(StudentGrade sg) {
		for (GradeItem gi : this) {
			Response r = sg.get( gi.name );
			if ( gi.warnIfZero && r.mark != gi.maxScore && r.comment.isBlank() ) // "is red".
				return false;
		}
		return true;
	}

    public boolean fileIsSubmission(File f) {
		return f.getName().endsWith( ".patch" );
	}
}
