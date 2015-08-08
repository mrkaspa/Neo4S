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
    "com.kreattiewe" %% "neo4s" % "2.0.0"  
    )
}

```

# Examples
Checkout how to use it on the tests directory [here](https://github.com/mrkaspa/Neo4S/blob/master/src/test/scala/graph/model/orm/NeoORMSpec.scala)


#Version

## 2.0.0

- Added type level scala, this version is incompatible with 1.x.x code (previous version)

## 1.5.0

- Fix unmarshalling Seq of Doubles or Ints

## 1.4.8

- NodeDAO.save returns false when the id of t is None 

## 1.4.7

- Added deleteWithRelations to NodeDAO

## 1.4.6

- FindById returns Optional value again


## 1.4.5

- Futures improved in findById

## 1.4.4

- Added transform option to for objects and lists 


## 1.4.3

- Uniqueness restrictions optionals for Relations 

## 1.4.2

- Catching unmarshalling errors

## 1.4.1

- Fix in unmarshalling null/None

## 1.4.0

- One label per node
- Added utilities to create and drop indexes

## 1.3.2

- Fixed execution context in the DAO and Query methods

## 1.3.1

- Added Mapper companion object to create Mapper[T] [here](https://github.com/mrkaspa/Neo4S/blob/master/src/test/scala/graph/model/orm/NeoORMTestModels.scala)


## 1.3.0

- Simplified types for the Main classes NodeDAO, RelDAO, NeoQuery
- Check how to define the models and the DAOs [here](https://github.com/mrkaspa/Neo4S/blob/master/src/test/scala/graph/model/orm/NeoORMTestModels.scala)

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
  