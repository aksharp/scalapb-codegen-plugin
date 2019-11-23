package aksharp

import com.google.protobuf.Descriptors._
import com.google.protobuf.{CodedInputStream, ExtensionRegistry}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import aksharp.codegen.generators.{ExampleMain, ExampleTest, GrpcClient, ServicesImpl, client, mockclient, mocks, server}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits
import scalapb.options.compiler.Scalapb

import scala.collection.JavaConverters._

/** This is the interface that code generators need to implement. */
object Generator extends protocbridge.ProtocCodeGenerator {

  // This would make sbt-protoc append the following artifacts to the user's
  // project.  If you have a runtime library this is the place to specify it.
  override def suggestedDependencies: Seq[protocbridge.Artifact] = Nil

  override def run(req: Array[Byte]): Array[Byte] = run(CodedInputStream.newInstance(req))

  val engine: TemplateEngine = new TemplateEngine

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

          val implicits = new DescriptorImplicits(params, fileDescByName.values.toVector)

          val defaultPort = 8080

          val iclientGenerator = GrpcClient()(engine, implicits)
          val serverGenerator = server(defaultPort)(engine, implicits)
          val serviceMocksGenerator = mocks()(engine, implicits)
          val mockclientGenerator = mockclient()(engine, implicits)
          val servicesImplGenerator = ServicesImpl()(engine, implicits)
          val exampleMainGenerator = ExampleMain()(engine, implicits)
          val exampleTestGenerator = ExampleTest()(engine, implicits)
          request.getFileToGenerateList.asScala.foreach {
            name =>
              val fileDesc = fileDescByName(name)

              val apiGatewayHostname = s"api.gateway.${fileDesc.getOptions.getJavaPackage}"
              val localhostHostname = "localhost"

              b.addFile(
                client(defaultPort, localhostHostname)(engine, implicits)
                  .generateFile(fileDesc)
              )

              b.addFile(iclientGenerator.generateFile(fileDesc))
              b.addFile(serverGenerator.generateFile(fileDesc))
              b.addFile(servicesImplGenerator.generateFile(fileDesc))
              b.addFile(serviceMocksGenerator.generateFile(fileDesc))
              b.addFile(mockclientGenerator.generateFile(fileDesc))
              b.addFile(exampleMainGenerator.generateFile(fileDesc))
              b.addFile(exampleTestGenerator.generateFile(fileDesc))
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