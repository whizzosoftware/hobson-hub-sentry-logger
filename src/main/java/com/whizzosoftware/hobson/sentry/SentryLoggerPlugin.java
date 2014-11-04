package com.whizzosoftware.hobson.sentry;

import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.bootstrap.api.config.ConfigurationMetaData;
import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.bootstrap.api.util.LogUtil;
import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.logback.SentryAppender;
import org.slf4j.Logger;

import java.util.Dictionary;

/**
 * A Hobson plugin that logs error messages to the Sentry logger service.
 *
 * @author Dan Noguerol
 */
public class SentryLoggerPlugin extends AbstractHobsonPlugin {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(SentryLoggerPlugin.class);

    protected static final String PROP_DSN = "dsn";

    private String dsn;
    private SentryAppender appender;

    public SentryLoggerPlugin(String pluginId) {
        super(pluginId);
    }

    @Override
    public void onStartup(Dictionary config) {
        logger.info("Starting Sentry logger service");

        addConfigurationMetaData(new ConfigurationMetaData(PROP_DSN, "Sentry DSN", "Your Sentry DSN (e.g. https://cc4234234ba41242342cd:c135588a34324d43244g@app.getsentry.com/12345", ConfigurationMetaData.Type.STRING));

        // not sure why this is needed but Raven doesn't work properly without it
        RavenFactory.registerFactory(new DefaultRavenFactory());

        // create the appender
        createAppender(config);
    }

    @Override
    public void onShutdown() {
        LogUtil.removeLogAppender(appender);
    }

    @Override
    public long getRefreshInterval() {
        return 0;
    }

    @Override
    public void onRefresh() {}

    @Override
    public void onPluginConfigurationUpdate(Dictionary config) {
        createAppender(config);
    }

    @Override
    public String getName() {
        return "Sentry Logging Plugin";
    }

    protected void createAppender(Dictionary config) {
        String newDsn = (String)config.get(PROP_DSN);

        if (newDsn != null && (dsn == null || !newDsn.equals(dsn))) {
            if (appender != null) {
                LogUtil.removeLogAppender(appender);
            }

            dsn = newDsn;

            logger.info("Creating Sentry log appender");
            appender = new SentryAppender();
            appender.setName("Sentry");
            appender.setDsn(dsn);
            LogUtil.addErrorLogAppender(appender);

            setStatus(new PluginStatus(PluginStatus.Status.RUNNING));
        }

        if (appender == null) {
            setStatus(new PluginStatus(PluginStatus.Status.NOT_CONFIGURED, "Sentry DSN not set in plugin configuration"));
        }
    }
}
