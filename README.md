# Notes
1) figure out how to configure host/port from sbt values
2) do not forget the magic behind naming mustache templates and generator classes with same name, or overwrite the name property
3) see how Kong handles gRPC interceptors, and how to wire up A/B test interceptor to codegen to dynamically redirect for diff variants
4) port mocks, service impl, etc.
5) have a story for updating codegen, without erasing code
6) (separate effort but needed) how to download only those proto files that we care about and of specific version automatically, including latest version capability
7) TLS over gRPC

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