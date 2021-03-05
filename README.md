# Notes

1) do not forget the magic behind naming mustache templates and generator classes with same name, or overwrite the name property
2) have a good story for updating / upgrading
3) TLS over gRPC

# scalapb-codegen-plugin

A Protoc plugin that generates...

To test the plugin, within SBT:

```
> e2e/test
```

# Using the plugin

Add the following to `project/plugins.sbt`:

```
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.1")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.0-M5"

libraryDependencies += "io.github.aksharp" %% "scalapb-codegen-plugin" % "0.4.1-SNAPSHOT"
```

and the following to your `build.sbt`:
```
PB.targets in Compile := Seq(
  aksharp.Generator -> (sourceManaged in Compile).value
)
```
