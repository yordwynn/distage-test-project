package dataStrorage

import java.net.InetSocketAddress

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, ResultSet, Row, Session}
import covid19.model.CovidData
import izumi.distage.framework.model.IntegrationCheck
import izumi.distage.model.definition.DIResource
import izumi.fundamentals.platform.integration.{PortCheck, ResourceCheck}
import logstage.LogBIO
import zio._
import zio.clock.Clock
import zio.duration._

import scala.jdk.CollectionConverters._

final case class CassandraConfig(
  keySpace: String,
)

final case class CassandraPortConfig(
  host: String,
  port: Int,
)

final class CassandraTransactor(session: Session, zioBlocking: zio.blocking.Blocking.Service) {
  // Execute all cassandra requests on Blocking IO pool to not take up threads on CPU pool
  def request[A](f: Session => A): Task[A] = zioBlocking.effectBlocking(f(session))
}

final class CassandraTransactorResource(
  portConfig: CassandraPortConfig,
  portCheck: PortCheck,
  zioBlocking: zio.blocking.Blocking.Service,
  log: LogBIO[IO],
) extends DIResource.OfZIO[Clock, Throwable, CassandraTransactor](
    (for {
      cluster <- ZManaged.fromAutoCloseable(ZIO {
        Cluster
          .builder()
          .addContactPointsWithPorts(InetSocketAddress.createUnresolved(portConfig.host, portConfig.port))
          .build()
      })
      session <- ZManaged.fromAutoCloseable(ZIO(cluster.connect()))
    } yield new CassandraTransactor(session, zioBlocking))
      .retry(
        Schedule.tapInput((error: Throwable) => log.info(s"Got $error")) >>>
        Schedule.recurs(100) >>>
        Schedule.spaced(1.second)
          .tapOutput(nTimes => log.info(s"Retrying cassandra connection $nTimes"))))
    with IntegrationCheck {
  override def resourcesAvailable(): ResourceCheck = {
    portCheck.checkPort(portConfig.host, portConfig.port, s"Couldn't connect to postgres at host=${portConfig.host} defaultPort=${portConfig.port}")
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

class CassandraResource(config: CassandraConfig, transactor: CassandraTransactor) extends DIResource.NoClose[Task, CassandraStorage] {
  private def createKeyspace: IO[Throwable, ResultSet] = {
    val createKeyspace =
      s"""
         |CREATE KEYSPACE IF NOT EXISTS ${config.keySpace}
         |WITH REPLICATION = {
         |'class' : 'SimpleStrategy',
         |'replication_factor' : 1
         | }""".stripMargin

    transactor.request(_.execute(createKeyspace))
  }

  private def createTable: IO[Throwable, ResultSet] = {
    val createTable =
      s"""
         |CREATE TABLE IF NOT EXISTS ${config.keySpace}.${CovidTable.name} (
         |${CovidTable.Fields.location} TEXT PRIMARY KEY,
         |${CovidTable.Fields.isoCode} TEXT,
         |${CovidTable.Fields.confirmed} INT,
         |${CovidTable.Fields.dead} INT,
         |${CovidTable.Fields.recovered} INT)""".stripMargin

    transactor.request(_.execute(createTable))
  }

  override def acquire: Task[CassandraStorage] = {
    createKeyspace
      .flatMap(_ => createTable)
      .as(new CassandraStorage(config, transactor))
  }
}

class CassandraStorage(config: CassandraConfig, transactor: CassandraTransactor) extends DataStorage {
  override def save(data: Seq[CovidData]): IO[Throwable, Unit] = {
    transactor.request(session => {
      data
        .map(item => QueryBuilder
          .update(config.keySpace, CovidTable.name)
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
          .insertInto(config.keySpace, CovidTable.name)
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
    val query = QueryBuilder.select().from(config.keySpace, CovidTable.name)
      .where(QueryBuilder.eq(CovidTable.Fields.location, location))

    transactor.request(_.execute(query).one() match {
      case null => None
      case row => Some(row)
    })
  }

  override def selectAll: IO[Throwable ,List[CovidData]] = {
    val query = QueryBuilder.select().from(config.keySpace, CovidTable.name)

    transactor.request(_.execute(query).all().asScala.toList.map(row => rowToCovidData(row)))
  }
}
