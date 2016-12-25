package nl.topicus.plugins.maven.javassist;

import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;

public abstract class ClassTransformer {

    private List<String> processInclusions = new ArrayList<>();

    private List<String> processExclusions = new ArrayList<>();

    private List<String> exclusions = new ArrayList<>();

    private ILogger logger;

    public final ILogger getLogger() {
        return logger;
    }

    public final void setLogger(ILogger logger) {
        this.logger = logger;
    }

    public List<String> getProcessInclusions() {
        return processInclusions;
    }

    public void setProcessInclusions(List<String> processInclusions) {
        this.processInclusions = processInclusions;
    }

    public List<String> getProcessExclusions() {
        return processExclusions;
    }

    public void setProcessExclusions(List<String> processExclusions) {
        this.processExclusions = processExclusions;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }

    public abstract void applyTransformations(ClassPool classPool,
            CtClass classToTransform) throws TransformationException;

    public boolean processClassName(String className) {
        
        for (String exclusion : getProcessExclusions())
            if (className.startsWith(exclusion))
                return false;

        for (String inclusion : getProcessInclusions())
            if (className.startsWith(inclusion))
                return true;

        return false;
    }
}
