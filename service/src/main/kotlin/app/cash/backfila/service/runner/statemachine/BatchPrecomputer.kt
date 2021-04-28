package app.cash.backfila.service.runner.statemachine

import app.cash.backfila.protos.clientservice.GetNextBatchRangeRequest
import app.cash.backfila.protos.clientservice.KeyRange
import app.cash.backfila.service.persistence.DbEventLog
import app.cash.backfila.service.runner.BackfillRunner
import com.google.common.base.Stopwatch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import misk.hibernate.load
import wisp.logging.getLogger

class BatchPrecomputer(
  private val backfillRunner: BackfillRunner
) {
  fun run(coroutineScope: CoroutineScope) = coroutineScope.launch {
    logger.info { "BatchPrecomputer started ${backfillRunner.logLabel()}" }

    // Start at the cursor we have in the DB, but after that we need to maintain our own,
    // since the DB stores how far we've completed batches, and we are likely ahead of that.
    var pkeyCursor = backfillRunner.metadata.precomputingPkeyCursor

    val stopwatch = Stopwatch.createUnstarted()

    while (true) {
      // Use the latest metadata snapshot.
      val metadata = backfillRunner.metadata

      if (metadata.precomputingDone) {
        break
      }

      if (backfillRunner.globalBackoff.backingOff()) {
        val backoffMs = backfillRunner.globalBackoff.backoffMs()
        logger.info { "BatchPrecomputer ${backfillRunner.logLabel()} backing off for $backoffMs" }
        delay(backoffMs)
      }

      try {
        val computeTimeLimitMs = 5_000L // half of HTTP timeout
        // Just give us a ton of batches!
        val computeCountLimit = 100L

        stopwatch.reset()
        stopwatch.start()

        val response = backfillRunner.client.getNextBatchRange(
          GetNextBatchRangeRequest(
            metadata.backfillRunId.toString(),
            backfillRunner.backfillName,
            backfillRunner.partitionName,
            metadata.batchSize,
            metadata.scanSize,
            pkeyCursor,
            KeyRange(metadata.pkeyStart, metadata.pkeyEnd),
            metadata.parameters,
            computeTimeLimitMs,
            computeCountLimit,
            metadata.dryRun,
            true
          )
        )

        backfillRunner.onRpcSuccess()

        if (response.batches.isEmpty()) {
          backfillRunner.factory.transacter.transaction { session ->
            val dbRunPartition = session.load(backfillRunner.partitionId)
            dbRunPartition.precomputing_done = true

            session.save(
              DbEventLog(
                backfillRunner.backfillRunId,
                partition_id = backfillRunner.partitionId,
                type = DbEventLog.Type.STATE_CHANGE,
                message = "precomputing complete"
              )
            )
          }
          logger.info { "Precomputing completed for ${backfillRunner.logLabel()}" }
          break
        }

        backfillRunner.factory.transacter.transaction { session ->
          val dbRunPartition = session.load(backfillRunner.partitionId)
          for (batch in response.batches) {
            pkeyCursor = batch.batch_range.end
            dbRunPartition.computed_scanned_record_count += batch.scanned_record_count
            dbRunPartition.computed_matching_record_count += batch.matching_record_count
          }
          dbRunPartition.precomputing_pkey_cursor = pkeyCursor
          logger.debug { "Precomputer advanced to $pkeyCursor after scanning ${response.batches}" }
        }
      } catch (e: CancellationException) {
        logger.info(e) { "BatchPrecomputer job cancelled ${backfillRunner.logLabel()}" }
        break
      } catch (e: Exception) {
        logger.info(e) {
          "Rpc failure when precomputing next batch for ${backfillRunner.logLabel()}"
        }
        backfillRunner.onRpcFailure(e, "precomputing batch", stopwatch.elapsed())
      }
    }
    logger.info { "BatchPrecomputer stopped ${backfillRunner.logLabel()}" }
  }

  companion object {
    private val logger = getLogger<BatchPrecomputer>()
  }
}
