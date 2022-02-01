package org.twak.leeds_grader;

import java.util.Date;

public class CW1Assignment extends Assignment {
    public CW1Assignment() {
        super();
    }

    @Override public Date dueDate() {

        return new Date(2021-1900, 10-1, 29, 12, 00, 0);
    }

    @Override
    public double getMaxScore() {
        return 100; // for late processing
    }
}
