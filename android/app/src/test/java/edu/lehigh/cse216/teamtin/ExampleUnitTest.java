package edu.lehigh.cse216.teamtin;

import android.os.Environment;

import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void Datum_constructor_sets_fields() throws Exception {
        Datum d = new Datum("7", "hello world");
        assertEquals(d.mSubject, "7");
        assertEquals(d.mText, "hello world");
    }

    @Test
    public void PictureData_constructor_and_methods_for_null() throws Exception {
        File dir = new File("/storage/emulated/0/TheBuzz");
        PictureData pd = new PictureData(dir);
        assertEquals(false, pd.mPic.isDirectory());
        assertEquals(null, pd.mBitmap);
        assertEquals(null, pd.asBitmap());
        if (dir.list() != null && dir.list().length > 0) {
            File pic = new File(dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return dir.getAbsolutePath().contains(".jpg");
                }
            })[0]);
            pd = new PictureData(pic);
            assertEquals(pic, pd.mPic);
            assertEquals(pd.mBitmap, pd.asBitmap());

        }
    }
}

