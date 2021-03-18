package ar.com.kfgodel.temas.application;

import ar.com.kfgodel.dependencies.api.DependencyInjector;
import ar.com.kfgodel.orm.api.HibernateOrm;
import ar.com.kfgodel.orm.api.config.DbCoordinates;
import ar.com.kfgodel.orm.impl.HibernateFacade;
import ar.com.kfgodel.temas.application.initializers.InicializadorDeDatos;
import ar.com.kfgodel.temas.config.TemasConfiguration;
import ar.com.kfgodel.temas.notifications.NotificadorDeTemasNoTratadosJob;
import ar.com.kfgodel.transformbyconvention.api.TypeTransformer;
import ar.com.kfgodel.transformbyconvention.impl.B2BTransformer;
import ar.com.kfgodel.transformbyconvention.impl.config.TransformerConfigurationByConvention;
import ar.com.kfgodel.webbyconvention.api.WebServer;
import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.impl.JettyWebServer;
import ar.com.kfgodel.webbyconvention.impl.config.ConfigurationByConvention;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This type represents the whole application as a single object.<br>
 * From this instance you can access the application components
 * Created by kfgodel on 22/03/15.
 */
public class TemasApplication implements Application {
    private static final TimeOfDay NOTIFICATION_TIME_OF_DAY = TimeOfDay.hourAndMinuteOfDay(9, 0);
    public static Logger LOG = LoggerFactory.getLogger(TemasApplication.class);

    private TemasConfiguration config;

    public static Application create(TemasConfiguration config) {
        TemasApplication application = new TemasApplication();
        application.setConfiguration(config);
        return application;
    }

    @Override
    public WebServer getWebServerModule() {
        return this.injector().getImplementationFor(WebServer.class).get();
    }

    @Override
    public HibernateOrm getOrmModule() {
        return this.injector().getImplementationFor(HibernateOrm.class).get();
    }

    @Override
    public TypeTransformer getTransformerModule() {
        return this.injector().getImplementationFor(TypeTransformer.class).get();
    }

    @Override
    public TemasConfiguration getConfiguration() {
        return config;
    }

    public void setConfiguration(TemasConfiguration aConfiguration) {
        config = aConfiguration;
    }

    @Override
    public DependencyInjector injector() {
        return config.getInjector();
    }

    /**
     * Initializes application components and starts the server to listen for requests
     */
    @Override
    public void start() {
        LOG.info("Starting APP");
        this.initialize();
        this.getWebServerModule().startAndJoin();
    }

    @Override
    public void stop() {
        LOG.info("Stopping APP");
        this.getWebServerModule().stop();
        stopOrmModule();
    }

    protected void initialize() {
        this.injector().bindTo(Application.class, this);

        bindOrmModule();
        // Web server depends on hibernate, so it needs to be created after hibernate
        this.injector().bindTo(WebServer.class, createWebServer());
        bindTransformer();

        registerCleanupHook();

        InicializadorDeDatos.create(this).inicializar();
    }

    private void registerCleanupHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Cleaning-up before shutdown");
            this.stop();
        }, "cleanup-thread"));
    }

    private TypeTransformer createTransformer() {
        TransformerConfigurationByConvention configuration = TransformerConfigurationByConvention.create(this.injector());
        return B2BTransformer.create(configuration);
    }

    private HibernateOrm createPersistenceLayer() {
        DbCoordinates dbCoordinates = config.getDatabaseCoordinates();
        LOG.info("Connecting to database url {}", dbCoordinates.getDbUrl());
        HibernateOrm hibernateOrm = HibernateFacade.createWithConventionsFor(dbCoordinates);
        return hibernateOrm;
    }

    private WebServer createWebServer() {
        int MAX_TIME_IN_SECONDS = 14400;

        WebServerConfiguration serverConfig = ConfigurationByConvention.create()
                .authenticatingWith(config.autenticador())
                .listeningHttpOn(config.getHttpPort())
                .expiringSessionsAfter(MAX_TIME_IN_SECONDS)
                .withInjections((binder) -> {
                    //Make application the only jetty injectable dependency
                    binder.bind(this).to(Application.class);
                })
                .withSecuredRootPaths("/api/v1")
                .redirectingAfterSuccessfulAuthenticationTo("/")
                .redirectingAfterFailedAuthenticationTo("/login?failed=true");
        return JettyWebServer.createFor(serverConfig);
    }

    private void bindTransformer() {
        this.injector().bindTo(TypeTransformer.class, createTransformer());
    }

    private void stopOrmModule() {
        this.getOrmModule().close();
    }

    private void bindOrmModule() {
        this.injector().bindTo(HibernateOrm.class, createPersistenceLayer());
    }

    public void restartOrmModule() {
        stopOrmModule();
        bindOrmModule();
        bindTransformer();
    }

    private void iniciarNotificadorDeTemasNoTratados() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("injector", injector());
        JobDetail job = JobBuilder.newJob(NotificadorDeTemasNoTratadosJob.class)
                .setJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(DailyTimeIntervalScheduleBuilder
                        .dailyTimeIntervalSchedule()
                        .onMondayThroughFriday()
                        .startingDailyAt(NOTIFICATION_TIME_OF_DAY)
                        .withRepeatCount(0)
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();

        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (SchedulerException exception) {
            LOG.error("Error al iniciar el notificador de temas no tratados.", exception);
        }
    }
}
