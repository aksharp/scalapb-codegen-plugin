val Scala213 = "2.13.4"

val Scala212 = "2.12.12"

//val Scala211 = "2.11.12"


ThisBuild / scalaVersion := Scala213
ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / organization := "io.github.aksharp"

lazy val generator = (project in file("generator"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    crossScalaVersions in ThisBuild := Seq(Scala213, Scala212),

    organization := "io.github.aksharp",

    name := "scalapb-codegen-plugin",

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

    Compile / mainClass := Some("io.github.aksharp.codegen.Main")
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
      "io.github.aksharp" %% "scala-type-classes" % "0.1.5",
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
          "scalapb-codegen-plugin",
          (generator / assembly / target).value / "scalapb-codegen-plugin-assembly-" + version.value + ".jar"
        ),
        outputPath = (Compile / sourceManaged).value,
        options = Seq("grpc", "java_conversions")
      )
    ),

  )

// publish

usePgpKeyHex("59934C580565D7C358C3AB8C62DD7D28B3D79883")

ThisBuild / organization := "io.github.aksharp"
ThisBuild / organizationName := "aksharp"
ThisBuild / organizationHomepage := Some(url("http://github.com/aksharp"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/aksharp/scalapb-codegen-plugin"),
    "scm:git@github.com:aksharp/scalapb-codegen-plugin.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "aksharp",
    name  = "Alexander Khotyanov",
    email = "alex@khotyanov.com",
    url   = url("https://github.com/aksharp")
  )
)

ThisBuild / description := "ScalaPB CodeGen Plugin."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/aksharp/scalapb-codegen-plugin"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true