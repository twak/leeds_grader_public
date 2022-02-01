package org.twak.leeds_grader;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

import static org.twak.leeds_grader.Grader.formatScore;

public class GradeItem {
	public String studentDescription;
	String name;
	String desc;
	double maxScore;
	double def;
	boolean warnIfZero = true;
	private Color color;

	public GradeItem( String name, String desc, double max, double def) {
		this.name = name;
		this.desc = desc;
		this.studentDescription = desc;
		this.maxScore = max;
		this.def = def;
	}

	public GradeItem( String name, String desc, double max) {
		this (name, desc, max, 0);
	}

	public GradeItem ignoreWarn0Warn() {
		warnIfZero = false;
		return this;
	}

	public Component buildUI( StudentGrade.Response response, ActionListener onChange, Assignment ass ) {

		JPanel out = new JPanel( new BorderLayout(  ) );
		out.setBackground( color );

		JLabel nameL = new JLabel( getTitleText( response.mark ) );
		nameL.setFont( nameL.getFont().deriveFont( Font.BOLD, 20 ) );

		JPanel top = new JPanel( new BorderLayout(  ) );
		top.setBackground( color );

		top.add(nameL, BorderLayout.WEST);
		top.setBorder( new EmptyBorder( 10, 2, 10, 2 ) );

		JTextArea descA = new JTextArea( desc );
		descA.setBackground( color );
		descA.setLineWrap( true );
		descA.setEditable( false );
		descA.setOpaque(false);
		descA.setFont(UIManager.getFont("Label.font"));
		descA.setFocusable( false );
		descA.setMaximumSize( new Dimension( 400, 1000 ) );
		descA.setBorder( new EmptyBorder(8, 6, 0, 16  ) );

		top.add(descA, BorderLayout.CENTER);

		out.add(top, BorderLayout.NORTH );

		JPanel score = new JPanel( new FlowLayout(FlowLayout.LEFT) );
		score.setBackground( color );

		ButtonGroup bg = new ButtonGroup();

		if (maxScore != 0) {

			JPanel radios = new JPanel( new GridLayout(0, 8) );// FlowLayout(FlowLayout.LEFT) );
			radios.setBackground( color );
			score.add(radios);

		for (double d = 0; d < maxScore+0.1; d+= 0.25) {

			JRadioButton hb = new JRadioButton( formatScore( d ) );
			hb.setBackground( color );

			double dd = d;

			ActionListener al = e -> nameL.setText( getTitleText( dd ) );
			hb.addActionListener( al );
			hb.addActionListener( c -> top.setBackground( getBGColor(response) ) );
			hb.addActionListener( onChange );
			hb.addActionListener( c -> response.mark = dd );

			bg.add( hb );
			radios.add(hb);

			if (d <= response.mark)
				hb.setSelected( true );
		}
		}


		JPanel middle = new JPanel( new GridLayout( 1, 3 ) );

		middle.add( score );
		middle.add( new JCommentBox(this, response, c -> top.setBackground( getBGColor(response) ), ass) );

		out.add( middle, BorderLayout.CENTER );
		out.setBorder( new CompoundBorder( new EmptyBorder( 16, 0, 0, 0 ),  new LineBorder(Color.darkGray, 1 ) ) );
		top.setBackground( getBGColor(response) );

		return out;
	}

	private Color getBGColor( StudentGrade.Response response ) {
		if ( warnIfZero && response.mark != maxScore && response.comment.isBlank())
			return Color.red;

		return color;
	}

	private String getTitleText( double dd ) {

		return formatScore ( dd ) + " / " + formatScore( maxScore ) + " " + name;
	}

	public GradeItem setColor( Color c ) {
		this.color = c;
		return this;
	}
}
