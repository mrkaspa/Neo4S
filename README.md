# Neo4S
Neo4S is an Neo ORM for Scala it uses the AnormCypher ibrary

Use Example:

# build.sbt

```scala
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= {
  Seq(
    "com.kreattiewe" %% "neo4s" % "1.0"  
    )
}

```

# Examples
Checkout how to use it on the tests directory [here](https://github.com/mrkaspa/Neo4S/blob/master/src/test/scala/graph/model/orm/NeoORMSpec.scala)