package covid.docker

import dataStrorage.CassandraPortConfig
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
}
