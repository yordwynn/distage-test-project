package covid.docker

import java.util.UUID

import dataStrorage.{CassandraConfig, CassandraPortConfig}
import distage.ModuleDef
import izumi.distage.docker.Docker
import izumi.distage.docker.modules.DockerSupportModule
import zio.Task

object CassandraDockerModule extends ModuleDef {
  include(DockerSupportModule[Task] overridenBy new ModuleDef {
    make[Docker.ClientConfig].from {
      Docker.ClientConfig(
        readTimeoutMs    = 20000,
        connectTimeoutMs = 20000
      )
    }
  })

  make[CassandraContainerDef.Container].fromResource(CassandraContainerDef.make[Task])

  make[CassandraPortConfig].from {
    docker: CassandraContainerDef.Container =>
      val knownAddress = docker.availablePorts.availablePorts(CassandraContainerDef.primaryPort).head
      CassandraPortConfig(knownAddress.hostV4, knownAddress.port)
  }
  // randomize keyspace so that parallel tests with _different axes_ (in different envs) use different keyspaces
  // and do not cause "Column ID mismatch error"
  make[CassandraConfig].from(CassandraConfig(s"test_keyspace_${UUID.randomUUID().toString.take(8)}"))
}
