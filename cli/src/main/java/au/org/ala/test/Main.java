package au.org.ala.test;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import lombok.val;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.List;

import static java.lang.System.out;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class Main {

    @Parameter(description = "Configuration file location", required = true, arity = 1)
    List<String> filenames = Lists.newArrayList("application.conf");

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        Main main = new Main();
        new JCommander(main, args);
        main.run();
    }

    public void run() throws IOException {
        val filename = filenames.get(0);
        val runner = HttpTestRunner.Builder.withFileResolver(filename).groovyScriptEvaluator().build();
        val tests = runner.getTests();
        int count = 0;
        int failedTests = 0;
        for (val test: tests) {
            count++;
            out.println(ansi().a("Running ").fgBright(BLUE).a(test.name).reset().a(" (").fgBright(DEFAULT).a(count + "/" + tests.size()).fg(DEFAULT).reset().a(")"));
            out.print("HTTP Request was successful... ");
            val response = runner.run(test);

            val success = response.isSuccessful();
            if (success) out.println(ansi().fg(GREEN).a("✓").reset());
            else {
                out.println(ansi().fg(RED).a("✗").reset());
                failedTests++;
                continue;
            }

            for (val pathTest : test.pathTests) {
                out.print("Evaluating path " + pathTest.path + " against " + pathTest.testExpression + (pathTest.optional ? "(optional)" : "") + "... ");
                val passed = response.evaluatePath(pathTest);
                if (passed) out.println(ansi().fg(GREEN).a("✓").reset());
                else {
                    out.println(ansi().fg(RED).a("✗").reset());
                    failedTests++;
                }
            }

            if (!isBlank(test.script)) {
                out.print("Evaluating script: " + test.script+ "... ");
                val passed = runner.evaluateScriptTest(test.script, response);
                if (passed) out.println(ansi().fg(GREEN).a("✓").reset());
                else {
                    out.println(ansi().fg(RED).a("✗").reset());
                    failedTests++;
                }
            }
            out.println();
        }

        System.exit(failedTests);
    }

    static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

}
