import SonatypeKeys._

// Your project orgnization (package name)
organization := "com.kreattiewe"

// Your profile name of the sonatype account. The default is the same with the organization
sonatypeProfileName := "com.kreattiewe"

name := """neo4s"""

version := "1.4.6"

scalaVersion := "2.11.6"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9"

libraryDependencies ++= {
  val scalaTestV = "2.2.1"
  Seq(
    "com.kreattiewe" %% "case-class-mapper" % "1.0.1",
    "org.anormcypher" %% "anormcypher" % "0.6.0",
    "org.scala-lang" % "scala-reflect" % "2.11.6",
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}


pomExtra := (
  <url>https://github.com/mrkaspa/Neo4S</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>github.com/mrkaspa/Neo4S.git</url>
      <connection>scm:git:git@github.com:mrkaspa/Neo4S.git</connection>
    </scm>
    <developers>
      <developer>
        <id>mrkaspa</id>
        <name>Michel Perez</name>
        <url>http://mrkaspa.com</url>
      </developer>
    </developers>)
