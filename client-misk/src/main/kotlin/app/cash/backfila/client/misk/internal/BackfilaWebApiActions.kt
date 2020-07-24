package app.cash.backfila.client.misk.internal

import app.cash.backfila.protos.clientservice.GetNextBatchRangeRequest
import app.cash.backfila.protos.clientservice.GetNextBatchRangeResponse
import app.cash.backfila.protos.clientservice.PrepareBackfillRequest
import app.cash.backfila.protos.clientservice.PrepareBackfillResponse
import app.cash.backfila.protos.clientservice.RunBatchRequest
import app.cash.backfila.protos.clientservice.RunBatchResponse
import javax.inject.Inject
import misk.logging.getLogger
import misk.security.authz.Authenticated
import misk.web.Post
import misk.web.ConcurrencyLimitsOptOut
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes

internal class PrepareBackfillAction @Inject constructor(
  private val operatorFactory: BackfillOperator.Factory
) : WebAction {
  @Post("/backfila/prepare-backfill")
  @ConcurrencyLimitsOptOut // TODO: Remove after 2020-08-01 (or use @AvailableWhenDegraded).
  @RequestContentType(MediaTypes.APPLICATION_PROTOBUF)
  @ResponseContentType(MediaTypes.APPLICATION_PROTOBUF)
  @Authenticated(services = ["backfila"])
  fun prepareBackfill(@RequestBody request: PrepareBackfillRequest): PrepareBackfillResponse {
    logger.info { "Preparing backfill `${request.backfill_name}::${request.backfill_id}`" }

    val operator = operatorFactory.create(request.backfill_name, request.backfill_id)
    return operator.prepareBackfill(request)
  }

  companion object {
    val logger = getLogger<PrepareBackfillAction>()
  }
}

internal class GetNextBatchRangeAction @Inject constructor(
  private val operatorFactory: BackfillOperator.Factory
) : WebAction {
  @Post("/backfila/get-next-batch-range")
  @ConcurrencyLimitsOptOut // TODO: Remove after 2020-08-01 (or use @AvailableWhenDegraded).
  @RequestContentType(MediaTypes.APPLICATION_PROTOBUF)
  @ResponseContentType(MediaTypes.APPLICATION_PROTOBUF)
  @Authenticated(services = ["backfila"])
  fun getNextBatchRange(@RequestBody request: GetNextBatchRangeRequest): GetNextBatchRangeResponse {
    logger.info {
      "Computing batch for backfill `${request.backfill_name}::${request.partition_name}" +
          "::${request.backfill_id}`. Previous end: `${request.previous_end_key}`"
    }

    val operator = operatorFactory.create(request.backfill_name, request.backfill_id)

    return operator.getNextBatchRange(request)
        .also {
          logger.info {
            "Next batches computed for backfill " +
                "`${request.backfill_name}::${request.partition_name}::${request.backfill_id}`. " +
                "${it.batches}"
          }
        }
  }

  companion object {
    val logger = getLogger<GetNextBatchRangeAction>()
  }
}

internal class RunBatchAction @Inject constructor(
  private val operatorFactory: BackfillOperator.Factory
) : WebAction {
  @Post("/backfila/run-batch")
  @ConcurrencyLimitsOptOut // TODO: Remove after 2020-08-01 (or use @AvailableWhenDegraded).
  @RequestContentType(MediaTypes.APPLICATION_PROTOBUF)
  @ResponseContentType(MediaTypes.APPLICATION_PROTOBUF)
  @Authenticated(services = ["backfila"])
  fun runBatch(@RequestBody request: RunBatchRequest): RunBatchResponse {
    logger.info {
      "Running backfila batch " +
          "`${request.backfill_name}::${request.partition_name}::${request.backfill_id}`: " +
          "[${request.batch_range.start.utf8()}, ${request.batch_range.end.utf8()}]"
    }

    val operator = operatorFactory.create(request.backfill_name, request.backfill_id)
    return operator.runBatch(request)
  }

  companion object {
    val logger = getLogger<RunBatchAction>()
  }
}
