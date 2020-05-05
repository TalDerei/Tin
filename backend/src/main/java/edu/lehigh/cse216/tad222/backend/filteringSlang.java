package edu.lehigh.cse216.tad222.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class filteringSlang {
    protected String mPatterns;
    public boolean isSlang = false;

    public filteringSlang(String patterns) {
        mPatterns = patterns;
    }

    public String filterText(String sText) {
        Pattern p = Pattern.compile(mPatterns, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sText);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            System.out.println("bad word!");
            isSlang = true;
            System.out.println(m.group());
            m.appendReplacement(buf, maskWord(m.group()));
        }
        m.appendTail(buf);

        return buf.toString();
    }

    public static String maskWord(String word) {
        StringBuffer buf = new StringBuffer();
        char[] ch = word.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            if (i < 1) {
                buf.append(ch[i]);
            } else {
                buf.append("*");
            }
        }
        return buf.toString();
    }
}
