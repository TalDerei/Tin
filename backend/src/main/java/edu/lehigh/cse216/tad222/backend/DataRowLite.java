package edu.lehigh.cse216.tad222.backend;

/**
 * DataRowLite is for communicating back a subset of the information in a
 * DataRow.  Specifically, we only send back the id and title.  Note that
 * in order to keep the client code as consistent as possible, we ensure 
 * that the field names in DataRowLite match the corresponding names in
 * DataRow.  As with DataRow, we plan to convert DataRowLite objects to
 * JSON, so we need to make their fields public.
 */
public class DataRowLite {
    /**
     * The id for this row; see DataRow.mId
     */
    public int mId;

    /**
     * The title string for this row of data; see DataRow.mTitle
     */
    public String mTitle;

    /**
     * Create a DataRowLite by copying fields from a DataRow
     */
    public DataRowLite(DataRow data) {
        this.mId = data.mId;
        this.mTitle = data.mTitle;
    }
}