package covid.docker

import izumi.distage.docker.{ContainerDef, Docker}

object CassandraContainerDef extends ContainerDef {
  val primaryPort: Docker.DockerPort = Docker.DockerPort.TCP(9042)

  override def config: Config = {
    Config(
      image = "cassandra:latest",
      ports = Seq(primaryPort)
    )
  }
}
