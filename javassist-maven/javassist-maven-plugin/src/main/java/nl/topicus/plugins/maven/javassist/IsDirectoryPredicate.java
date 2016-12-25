package nl.topicus.plugins.maven.javassist;

import java.io.File;

import com.google.common.base.Predicate;

public class IsDirectoryPredicate implements Predicate<String> {

    public static final IsDirectoryPredicate INSTANCE = new IsDirectoryPredicate();

    @Override
    public boolean apply(String resource) {
        try {
            File file = new File(resource);
            if (file.isDirectory()) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }
}
