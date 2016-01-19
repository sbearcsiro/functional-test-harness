package au.org.ala.test;

import lombok.val;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

public class FileResolver implements Resolver {

    private final File file;

    public FileResolver(String path) {
        this.file = new File(path);
    }

    @Override
    public Reader getConfigFileReader() throws FileNotFoundException {
        return new FileReader(file);
    }

    @Override
    public Reader resolve(String path) throws FileNotFoundException {
        val segment = Paths.get(path);
        final File file2;
        if (segment.isAbsolute()) {
            file2 = segment.toFile();
        } else {
            val parent = file.getAbsoluteFile().getParentFile();
            val parentPath = parent.toPath();
            val resolvedPath = parentPath.resolve(segment);
            file2 = resolvedPath.toFile();
        }
        return new FileReader(file2);
    }
}
