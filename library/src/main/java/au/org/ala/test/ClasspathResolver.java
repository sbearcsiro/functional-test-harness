package au.org.ala.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

import static au.org.ala.test.Util.UTF8;

public class ClasspathResolver implements Resolver {

    private final URL url;

    public ClasspathResolver(String path) {
        url = ClasspathResolver.class.getClassLoader().getResource(path);
        if (url == null) throw new IllegalArgumentException(path + " is not a valid class path resource");
    }


    @Override
    public Reader getConfigFileReader() throws IOException {
        return urlToReader(url);
    }

    @Override
    public Reader resolve(String path) throws IOException {
        try {
            return urlToReader(url.toURI().resolve(path).toURL());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Reader urlToReader(URL url) throws IOException {
        return new InputStreamReader(url.openStream(), UTF8);
    }
}
