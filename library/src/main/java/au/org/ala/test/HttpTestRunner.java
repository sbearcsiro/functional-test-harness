package au.org.ala.test;

import com.typesafe.config.ConfigFactory;
import lombok.val;
import okhttp3.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import static au.org.ala.test.Util.notEmpty;
import static au.org.ala.test.Util.read;
import static okhttp3.MediaType.*;

public class HttpTestRunner {

    private final Resolver resolver;
    private final Settings settings;
    private final Call.Factory client;
    private final ScriptEvaluator scriptEvaluator;

    public HttpTestRunner(Resolver resolver) throws IOException {
        this(resolver, new OkHttpClient(), null);
    }

    public HttpTestRunner(Resolver resolver, Call.Factory client, ScriptEvaluator scriptEvaluator) throws IOException {
        this.client = client;
        this.resolver = resolver;
        this.scriptEvaluator = scriptEvaluator;
        this.settings = new Settings(ConfigFactory.parseReader(resolver.getConfigFileReader()).resolveWith(ConfigFactory.systemProperties().withFallback(ConfigFactory.systemEnvironment())));
    }

    public Set<HttpTest> getTests() {
        return settings.getHttpTests();
    }

    public RealisedBody run(HttpTest test) throws IOException {
        val request = buildRequest(test);
        val response =  client.newCall(request).execute();
        //if (!response.isSuccessful()) throw new AssertionError("Boo");
        return getRealisedBody(response);
    }


    public RealisedBody getRealisedBody(Response response) throws IOException {
        return new RealisedBody(response);
    }

    private Request buildRequest(HttpTest test) throws IOException {
        RequestBody body;
        if (notEmpty(test.entity)) {
            try (Reader reader = resolver.resolve(test.entity)) {
                body = RequestBody.create(parse(test.contentType), read(reader));
            }
        } else body = null;

        val builder = new Request.Builder().method(test.method, body).url(test.url);
        //if (test.contentType != null) builder.header("Content-Type", test.contentType);
        if (test.acceptHeader != null) builder.header("Accept", test.acceptHeader);

        return builder.build();
    }

    /**
     * Evaluate a script test
     *
     * @param script the path to the script
     * @return true if the script passes
     */
    public boolean evaluateScriptTest(String script, RealisedBody body) throws IOException {
        if (scriptEvaluator == null) throw new IllegalStateException("Can't evaluate script " + script + " because no script evaluator is installed");

        return scriptEvaluator.evaluateScriptTest(resolver.resolve(script), body);
    }


    public static class Builder {

        OkHttpClient client;
        Resolver resolver;
        ScriptEvaluator scriptEvaluator;

        private Builder(Resolver resolver) {
            this.resolver = resolver;
        }

        public static Builder withFileResolver(String configPath) {
            return new Builder(new FileResolver(configPath));
        }

        public static Builder withClasspathResolver(String configPath) {
            return new Builder(new ClasspathResolver(configPath));
        }

        public Builder client(OkHttpClient client) {
            this.client = client;
            return this;
        }

        public Builder scriptEvaluator(ScriptEvaluator scriptEvaluator) {
            this.scriptEvaluator = scriptEvaluator;
            return this;
        }

        public Builder groovyScriptEvaluator() {
            try {
                this.scriptEvaluator = new GroovyScriptEvaluator();
                return this;
            } catch (NoClassDefFoundError e) {
                throw new IllegalStateException("Groovy no present on classpath");
            }
        }

        public HttpTestRunner build() throws IOException {
            if (client == null) client = new OkHttpClient();
            return new HttpTestRunner(resolver, client, scriptEvaluator);
        }
    }
}
