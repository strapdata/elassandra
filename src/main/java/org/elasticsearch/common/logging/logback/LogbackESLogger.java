/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.logging.logback;

import org.elasticsearch.common.logging.support.AbstractESLogger;
import org.slf4j.spi.LocationAwareLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Add support of Logback framework.
 * @author vroyer
 *
 */
public class LogbackESLogger extends AbstractESLogger {

    private final Logger logger;
    private final LocationAwareLogger lALogger;
    private final String FQCN = AbstractESLogger.class.getName();

    public LogbackESLogger(String prefix, Logger logger) {
        super(prefix);
        this.logger = logger;
        if (logger instanceof LocationAwareLogger) {
            lALogger = (LocationAwareLogger) logger;
        } else {
            lALogger = null;
        }
        //FQCN = MarkerFactory.getMarker(logger.getName());
    }

    public Logger logger() {
        return logger;
    }

    public void setLevel(String level) {
        if (level == null) {
            logger.setLevel(null);
        } else if ("error".equalsIgnoreCase(level)) {
            logger.setLevel(Level.ERROR);
        } else if ("warn".equalsIgnoreCase(level)) {
            logger.setLevel(Level.WARN);
        } else if ("info".equalsIgnoreCase(level)) {
            logger.setLevel(Level.INFO);
        } else if ("debug".equalsIgnoreCase(level)) {
            logger.setLevel(Level.DEBUG);
        } else if ("trace".equalsIgnoreCase(level)) {
            logger.setLevel(Level.TRACE);
        }
    }

    @Override
    public String getLevel() {
        if (logger.getLevel() == null) {
            return null;
        }
        return logger.getLevel().toString();
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    protected void internalTrace(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, null);
        } else {
            logger.trace(msg);
        }
    }

    @Override
    protected void internalTrace(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, null, cause);
        } else {
            logger.trace(msg);
        }
    }

    @Override
    protected void internalDebug(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
        } else {
            logger.debug(msg);
        }
    }

    @Override
    protected void internalDebug(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, cause);
        } else {
            logger.debug(msg, cause);
        }
    }

    @Override
    protected void internalInfo(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, null);
        } else {
            logger.info(msg);
        }
    }

    @Override
    protected void internalInfo(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, null, cause);
        } else {
            logger.info(msg, cause);
        }
    }

    @Override
    protected void internalWarn(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
        } else {
            logger.warn(msg);
        }
    }

    @Override
    protected void internalWarn(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, cause);
        } else {
            logger.warn(msg, cause);
        }
    }

    @Override
    protected void internalError(String msg) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, null);
        } else {
            logger.error(msg);
        }
    }

    @Override
    protected void internalError(String msg, Throwable cause) {
        if (lALogger != null) {
            lALogger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, null, cause);
        } else {
            logger.error(msg, cause);
        }
    }
}
