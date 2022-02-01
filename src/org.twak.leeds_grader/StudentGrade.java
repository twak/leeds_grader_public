package org.twak.leeds_grader;

import java.util.HashMap;
import java.util.Map;

public class StudentGrade extends HashMap<String, org.twak.leeds_grader.StudentGrade.Response>  {

	public Map<String, Integer> contributionsClaimed = new HashMap<>();

	public StudentGrade( Assignment cw1 ) {
		for (GradeItem gi : cw1)
			put (gi.name, new Response( gi.def, "") );
	}

	static class Response {
		double mark;
		String comment;

		public Response() {
			mark = 0;
			comment = "";
		}

		public Response( double mark, String comment ) {
			this.mark = mark;
			this.comment = comment;
		}
	}

	public double score() {
		return Math.max( 0, this.values().stream().mapToDouble( x -> x.mark).sum() );
	}

	@Override public Response get( Object _key ) {

		String key = (String)_key;

		Response out = super.get( key );
		if (out == null)
			put (key, new Response());

		return super.get( key );
	}
}
