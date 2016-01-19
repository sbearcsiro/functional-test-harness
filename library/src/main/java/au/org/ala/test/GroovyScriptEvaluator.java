package au.org.ala.test;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.Reader;

public class GroovyScriptEvaluator implements ScriptEvaluator {
    @Override
    public boolean evaluateScriptTest(Reader script, RealisedBody body) throws IOException {
        // call groovy expressions from Java code
        Binding binding = new Binding();
        binding.setVariable("response", body);
        GroovyShell shell = new GroovyShell(binding);

        return (boolean) shell.evaluate(script);
    }
}
