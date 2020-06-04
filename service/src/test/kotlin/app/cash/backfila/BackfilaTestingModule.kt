package app.cash.backfila

import app.cash.backfila.api.ServiceWebActionsModule
import app.cash.backfila.client.BackfilaClientServiceClientProvider
import app.cash.backfila.client.ConnectorProvider
import app.cash.backfila.client.Connectors
import app.cash.backfila.client.FakeBackfilaClientServiceClientProvider
import app.cash.backfila.service.BackfilaConfig
import app.cash.backfila.service.BackfilaDb
import app.cash.backfila.service.BackfilaPersistenceModule
import app.cash.backfila.service.ForBackfilaScheduler
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.inject.Provides
import java.util.concurrent.Executors
import javax.inject.Singleton
import misk.MiskCaller
import misk.MiskTestingServiceModule
import misk.environment.Environment
import misk.environment.EnvironmentModule
import misk.hibernate.HibernateTestingModule
import misk.inject.KAbstractModule
import misk.jdbc.DataSourceClusterConfig
import misk.jdbc.DataSourceClustersConfig
import misk.jdbc.DataSourceConfig
import misk.jdbc.DataSourceType
import misk.logging.LogCollectorModule
import misk.scope.ActionScopedProviderModule

internal class BackfilaTestingModule : KAbstractModule() {
  override fun configure() {
    val config = BackfilaConfig(
        backfill_runner_threads = null,
        data_source_clusters = DataSourceClustersConfig(
            mapOf("backfila-001" to DataSourceClusterConfig(
                writer = DataSourceConfig(
                    type = DataSourceType.MYSQL,
                    database = "backfila_test",
                    username = "root",
                    migrations_resource = "classpath:/migrations"
                ),
                reader = null
            ))
        ),
        web_url_root = "",
        slack = null
    )
    bind<BackfilaConfig>().toInstance(config)
    install(EnvironmentModule(Environment.TESTING))
    install(LogCollectorModule())
    install(MiskTestingServiceModule())

    install(HibernateTestingModule(BackfilaDb::class))
    install(BackfilaPersistenceModule(config))

    install(ServiceWebActionsModule())

    bind(BackfilaClientServiceClientProvider::class.java)
        .to(FakeBackfilaClientServiceClientProvider::class.java)

    install(object : ActionScopedProviderModule() {
      override fun configureProviders() {
        bindSeedData(MiskCaller::class)
      }
    })
  }

  @Provides @Singleton
  fun connectorProvider(
    testClientProvider: FakeBackfilaClientServiceClientProvider
  ) = ConnectorProvider(
      Connectors.HTTP to testClientProvider,
      Connectors.ENVOY to testClientProvider
  )

  @Provides @ForBackfilaScheduler @Singleton
  fun backfillRunnerExecutor(): ListeningExecutorService {
    // TODO better executor for testing
    return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(ThreadFactoryBuilder()
        .setNameFormat("backfila-runner-%d")
        .build()))
  }
}
