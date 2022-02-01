package org.twak.leeds_grader;

import org.twak.utils.Pair;
import org.twak.utils.ui.ListDownLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author twak
 */
public class MapEditor extends JPanel {

	public Map<String, Integer> map2 = new LinkedHashMap<>();
	public List<Pair<String,Integer>> mapProxy = new ArrayList<>();
	public boolean fireSelection = true;

	/** Creates new form ListEditor */
	public MapEditor( Map<String,Integer> map ) {
		this.map2 = map;

		buildProxy();

		init();

	}

	private void buildProxy() {
		if (map2 != null)
			for ( Map.Entry<String,Integer> e : map2.entrySet() )
				mapProxy.add( new Pair<String,Integer> ( e.getKey(), e.getValue() ) );
	}

	private synchronized void saveToMap() {

		map2.clear();

		for (Pair<String,Integer> p : mapProxy)
			map2.put(p.first(), p.second());
	}

	public void init()
	{
		removeAll();
		setLayout( new ListDownLayout() );

			for ( Pair<String,Integer> entry : mapProxy ) {
				add(new MapEntry(entry));
			}

		JButton add = new JButton( "+" );
		add(add);
		add.addActionListener( i->add() );
	}

	void add() {
		mapProxy.add(new Pair<>("", 100));
		saveToMap();
		init();
		revalidate();
		repaint();
	}

	private void set( Pair<String, Integer> entry, String text, Integer value ) {

		entry.set1(text);
		entry.set2(value);

		saveToMap();
	}

	public class DL implements DocumentListener {

		Pair<String, Integer> entry;

		public DL (Pair<String, Integer> entry ) {
			this.entry = entry;
		}

		@Override public void insertUpdate( DocumentEvent e ) {
			set (entry, e(e), entry.second() );
		}

		private String e( DocumentEvent e ) {

			try {
				return e.getDocument().getText( 0, e.getDocument().getLength() );
			} catch ( BadLocationException e1 ) {
				e1.printStackTrace();
			}
			return null;
		}

		@Override public void removeUpdate( DocumentEvent e ) {
			set (entry, e(e), entry.second() );
		}

		@Override public void changedUpdate( DocumentEvent e ) {
			set (entry, e(e), entry.second() );
		}
	}

	public class MapEntry extends  JPanel
	{
		public MapEntry(Pair<String,Integer> entry) {

			setLayout( new BorderLayout());

			JTextField left = new JTextField(entry.first());
			JSpinner right = new JSpinner( new SpinnerNumberModel( (int) entry.second(), 0, 500, 1 ) );
			JButton del = new JButton("x");

			left .getDocument().addDocumentListener( new DL(entry ) );
			right.addChangeListener( i -> set (entry, entry.first(), (Integer) right.getValue() ) );
			del  .addActionListener( i -> del(entry) );

			JPanel entryP = new JPanel( new GridLayout( 1,2 ) );

			entryP.add(left);
			entryP.add(right);

			add(entryP, BorderLayout.CENTER);
			add(del, BorderLayout.EAST);
		}

		private void del( Pair<String, Integer> entry ) {

			mapProxy.remove(entry);
			saveToMap();

			MapEditor.this.init();
			MapEditor.this.revalidate();
			MapEditor.this.repaint();
		}

	}
}
