# Notes
a) Generate MockServer
b) Generate ExamplePropertyTests 
1) figure out how to configure host/port from sbt values
2) do not forget the magic behind naming mustache templates and generator classes with same name, or overwrite the name property
3) see how Kong handles gRPC interceptors, and how to wire up A/B test interceptor to codegen to dynamically redirect for diff variants
4) have a better story for updating codegen, without erasing code
5) (separate effort but needed) how to download only those proto files that we care about and of specific version automatically, including latest version capability
6) TLS over gRPC

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

libraryDependencies += "aksharp" %% "scalapb-grpc-client-server-mocks-codegen-plugin" % "0.1.0"
```

and the following to your `build.sbt`:
```
PB.targets in Compile := Seq(
  aksharp.Generator -> (sourceManaged in Compile).value
)
```