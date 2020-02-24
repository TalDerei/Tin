package edu.lehigh.cse216.teamtin;

class Datum {
    /**
     * An integer index for this piece of data
     */
    String mSubject;

    /**
     * The string contents that comprise this piece of data
     */
    String mText;

    /**
     * Construct a Datum by setting its index and text
     *
     * @param sub The index of this piece of data
     * @param txt The string contents for this piece of data
     */
    Datum(String sub, String txt) {
        mSubject = sub;
        mText = txt;
    }
}
