name := """play-java-hello-world-tutorial-2-6-x"""

version := "0.1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  guice,
  "mysql" % "mysql-connector-java" % "8.0.28"
)

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.typesafeRepo("releases")

fork in run := true

javaOptions in run += {
  val agentJar = (dependencyClasspath in Compile).value
    .find(_.data.getName.contains("ebean-agent"))
    .getOrElse(throw new RuntimeException("ebean-agent not found"))
    .data.getAbsolutePath
  s"-javaagent:$agentJar"
}
