package nl.topicus.plugins.maven.javassist;

import java.io.File;
import java.util.Iterator;

public interface ClassFileIterator extends Iterator<String> {
    public File getLastFile();
}
