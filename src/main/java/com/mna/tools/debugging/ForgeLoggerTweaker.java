/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.core.Filter
 *  org.apache.logging.log4j.core.Filter$Result
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.config.LoggerConfig
 *  org.apache.logging.log4j.core.filter.AbstractFilter
 */
package com.mna.tools.debugging;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class ForgeLoggerTweaker {
    private static MBELogFilter mbeLogFilter = new MBELogFilter();

    public static void applyLoggerFilter() {
        ArrayList<LoggerConfig> foundLog4JLoggers = new ArrayList<LoggerConfig>();
        LoggerContext logContext = (LoggerContext)LogManager.getContext((boolean)false);
        Map map = logContext.getConfiguration().getLoggers();
        for (LoggerConfig logger : map.values()) {
            if (foundLog4JLoggers.contains(logger)) continue;
            logger.addFilter((org.apache.logging.log4j.core.Filter)mbeLogFilter);
            foundLog4JLoggers.add(logger);
        }
    }

    public static void setMinimumLevel(Level minimumLevel) {
        ForgeLoggerTweaker.mbeLogFilter.minimumLevel = minimumLevel;
    }

    public static class MBELogFilter
    extends AbstractFilter
    implements Filter {
        private Level minimumLevel = Level.DEBUG;

        @Override
        public boolean isLoggable(LogRecord record) {
            return true;
        }

        public Filter.Result filter(LogEvent event) {
            if (this.minimumLevel.compareTo(event.getLevel()) < 0) {
                return Filter.Result.DENY;
            }
            return Filter.Result.NEUTRAL;
        }
    }
}

