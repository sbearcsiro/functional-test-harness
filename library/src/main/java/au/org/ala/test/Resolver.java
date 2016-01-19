package au.org.ala.test;

import java.io.IOException;
import java.io.Reader;

public interface Resolver {

    Reader getConfigFileReader() throws IOException;

    Reader resolve(String path) throws IOException;

}
