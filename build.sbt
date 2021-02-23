val Scala213 = "2.13.4"

val Scala212 = "2.12.12"

//val Scala211 = "2.11.12"

ThisBuild / scalaVersion := Scala213
ThisBuild / version := "0.1.2-SNAPSHOT"
ThisBuild / organization := "aksharp"

resolvers ++= Seq(
  ("Artifactory Releases" at "http://artifactory.service.iad1.consul:8081/artifactory/libs-release/").withAllowInsecureProtocol(true),
  ("Artifactory Snapshots" at "http://artifactory.service.iad1.consul:8081/artifactory/libs-snapshot/").withAllowInsecureProtocol(true)
)

resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val generator = (project in file("generator"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions in ThisBuild := Seq(Scala212, Scala213),

    organization := "aksharp",

    name := "scalapb-grpc-client-server-mocks-codegen-plugin",

    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin" % scalapb.compiler.Version.scalapbVersion,
      "org.scalatra.scalate" %% "scalate-core" % "1.9.6"
    ),

    assemblyOption in assembly := (assemblyOption in assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = !isWindows))
    ),

    unmanagedResourceDirectories in Compile += {
      baseDirectory.value / "templates"
    },

    Compile / mainClass := Some("aksharp.Main")
//
//    resourceDirectory in Compile := file(".") / "./generator/src/main/scala/templates",
//    resourceDirectory in Runtime := file(".") / "./generator/src/main/scala/templates"
  )

def isWindows: Boolean = sys.props("os.name").startsWith("Windows")

// The e2e project exercises the generator. We need to use the generator project above to generate
// code for this project. To accomplish that, we use the assembly task to create a fat jar of the
// generator, and provide this to sbt-protoc as a plugin.
lazy val e2e = (project in file("e2e"))
  .settings(
    libraryDependencies ++= Seq(
      "com.tremorvideo" %% "lib-kafka" % "8.0.0-SNAPSHOT",
      "com.tremorvideo" %% "lib-feature-flags" % "3.1.0-SNAPSHOT",
      "com.tremorvideo" %% "lib-api" % "0.32.0",
      "io.github.aksharp" %% "scala-type-classes" % "0.1.5",
      "org.typelevel" %% "cats-core" % "2.3.1",
      "io.monix" %% "monix" % "3.3.0",
      "org.scalatest" %% "scalatest" % "3.0.8",
      "org.scalacheck" %% "scalacheck" % "1.15.2",
      "io.grpc" % "grpc-services" % "1.35.0",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    ),

    // Makes the e2e project depends on assembling the generator
    Compile / PB.generate := ((Compile / PB.generate) dependsOn (generator / Compile / assembly)).value,

    // Regenerates protos on each compile even if they have not changed. This is so changes in the plugin
    // are picked up without having to manually clean.
    Compile / PB.recompile := true,

    Compile / PB.targets := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value,

      // Creates a target using the assembled
      protocbridge.Target(
        generator = PB.gens.plugin(
          "scalapb-grpc-client-server-mocks-codegen-plugin",
          (generator / assembly / target).value / "scalapb-grpc-client-server-mocks-codegen-plugin-assembly-" + version.value + ".jar"
        ),
        outputPath = (Compile / sourceManaged).value,
        options = Seq("grpc", "java_conversions")
      )
    ),

  )

resolvers ++= Seq(
  ("Artifactory Releases" at "http://artifactory.service.iad1.consul:8081/artifactory/libs-release/").withAllowInsecureProtocol(true),
  ("Artifactory Snapshots" at "http://artifactory.service.iad1.consul:8081/artifactory/libs-snapshot/").withAllowInsecureProtocol(true)
)

publishTo := {
  val artifactory = "http://artifactory.service.iad1.consul:8081/artifactory/"
  val (name, url) = if (version.value.contains("-SNAPSHOT"))
    ("sbt-plugin-snapshots", artifactory + "libs-snapshot")
  else
    ("sbt-plugin-releases", artifactory + "libs-release")
  Some(Resolver.url(name, new URL(url)).withAllowInsecureProtocol(true))
}