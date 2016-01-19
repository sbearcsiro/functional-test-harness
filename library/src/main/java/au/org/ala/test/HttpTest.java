package au.org.ala.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Value;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@Value
public class HttpTest {

    public final String name;
    public final String method;
    public final String url;
    public final String acceptHeader;
    public final String entity;
    public final String contentType;
    public final List<PathTest> pathTests;
    public final String script;

    static HttpTest fromConfig(String name, Config config) {
        val pathTests = new ArrayList<PathTest>();
        //val pathTestDefault = ConfigFactory.
        for (val pathTestObj : config.getConfigList("pathTests")) {
            pathTests.add(PathTest.fromConfig(pathTestObj));
        }

        return new HttpTest(name, config.getString("method"), config.getString("url"), config.getString("acceptHeader"),
                config.getString("entity"), config.getString("contentType"), pathTests, config.getString("script"));
    }
}
