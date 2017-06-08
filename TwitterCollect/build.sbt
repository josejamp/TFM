name := "spark-twitter-analizer"

version := "1.0"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.0.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.0.0" % "provided"

libraryDependencies += "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.4"

libraryDependencies += "commons-cli" % "commons-cli" % "1.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"

libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.6.0"
)

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
