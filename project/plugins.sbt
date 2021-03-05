addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

//libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.10"
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.0-M5"

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5")