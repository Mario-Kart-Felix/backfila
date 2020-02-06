package app.cash.backfila.client.misk

import javax.persistence.AttributeOverride
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.criteria.JoinType.INNER
import misk.hibernate.Constraint
import misk.hibernate.DbChild
import misk.hibernate.Gid
import misk.hibernate.Id
import misk.hibernate.Join
import misk.hibernate.Operator
import misk.hibernate.Query
import misk.hibernate.annotation.Keyspace
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter

@Entity
@Keyspace("backfila_orders")
@Table(name = "orders")
class DbOrder private constructor() : DbChild<DbRestaurant, DbOrder> {
  @EmbeddedId
  @AttributeOverride(name = "rootId", column = Column(name = "restaurant_id"))
  @GeneratedValue(generator = "child")
  @GenericGenerator(name = "child", strategy = "misk.hibernate.GidGenerator",
      parameters = [Parameter(name = "rootColumn", value = "restaurant_id")])
  override lateinit var gid: Gid<DbRestaurant, DbOrder>

  @Column(updatable = false, insertable = false)
  override lateinit var id: Id<DbOrder>

  override val rootId: Id<DbRestaurant>
    get() = restaurant_id

  @ManyToOne @JoinColumn(name = "restaurant_id", updatable = false, insertable = false)
  lateinit var restaurant: DbRestaurant

  @Column(updatable = false, insertable = false)
  lateinit var restaurant_id: Id<DbRestaurant>

  constructor(id: Id<DbRestaurant>) : this() {
    this.restaurant_id = id
  }
}

interface OrderQuery : Query<DbOrder> {
  @Join(path = "restaurant", type = INNER)
  fun innerJoinRestaurant(block: RestaurantQuery.() -> Unit): OrderQuery

  @Constraint(path = "restaurant.name", operator = Operator.EQ)
  fun restaurantName(name: String): OrderQuery

  @Constraint(path = "id", operator = Operator.EQ)
  fun id(id: Id<DbMenu>): OrderQuery

  @Constraint(path = "id", operator = Operator.LE)
  fun idLe(id: Id<DbMenu>): OrderQuery

  @Constraint(path = "id", operator = Operator.GE)
  fun idGe(id: Id<DbMenu>): OrderQuery
}
