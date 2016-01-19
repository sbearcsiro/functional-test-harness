package au.org.ala.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Value;
import lombok.val;

import java.util.regex.Pattern;

@Value
public class PathTest {

    private static final Config defaults = ConfigFactory.load("reference-pathtest.conf");

    public final String path;
    public final String testExpression;
    public final boolean optional;

    public Pattern getPattern() {
        return Pattern.compile(testExpression);
    }

    public static PathTest fromConfig(Config config) {
        val conf2 = config.withFallback(defaults);
        return new PathTest(conf2.getString("path"), conf2.getString("testExpression"), conf2.getBoolean("optional"));
    }
}
