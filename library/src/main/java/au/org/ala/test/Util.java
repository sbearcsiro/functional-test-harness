package au.org.ala.test;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

class Util {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String JSON_BASED = "+json";
    public static final String XML_BASED = "+xml";

    static final Charset UTF8 = Charset.forName("UTF-8");

    static String read(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        CharBuffer buf = CharBuffer.allocate(0x800);
        while (reader.read(buf) != -1) {
            buf.flip();
            sb.append(buf);
            buf.clear();
        }
        return sb.toString();
    }

    static boolean notEmpty(String s) {
        return s != null && !"".equals(s.trim());
    }
}
