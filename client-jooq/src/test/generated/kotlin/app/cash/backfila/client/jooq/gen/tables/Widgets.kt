/*
 * This file is generated by jOOQ.
 */
package app.cash.backfila.client.jooq.gen.tables

import app.cash.backfila.client.jooq.gen.Jooq
import app.cash.backfila.client.jooq.gen.indexes.WIDGETS_MANUFACTURER_CREATED_AT
import app.cash.backfila.client.jooq.gen.keys.KEY_WIDGETS_PRIMARY
import app.cash.backfila.client.jooq.gen.tables.records.WidgetsRecord

import kotlin.collections.List

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Index
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row4
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl

/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Widgets(
  alias: Name,
  child: Table<out Record>?,
  path: ForeignKey<out Record, WidgetsRecord>?,
  aliased: Table<WidgetsRecord>?,
  parameters: Array<Field<*>?>?
) : TableImpl<WidgetsRecord>(
  alias,
  Jooq.JOOQ,
  child,
  path,
  aliased,
  parameters,
  DSL.comment(""),
  TableOptions.table()
) {
  companion object {

    /**
     * The reference instance of <code>jooq.widgets</code>
     */
    val WIDGETS = Widgets()
  }

  /**
   * The class holding records for this type
   */
  override fun getRecordType(): Class<WidgetsRecord> = WidgetsRecord::class.java

  /**
   * The column <code>jooq.widgets.widget_token</code>.
   */
  val WIDGET_TOKEN: TableField<WidgetsRecord, ByteArray?> = createField(DSL.name("widget_token"), SQLDataType.VARBINARY(64).nullable(false), this, "")

  /**
   * The column <code>jooq.widgets.manufacturer_token</code>.
   */
  val MANUFACTURER_TOKEN: TableField<WidgetsRecord, String?> = createField(DSL.name("manufacturer_token"), SQLDataType.VARCHAR(255).nullable(false), this, "")

  /**
   * The column <code>jooq.widgets.created_at_ms</code>.
   */
  val CREATED_AT_MS: TableField<WidgetsRecord, Long?> = createField(DSL.name("created_at_ms"), SQLDataType.BIGINT.nullable(false), this, "")

  /**
   * The column <code>jooq.widgets.name</code>.
   */
  val NAME: TableField<WidgetsRecord, String?> = createField(DSL.name("name"), SQLDataType.VARCHAR(128).nullable(false), this, "")

  private constructor(alias: Name, aliased: Table<WidgetsRecord>?) : this(alias, null, null, aliased, null)
  private constructor(alias: Name, aliased: Table<WidgetsRecord>?, parameters: Array<Field<*>?>?) : this(alias, null, null, aliased, parameters)

  /**
   * Create an aliased <code>jooq.widgets</code> table reference
   */
  constructor(alias: String) : this(DSL.name(alias))

  /**
   * Create an aliased <code>jooq.widgets</code> table reference
   */
  constructor(alias: Name) : this(alias, null)

  /**
   * Create a <code>jooq.widgets</code> table reference
   */
  constructor() : this(DSL.name("widgets"), null)

  constructor(child: Table<out Record>, key: ForeignKey<out Record, WidgetsRecord>) : this(Internal.createPathAlias(child, key), child, key, WIDGETS, null)
  override fun getSchema(): Schema = Jooq.JOOQ
  override fun getIndexes(): List<Index> = listOf(WIDGETS_MANUFACTURER_CREATED_AT)
  override fun getPrimaryKey(): UniqueKey<WidgetsRecord> = KEY_WIDGETS_PRIMARY
  override fun getKeys(): List<UniqueKey<WidgetsRecord>> = listOf(KEY_WIDGETS_PRIMARY)
  override fun `as`(alias: String): Widgets = Widgets(DSL.name(alias), this)
  override fun `as`(alias: Name): Widgets = Widgets(alias, this)

  /**
   * Rename this table
   */
  override fun rename(name: String): Widgets = Widgets(DSL.name(name), null)

  /**
   * Rename this table
   */
  override fun rename(name: Name): Widgets = Widgets(name, null)

  // -------------------------------------------------------------------------
  // Row4 type methods
  // -------------------------------------------------------------------------
  override fun fieldsRow(): Row4<ByteArray?, String?, Long?, String?> = super.fieldsRow() as Row4<ByteArray?, String?, Long?, String?>
}
