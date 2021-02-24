package io.github.aksharp.codegen

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import com.google.protobuf.{CodedInputStream, ExtensionRegistry}
import io.github.aksharp.codegen.generators._
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits
import scalapb.options.compiler.Scalapb
import scala.jdk.CollectionConverters._

/** This is the interface that code generators need to implement. */
object Generator extends protocbridge.ProtocCodeGenerator {

  // This would make sbt-protoc append the following artifacts to the user's
  // project.  If you have a runtime library this is the place to specify it.
  override def suggestedDependencies: Seq[protocbridge.Artifact] = Nil

  override def run(req: Array[Byte]): Array[Byte] = run(CodedInputStream.newInstance(req))

  implicit val engine: TemplateEngine = new TemplateEngine

  def run(input: CodedInputStream): Array[Byte] = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)
    val request = CodeGeneratorRequest.parseFrom(input)
    val b = CodeGeneratorResponse.newBuilder

    scalapb.compiler.ProtobufGenerator.parseParameters(request.getParameter) match {
      case Right(params) =>
        try {
          val fileDescByName: Map[String, FileDescriptor] =
            request.getProtoFileList.asScala.foldLeft[Map[String, FileDescriptor]](Map.empty) {
              case (acc, fp) =>
                val deps = fp.getDependencyList.asScala.map(acc)
                acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
            }

          implicit val implicits = new DescriptorImplicits(params, fileDescByName.values.toVector)

          val defaultPort = 8080

          val iclientGenerator = new GrpcClient()
          val serverGenerator = new server(defaultPort)
          val serviceMocksGenerator = new mocks()
          val mockclientGenerator = new mockclient()
          val exampleMainGenerator = new ExampleMain()
          val servicesGenerator = new services()
          val exampleTestGenerator = new ExampleTest()
          val mockServerMainGenerator = new MockServerMain()
          val mockserverGenerator = new mockserver()
          request.getFileToGenerateList.asScala.foreach {
            name =>
              val fileDesc = fileDescByName(name)

              val apiGatewayHostname = s"api.gateway.${fileDesc.getPackage}"
              val localhostHostname = "localhost"

              b.addFile(
                new client(defaultPort, localhostHostname)
                  .generateFile(fileDesc)
              )

              b.addFile(iclientGenerator.generateFile(fileDesc))
              b.addFile(serverGenerator.generateFile(fileDesc))
              b.addFile(serviceMocksGenerator.generateFile(fileDesc))
              b.addFile(mockclientGenerator.generateFile(fileDesc))
              b.addFile(exampleMainGenerator.generateFile(fileDesc))
              b.addFile(servicesGenerator.generateFile(fileDesc))
              b.addFile(exampleTestGenerator.generateFile(fileDesc))
              b.addFile(mockserverGenerator.generateFile(fileDesc))
              b.addFile(mockServerMainGenerator.generateFile(fileDesc))
          }
          b.build.toByteArray
        }
        catch {
          case e: Throwable => b.setError(e.getMessage)
        }
      case Left(error) =>
        b.setError(error)
    }
    b.build().toByteArray
  }
}
