package app.cash.backfila.client.misk.hibernate

import app.cash.backfila.client.misk.ClientMiskTestingModule
import com.google.inject.Module
import misk.testing.MiskTest
import misk.testing.MiskTestModule

@MiskTest(startService = true)
class MysqlOnlyHibernateBackfillTest : SingleInstanceHibernateBackfillTest() {
  @Suppress("unused")
  @MiskTestModule
  val module: Module = ClientMiskTestingModule(false, listOf(TestBackfill::class))
}
