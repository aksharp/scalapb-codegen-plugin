# scalapb-codegen-plugin

For ScalaPB generated case classes from proto definitions, this plugin generates: 
1. grpc server
2. grpc client
3. scalacheck generators and arbitraries for property based testing
4. mocks 
5. mock grpc server
6. mock grpc client
7. kafka serde (serialization / deserialization) 

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

# Test the plugin
```
sbt
> e2e/test
```
