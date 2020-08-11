package app.cash.backfila.service

import app.cash.backfila.client.BackfilaClientServiceClient
import app.cash.backfila.client.BackfilaClientServiceClientProvider
import app.cash.backfila.client.BackfilaDefaultEndpointConfigModule
import app.cash.backfila.client.ForConnectors
import app.cash.backfila.dashboard.ViewLogsUrlProvider
import app.cash.backfila.protos.clientservice.GetNextBatchRangeRequest
import app.cash.backfila.protos.clientservice.GetNextBatchRangeResponse
import app.cash.backfila.protos.clientservice.KeyRange
import app.cash.backfila.protos.clientservice.PrepareBackfillRequest
import app.cash.backfila.protos.clientservice.PrepareBackfillResponse
import app.cash.backfila.protos.clientservice.RunBatchRequest
import app.cash.backfila.protos.clientservice.RunBatchResponse
import app.cash.backfila.service.persistence.DbBackfillRun
import misk.MiskApplication
import misk.MiskCaller
import misk.MiskRealServiceModule
import misk.environment.Deployment
import misk.environment.DeploymentModule
import misk.environment.Env
import misk.environment.Environment
import misk.environment.EnvironmentModule
import misk.hibernate.Session
import misk.inject.KAbstractModule
import misk.jdbc.DataSourceClusterConfig
import misk.jdbc.DataSourceClustersConfig
import misk.jdbc.DataSourceConfig
import misk.jdbc.DataSourceType
import misk.security.authz.DevelopmentOnly
import misk.security.authz.FakeCallerAuthenticator
import misk.security.authz.MiskCallerAuthenticator
import misk.web.MiskWebModule
import misk.web.WebConfig
import misk.web.dashboard.AdminDashboardModule
import okio.ByteString.Companion.encodeUtf8

fun main(args: Array<String>) {
  val environment = Environment.DEVELOPMENT
  val env = Env(environment.toString())
  val deployment = Deployment(name = "backfila", isLocalDevelopment = true)

  MiskApplication(
      object : KAbstractModule() {
        override fun configure() {
          val webConfig = WebConfig(
              port = 8080,
              idle_timeout = 500000,
              host = "127.0.0.1"
          )
          install(MiskWebModule(webConfig))
          multibind<MiskCallerAuthenticator>().to<FakeCallerAuthenticator>()
          bind<MiskCaller>().annotatedWith<DevelopmentOnly>()
              .toInstance(MiskCaller(user = "testfila"))
          bind<ViewLogsUrlProvider>().to<DevelopmentViewLogsUrlProvider>()

          newMapBinder<String, BackfilaClientServiceClientProvider>(ForConnectors::class)
              .permitDuplicates().addBinding("DEV")
              .toInstance(object : BackfilaClientServiceClientProvider {
                override fun validateExtraData(connectorExtraData: String?) {
                }

                override fun clientFor(serviceName: String, connectorExtraData: String?): BackfilaClientServiceClient {
                  return object : BackfilaClientServiceClient {
                    override fun prepareBackfill(request: PrepareBackfillRequest): PrepareBackfillResponse {
                      return PrepareBackfillResponse(listOf(
                          PrepareBackfillResponse.Partition(
                              "-80", KeyRange("0".encodeUtf8(), "1000".encodeUtf8()), null
                          ),
                          PrepareBackfillResponse.Partition(
                              "80-", KeyRange("0".encodeUtf8(), "1000".encodeUtf8()), null
                          )
                      ), mapOf())
                    }

                    override suspend fun getNextBatchRange(request: GetNextBatchRangeRequest): GetNextBatchRangeResponse {
                      TODO("Not yet implemented")
                    }

                    override suspend fun runBatch(request: RunBatchRequest): RunBatchResponse {
                      TODO("Not yet implemented")
                    }
                  }
                }
              })
        }
      },
      EnvironmentModule(environment = environment),
      DeploymentModule(deployment, env),
      BackfilaServiceModule(
          deployment,
          BackfilaConfig(
              backfill_runner_threads = null,
              data_source_clusters = DataSourceClustersConfig(
                  mapOf("backfila-001" to DataSourceClusterConfig(
                      writer = DataSourceConfig(
                          type = DataSourceType.MYSQL,
                          database = "backfila_development",
                          username = "root",
                          migrations_resource = "classpath:/migrations"
                      ),
                      reader = null
                  ))
              ),
              web_url_root = "http://localhost:8080/app/",
              slack = null
          )
      ),
      AdminDashboardModule(isDevelopment = true),
      BackfilaDefaultEndpointConfigModule(),
      MiskRealServiceModule()
  ).run(args)
}

class DevelopmentViewLogsUrlProvider : ViewLogsUrlProvider {
  override fun getUrl(session: Session, backfillRun: DbBackfillRun): String {
    return "/"
  }
}
