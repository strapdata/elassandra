/*
 * Copyright (c) 2016 Vincent Royer (vroyer@vroyer.org).
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elassandra.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import nl.topicus.plugins.maven.javassist.ClassTransformer;
import nl.topicus.plugins.maven.javassist.ILogger;
import nl.topicus.plugins.maven.javassist.TransformationException;

/**
 * See https://issues.apache.org/jira/browse/CASSANDRA-9626.
 * Modify Cassandra bytes code to be Locale independent.
 * @author vroyer
 *
 */
public class StringLocaleTransformer extends ClassTransformer {

    public void applyTransformations(ClassPool pool, CtClass classToTransform) throws TransformationException {
        try {
            final CtMethod[] targetMethods = classToTransform.getDeclaredMethods();
            for (int i = 0; i < targetMethods.length; i++) {
                targetMethods[i].instrument(new ExprEditor() {
                    public void edit(final MethodCall m) throws CannotCompileException {
                        ILogger logger = getLogger();
                        if ("java.lang.String".equals(m.getClassName())) {
                           if ("format".equals(m.getMethodName()) && m.getSignature().startsWith("(Ljava/lang/String;")) {
                               logger.info("Modifing format() @ "+m.getFileName()+":"+m.getLineNumber());
                               m.replace("{$_ = java.lang.String.format(java.util.Locale.ROOT, $$);}");
                           } else if ("toUpperCase".equals(m.getMethodName()) && m.getSignature().startsWith("()")) {
                               logger.info("Modifing toUpperCase() @ "+m.getFileName()+":"+m.getLineNumber());
                               m.replace("{$_ = $proceed(java.util.Locale.ROOT);}");
                           } else if ("toLowerCase".equals(m.getMethodName()) && m.getSignature().startsWith("()")) {
                               logger.info("Modifing toLowerCase() @ "+m.getFileName()+":"+m.getLineNumber());
                               m.replace("{$_ = $proceed(java.util.Locale.ROOT);}");
                           }
                        }
                    }
                });
            }
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
