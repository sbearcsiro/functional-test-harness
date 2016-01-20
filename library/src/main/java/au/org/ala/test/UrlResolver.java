package au.org.ala.test;

import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static au.org.ala.test.Util.UTF8;

public class UrlResolver implements Resolver {

    private final URL url;

    public UrlResolver(URL url) {
        this.url = url;
    }

    public static UrlResolver forClasspath(String path) {
        val url = UrlResolver.class.getClassLoader().getResource(path);
        if (url == null) throw new IllegalArgumentException(path + " is not a valid class path resource");
        return new UrlResolver(url);
    }

    public static UrlResolver forUrl(String urlString) {
        try {
            val url = new URL(urlString);
            return new UrlResolver(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(urlString + " is not a valid URL", e);
        }
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
