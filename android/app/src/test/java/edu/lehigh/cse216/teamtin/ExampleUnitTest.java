package edu.lehigh.cse216.teamtin;

import org.junit.Test;

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

}

