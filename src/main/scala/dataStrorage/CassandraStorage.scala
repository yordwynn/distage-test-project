package dataStrorage

import java.net.URI

import com.datastax.driver.core.querybuilder.QueryBuilder
import covid19.model.CovidData
import com.datastax.driver.core.{Cluster, ResultSet, Row, Session}
import izumi.distage.framework.model.IntegrationCheck
import izumi.distage.model.definition.DIResource
import izumi.fundamentals.platform.functional.Identity
import izumi.fundamentals.platform.integration.{PortCheck, ResourceCheck}
import zio.{IO, Schedule, ZIO}

import scala.jdk.CollectionConverters._

final case class CassandraConfig(
  host: String,
  keySpace: String,
  port: Int,
  url: String
)

final case class CassandraPortConfig(
  host: String,
  port: Int,
) {
  def substitute(s: String): String = {
    s.replace("{host}", host).replace("{port}", port.toString)
  }
}

class CassandraTransactor(val config: CassandraConfig, val portConfig: CassandraPortConfig) {
  lazy val cluster: Cluster = Cluster
    .builder()
    .addContactPoint(portConfig.host)
    .withPort(portConfig.port)
    .build()

  lazy val session: IO[Throwable, Session] = ZIO[Session]({
    Thread.sleep(1000)
    cluster.connect()
  }).retry(Schedule.recurs(100))

  def close(): Unit =
    cluster.close()
}

class CassandraTransactorResource(val config: CassandraConfig, val portConfig: CassandraPortConfig, portCheck: PortCheck)
  extends DIResource.Simple[CassandraTransactor] with IntegrationCheck {
  override def acquire: Identity[CassandraTransactor] = {
    new CassandraTransactor(config, portConfig)
  }

  override def release(resource: CassandraTransactor): Identity[Unit] = {
    resource.close()
  }

  override def resourcesAvailable(): ResourceCheck = {
    val str = portConfig.substitute(config.url.stripPrefix("jdbc:"))
    val uri = URI.create(str)

    portCheck.checkUri(uri, portConfig.port, s"Couldn't connect to postgres at uri=$uri defaultPort=${portConfig.port}")
  }
}

object CovidTable {
  val name: String = "covid"

  object Fields {
    val location: String = "location"
    val isoCode: String = "iso_code"
    val confirmed: String = "confirmed"
    val dead: String = "dead"
    val recovered: String = "recovered"
  }
}

class CassandraResource(val transactor: CassandraTransactor) extends DIResource.Simple[CassandraStorage] {
  private def createKeyspace: ZIO[Any, Throwable, ResultSet] = {
    val createKeyspace =
      s"""
         |CREATE KEYSPACE IF NOT EXISTS ${transactor.config.keySpace}
         |WITH REPLICATION = {
         |'class' : 'SimpleStrategy',
         |'replication_factor' : 1
         | }""".stripMargin

    transactor.session.map(_.execute(createKeyspace))
  }

  private def createTable: IO[Throwable, ResultSet] = {
    val createTable =
      s"""
         |CREATE TABLE IF NOT EXISTS ${transactor.config.keySpace}.${CovidTable.name} (
         |${CovidTable.Fields.location} TEXT PRIMARY KEY,
         |${CovidTable.Fields.isoCode} TEXT,
         |${CovidTable.Fields.confirmed} INT,
         |${CovidTable.Fields.dead} INT,
         |${CovidTable.Fields.recovered} INT)""".stripMargin

    transactor.session.map(_.execute(createTable))
  }

  override def acquire: Identity[CassandraStorage] = {
    zio.Runtime.default.unsafeRun(createKeyspace.flatMap(_ => createTable))
    new CassandraStorage(transactor)
  }

  override def release(resource: CassandraStorage): Identity[Unit] = {

  }
}

class CassandraStorage(val transactor: CassandraTransactor) extends DataStorage {
  override def save(data: Seq[CovidData]): IO[Throwable, Unit] = {
    transactor.session.map(session => {
      data
        .map(item => QueryBuilder
          .update(transactor.config.keySpace, CovidTable.name)
          .`with`(QueryBuilder.set(CovidTable.Fields.confirmed, item.confirmed))
          .and(QueryBuilder.set(CovidTable.Fields.recovered, item.recovered))
          .and(QueryBuilder.set(CovidTable.Fields.dead, item.deaths))
          .and(QueryBuilder.set(CovidTable.Fields.isoCode, item.isoCode.fold("NaN")(x => x)))
          .where(QueryBuilder.eq(CovidTable.Fields.location, item.locationName))
        ).foreach(session.execute(_))
      session
    }).map(session => {
      data
        .map(item => QueryBuilder
          .insertInto(transactor.config.keySpace, CovidTable.name)
          .ifNotExists()
          .value(CovidTable.Fields.location, item.locationName)
          .value(CovidTable.Fields.isoCode, item.isoCode.fold("NaN")(x => x))
          .value(CovidTable.Fields.confirmed, item.confirmed)
          .value(CovidTable.Fields.dead, item.deaths)
          .value(CovidTable.Fields.recovered, item.recovered))
        .foreach(session.execute(_))
    })
  }

  implicit def rowToCovidData(row: Row): CovidData = {
    CovidData(
      row.getString(CovidTable.Fields.location),
      Option(row.getString(CovidTable.Fields.isoCode)),
      row.getInt(CovidTable.Fields.confirmed),
      row.getInt(CovidTable.Fields.recovered),
      row.getInt(CovidTable.Fields.dead)
    )
  }

  override def selectByLocation(location: String): IO[Throwable, Option[CovidData]] = {
    val query = QueryBuilder.select().from(transactor.config.keySpace, CovidTable.name)
      .where(QueryBuilder.eq(CovidTable.Fields.location, location))

    transactor.session.map(_.execute(query).one() match {
      case null => None
      case row => Some(row)
    })
  }

  override def selectAll: IO[Throwable ,List[CovidData]] = {
    val query = QueryBuilder.select().from(transactor.config.keySpace, CovidTable.name)

    transactor.session.map(_.execute(query).all().asScala.toList.map(row => rowToCovidData(row)))
  }
}
