package edu.lehigh.cse216.teamtin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class PictureData {
    String mPath;
    File mPic;
    Bitmap mBitmap;

    PictureData(File pic) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        mPath = pic.getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(mPath, options);
        if (options.outWidth != -1 && options.outHeight != -1) {
            mPic = pic;
            mBitmap = bitmap;
        } else {
            mPic = null;
            mBitmap = null;
        }
    }

    Bitmap asBitmap() {
        return mBitmap;
    }
}
