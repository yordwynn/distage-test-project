package dataStrorage

import akka.event.slf4j.SLF4JLogging
import com.datastax.driver.core.querybuilder.QueryBuilder
import covid19.model.CovidData
import com.datastax.driver.core.{Cluster, Row}

class CassandraStorage extends DataStorage with SLF4JLogging {
  private val cluster = Cluster.builder().addContactPoint("172.17.0.2").build()
  private val session = cluster.connect("lul")

  object CovidTable {
    val location: String = "location"
    val confirmed: String = "confirmed"
    val dead: String = "dead"
    val recovered: String = "recovered"
  }

  def createTable: Unit = {
    val query =
      s"""
        |CREATE TABLE IF NOT EXISTS covid (
        |${CovidTable.location} TEXT PRIMARY KEY,
        |${CovidTable.confirmed} INT,
        |${CovidTable.dead} INT,
        |${CovidTable.recovered} INT)""".stripMargin

    session.execute(query)
  }

  override def save(data: Seq[CovidData]): Unit = {
    data
      .map(item => QueryBuilder
        .update("covid")
        .`with`(QueryBuilder.set(CovidTable.confirmed, item.confirmed))
        .and(QueryBuilder.set(CovidTable.recovered, item.recovered))
        .and(QueryBuilder.set(CovidTable.dead, item.deaths))
        .where(QueryBuilder.eq(CovidTable.location, item.locationName))
      ).foreach(session.execute(_))

    data
      .map(item => QueryBuilder
        .insertInto("covid")
        .ifNotExists()
        .value(CovidTable.location, item.locationName)
        .value(CovidTable.confirmed, item.confirmed)
        .value(CovidTable.dead, item.deaths)
        .value(CovidTable.recovered, item.recovered))
      .foreach(session.execute(_))
  }

  override def getByLocation(location: String): Option[CovidData] = {
    implicit def rowToCovidData(row: Row): CovidData = {
      CovidData(
        row.getString(CovidTable.location),
        None,
        row.getInt(CovidTable.confirmed),
        row.getInt(CovidTable.recovered),
        row.getInt(CovidTable.dead)
      )
    }

    val query = QueryBuilder.select().from("covid").where(QueryBuilder.eq(CovidTable.location, location))
    session.execute(query).one() match {
      case null => None
      case row => Some(row)
    }
  }

  def close: Unit = cluster.close()
}
