package nl.topicus.plugins.maven.javassist;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.sonatype.plexus.build.incremental.BuildContext;

import com.google.common.io.Files;

public class ClassNameJarIterator implements ClassFileIterator {
    private Iterator<String> classFiles = new ArrayList<String>().iterator();
    private File jarFile;

    public ClassNameJarIterator(final String classPath,
            final BuildContext buildContext) {

        jarFile = new File(classPath);
        if (buildContext.hasDelta(classPath)) {
            List<String> classNames = new ArrayList<>();
            try {
                JarInputStream jarFileStream = new JarInputStream(
                        new FileInputStream(jarFile));
                JarEntry jarEntry;

                while (true) {
                    jarEntry = jarFileStream.getNextJarEntry();
                    if (jarEntry == null)
                        break;

                    if (jarEntry.getName().endsWith(".class"))
                        classNames.add(jarEntry.getName()
                                .replaceAll("/", "\\."));

                }

                jarFileStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            classFiles = classNames.iterator();
        } else {
            classFiles = Collections.emptyIterator();
        }
    }

    @Override
    public boolean hasNext() {
        return classFiles.hasNext();
    }

    @Override
    public String next() {
        return Files.getNameWithoutExtension(classFiles.next()).replace(
                File.separator, ".");
    }

    @Override
    public File getLastFile() {
        return jarFile;
    }

    @Override
    public void remove() {
        classFiles.remove();
    }
}
