package app.cash.backfila.service.persistence

enum class BackfillState {
  PAUSED,
  RUNNING,
  CANCELLED,
  COMPLETE
}
