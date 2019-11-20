# scalapb-grpc-client-server-mocks-codegen-plugin

A Protoc plugin that generates...

To test the plugin, within SBT:

```
> e2e/test
```

# Using the plugin

To add the plugin to another project, you need publish it first on maven, or publish locally by using `+publishLocal`.

In the other project, add the following to `project/plugins.sbt`:

```
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.27")

libraryDependencies += "io.nomadic" %% "scalapb-grpc-client-server-mocks-codegen-plugin" % "0.1.0"
```

and the following to your `build.sbt`:
```
PB.targets in Compile := Seq(
  io.nomadic.Generator -> (sourceManaged in Compile).value
)
```