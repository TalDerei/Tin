package edu.lehigh.cse216.tad222.backend;

/**
 * This class provides some information on top of what DataRow gives
 */
public class CombinedDataRow {
    /**
         * The ID of this row of the database
         */
        int mId;
        /**
         * The subject stored in this row
         */
        String mSubject;
        /**
         * The message stored in this row
         */
        String mMessage;

        String mUser_id;

        int mLikes;

        String mNickname;

        /**
         * Construct a CombinedDataRow object by providing values for its fields
         */
        public CombinedDataRow(int id, String subject, String message, String uid, int likes, String nickname) {
            mId = id;
            mSubject = subject;
            mMessage = message;
            mUser_id = uid;
            mLikes = likes;
            mNickname = nickname;
        }

        public CombinedDataRow(Database.RowData data, int likes, String nickname) {
            mId = data.mId;
            mSubject = data.mSubject;
            mMessage = data.mMessage;
            mUser_id = data.mUser_id;
            mLikes = likes;
            mNickname = nickname;
        }
}		