package app.cash.backfila.service.persistence

import misk.hibernate.DbTimestampedEntity
import misk.hibernate.DbUnsharded
import misk.hibernate.Id
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Table

@Entity
@Table(name = "services")
class DbService() : DbUnsharded<DbService>, DbTimestampedEntity {
  @javax.persistence.Id
  @GeneratedValue
  override lateinit var id: Id<DbService>

  @Column(nullable = false)
  lateinit var registry_name: String

  /** Deprecated, use url instead */
  @Column
  lateinit var connector: String

  /** Deprecated, use url instead */
  @Column(columnDefinition = "mediumtext")
  var connector_extra_data: String? = null

  @Column
  var url: String? = null

  @Column
  var slack_channel: String? = null

  @Column
  override lateinit var created_at: Instant

  @Column
  override lateinit var updated_at: Instant

  constructor(
    registry_name: String,
    connector: String,
    connector_extra_data: String?,
    url: String?,
    slack_channel: String?
  ) : this() {
    this.registry_name = registry_name
    this.connector = connector
    this.connector_extra_data = connector_extra_data
    this.url = url
    this.slack_channel = slack_channel
  }
}
