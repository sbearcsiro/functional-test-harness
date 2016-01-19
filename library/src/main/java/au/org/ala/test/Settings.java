package au.org.ala.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.val;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Settings {

    private final Config config;
    private final Set<HttpTest> httpTests;

    private static final Config defaults = ConfigFactory.load("reference-test.conf");

    public Settings(Config config) {
        //config.checkValid(ConfigFactory.defaultReference());
        this.config = config;
        this.httpTests = Collections.unmodifiableSet(readTests(config));
//        validateTests();
    }

    public Config asConfig() {
        return this.config;
    }

//    private void validateTests() {
//        for (val test : httpTests) {
//            test.validate();
//        }
//    }

    private static Set<HttpTest> readTests(Config config) {
        val testsObj = config.getObject("tests");
        val testsConfig = testsObj.toConfig();
        val tests = new HashSet<HttpTest>();
        for (val name : testsObj.keySet()) {
            tests.add(HttpTest.fromConfig(name, testsConfig.getConfig(name).withFallback(defaults)));
        }
        return tests;
    }
}
