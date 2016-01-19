package au.org.ala.test;

import java.io.IOException;
import java.io.Reader;

public interface ScriptEvaluator {

    /**
     * Evaluate a script test and return a true / false result
     *
     * @param script the script body
     * @return true if the script passes
     */
    boolean evaluateScriptTest(Reader script, RealisedBody body) throws IOException;

}
