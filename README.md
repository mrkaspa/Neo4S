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
    "com.kreattiewe" %% "neo4s" % "1.2.1"  
    )
}

```

# Examples
Checkout how to use it on the tests directory [here](https://github.com/mrkaspa/Neo4S/blob/master/src/test/scala/graph/model/orm/NeoORMSpec.scala)

#Version

## 1.2.1

- Dependency changes

## 1.2.0

- Improved support for optional values

## 1.1

- Added support for Optional values
- Changes in setting parameters for updating nodes and rels

## 1.0

- CRUD For Nodes
- CRUD For Relationships
  