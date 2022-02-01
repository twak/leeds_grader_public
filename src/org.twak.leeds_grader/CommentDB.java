package org.twak.leeds_grader;

import org.twak.utils.collections.MultiMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CommentDB extends MultiMap<String,String> {

	static String COMMENT_DB="comment_db.xml";

	public static CommentDB create(File loc) {

		File dbFile = new File (loc, COMMENT_DB);
		CommentDB out = new CommentDB(dbFile );
		try {

			if (dbFile.exists()) {
				out = (CommentDB) Grader.xStream.fromXML( dbFile );
				out.loc = dbFile;

				CommentDB noDupes = new CommentDB( out.loc );
				for (String key: out.keySet())
					for (String val : out.get( key ))
						noDupes.insert( key, val );

				out = noDupes;
			}
		}
		catch (Throwable th) { th.printStackTrace(); }

		return out;
	}

	File loc;
	public CommentDB(File location) {
		this.loc = location;
	}

	public void save() {
		try {
			Grader.xStream.toXML( this, new FileOutputStream( loc ) );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}

	public void insert( String name, String text ) {

		if (text == null)
			return;

		text = text.trim();

		if (text.isBlank())
			return;

		if (!contains( name, text ))
			put (name, text);

		save();
	}
}
