package app.cash.backfila.client

import app.cash.backfila.client.Connectors.HTTP
import app.cash.backfila.service.HttpClientNetworkInterceptor
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import misk.client.HttpClientConfigUrlProvider
import misk.client.HttpClientEndpointConfig
import misk.client.HttpClientFactory
import misk.moshi.adapter
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.adapter.guava.GuavaCallAdapterFactory
import retrofit2.converter.wire.WireConverterFactory

@Singleton
class HttpClientServiceClientProvider @Inject constructor(
  @Named(HTTP) private val defaultHttpClientEndpointConfig: HttpClientEndpointConfig,
  private val moshi: Moshi
) : BackfilaClientServiceClientProvider {
  override fun validateExtraData(connectorExtraData: String?) {
    checkNotNull(connectorExtraData, { "Extra data required for HTTP connector" })
    val fromJson = adapter().fromJson(connectorExtraData)
    checkNotNull(fromJson, { "Failed to parse HTTP connector extra data JSON" })
    checkNotNull(fromJson.url, { "HTTP connector extra data must contain a URL" })
  }

  override fun clientFor(
    serviceName: String,
    connectorExtraData: String?
  ): BackfilaClientServiceClient {
    val url = adapter().fromJson(connectorExtraData!!)!!.url
    /*
    url = getUrl()
    val httpClientEndpointConfig = httpClientsConfig[url]
    val okHttpClient = okHttpClient(httpClientEndpointConfig)
    val baseUrl = url

     */
    val httpClientEndpointConfig = defaultHttpClientEndpointConfig.copy(
        url = url
    )
    val okHttpClient = httpClientFactory.create(httpClientEndpointConfig)
        .newBuilder()
        .apply {
          networkInterceptors.forEach {
            addNetworkInterceptor(it)
          }
        }
        .build()
    val baseUrl = httpClientConfigUrlProvider.getUrl(httpClientEndpointConfig)
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(WireConverterFactory.create())
        .addCallAdapterFactory(GuavaCallAdapterFactory.create())
        .build()
    val api = retrofit.create(HttpClientServiceApi::class.java)
    return HttpClientServiceClient(api)
  }

  private fun adapter() = moshi.adapter<HttpConnectorData>()
}
