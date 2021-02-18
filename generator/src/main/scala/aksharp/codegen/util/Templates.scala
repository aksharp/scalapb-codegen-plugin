package aksharp.codegen.util

object Templates {

  def getTemplateContent(templateName: String): String = m.getOrElse(templateName, "Template not found")

  val server: String =
    """
      |{{#root}}
      |package {{javaPackage}}
      |
      |{{#services}}
      |import {{serviceTypeName}}Grpc._
      |{{/services}}
      |
      |import io.grpc.Server
      |import io.grpc.netty.NettyServerBuilder
      |import io.grpc.protobuf.services.ProtoReflectionService
      |import scala.concurrent.ExecutionContext
      |
      |object server { self =>
      |    private[this] var s: Server = null
      |    private val port = {{port}}
      |
      |    def run(
      |{{#servicesAsArguments}}
      |            {{#value}}{{serviceName}}: {{serviceTypeName}}Grpc.{{serviceTypeName}}{{/value}}{{separator}}
      |{{/servicesAsArguments}}
      |    )
      |    (implicit ec: ExecutionContext): Unit = {
      |        s = NettyServerBuilder
      |            .forPort(port)
      |    {{#services}}
      |            .addService({{serviceTypeName}}Grpc.bindService({{serviceName}}, ec))
      |    {{/services}}
      |            .addService(ProtoReflectionService.newInstance())
      |            .build
      |            .start
      |
      |        System.out.println(s"*** running gRPC server on port $port")
      |
      |        sys.addShutdownHook {
      |            System.err.println("*** shutting down gRPC server since JVM is shutting down")
      |            self.stop()
      |            System.err.println("*** server shut down")
      |        }
      |
      |        s.awaitTermination()
      |    }
      |
      |    def stop(): Unit = if (s != null) { s.shutdownNow() }
      |
      |}
      |{{/root}}
      |""".stripMargin

  val mockServerMain: String =
    """
      |{{#root}}
      |
      |package {{javaPackage}}
      |
      |import scala.concurrent.Future
      |import scala.concurrent.ExecutionContext
      |import scala.concurrent.ExecutionContext.global
      |
      |object MockServerMain extends App {
      |
      |    implicit val ec: ExecutionContext = global
      |
      |    mockserver.run(
      |        {{#servicesAsArguments}}
      |            {{#value}}{{serviceName}} = new {{serviceTypeName}}Service{{/value}}{{separator}}
      |        {{/servicesAsArguments}}
      |    )
      |
      |}
      |
      |{{#serviceMethods}}
      |class {{serviceTypeName}}Service extends {{serviceTypeName}}Grpc.{{serviceTypeName}} {
      |{{#methods}}
      |    override def {{methodName}}(req: {{methodInputType}}): Future[{{methodOutputType}}] = {
      |        Future.successful({{methodOutputType}}())
      |    }
      |{{/methods}}
      |}
      |
      |{{/serviceMethods}}
      |
      |{{/root}}
      |""".stripMargin

  val mocks: String =
    """
      |{{#root}}
      |
      |    package {{javaPackage}}.mocks
      |
      |    import {{javaPackage}}._
      |    import org.scalacheck.Gen
      |
      |    import scala.concurrent.Future
      |
      |
      |    // Stubs
      |
      |    {{#messages}}
      |
      |        object a{{messageTypeName}} {
      |
      |        def apply(
      |        {{#fields}}
      |            {{#value}}{{fieldName}}: {{fieldTypeName}} = {{fieldGenerator}}{{/value}}{{separator}}
      |        {{/fields}}
      |        ): {{messageTypeName}} = {{messageTypeName}}(
      |        {{#fields}}
      |            {{#value}}
      |                {{fieldName}} = {{fieldNameOrOptionalOrSeq}}{{separator}}
      |            {{/value}}
      |        {{/fields}}
      |        )
      |
      |        }
      |    {{/messages}}
      |
      |    {{#messagesWithOneOf}}
      |
      |        // OneOffMessage
      |        object a{{messageTypeName}} {
      |
      |        def apply(
      |        {{#fields}}
      |            {{#value}}{{fieldName}}: {{fieldTypeName}} = {{fieldGenerator}}{{/value}}{{separator}}
      |        {{/fields}}
      |        ): {{messageTypeName}} = {
      |           Gen.oneOf(
      |             {{#fields}}
      |                 {{#value}}
      |                      {{fieldName}}{{separator}}
      |                  {{/value}}
      |              {{/fields}}
      |           ).sample.get
      |        }
      |
      |        }
      |    {{/messagesWithOneOf}}
      |
      |
      |    // Generators
      |
      |    {{#messages}}
      |        object {{messageTypeName}}Gen {
      |        def apply(): Gen[{{messageTypeName}}] =
      |        for {
      |        {{#fields}}
      |            {{#value}}
      |                {{fieldName}} <- {{fieldForExpressionGenerator}}
      |            {{/value}}
      |        {{/fields}}
      |        } yield {
      |            {{messageTypeName}}(
      |            {{#fields}}
      |                {{#value}}
      |                    {{fieldName}} = {{fieldNameOrOptionalOrSeq}}{{separator}}
      |                {{/value}}
      |            {{/fields}}
      |            )
      |        }
      |        }
      |    {{/messages}}
      |
      |    {{#messagesWithOneOf}}
      |        object {{messageTypeName}}Gen {
      |        def apply(): Gen[{{messageTypeName}}] =
      |        for {
      |        {{#fields}}
      |            {{#value}}
      |                {{fieldName}} <- {{fieldForExpressionGenerator}}
      |            {{/value}}
      |        {{/fields}}
      |        oneOf <- Gen.oneOf(
      |                   {{#fields}}
      |                     {{#value}}
      |                         {{fieldName}}{{separator}}
      |                     {{/value}}
      |                   {{/fields}}
      |                 )
      |        } yield {
      |         oneOf
      |        }
      |        }
      |    {{/messagesWithOneOf}}
      |
      |    {{#services}}
      |        case class {{serviceTypeName}}Mock(
      |        {{#serviceMethods}}
      |            {{#value}}{{methodName}}Mock: {{methodInputType}} => Future[{{methodOutputType}}] = _ => Future.successful(a{{methodOutputType}}()){{/value}}{{separator}}
      |        {{/serviceMethods}}
      |        ) extends {{serviceTypeName}}Grpc.{{serviceTypeName}} {
      |        {{#serviceMethods}}
      |            {{#value}}
      |                override def {{methodName}}(request: {{methodInputType}}): Future[{{methodOutputType}}] = {{methodName}}Mock(request)
      |            {{/value}}
      |        {{/serviceMethods}}
      |        }
      |
      |    {{/services}}
      |
      |
      |{{/root}}
      |""".stripMargin

  val mockclient: String =
    """
      |{{#root}}
      |    package {{javaPackage}}.mocks
      |
      |    import {{javaPackage}}._
      |    import {{javaPackage}}.mocks._
      |
      |    case class mockclient(
      |    // for each grpc service
      |    {{#servicesAsArguments}}
      |        {{#value}}{{serviceName}}: {{serviceTypeName}}Grpc.{{serviceTypeName}} = new {{serviceTypeName}}Mock{{/value}}{{separator}}
      |    {{/servicesAsArguments}}
      |    ) extends GrpcClient
      |
      |{{/root}}
      |""".stripMargin

  val grpcClient: String = """{{#root}}
      package {{javaPackage}}

      {{#services}}
      import {{serviceTypeName}}Grpc._
      {{/services}}


      trait GrpcClient {

      {{#services}}
          val {{serviceName}}: {{serviceTypeName}}
      {{/services}}

      }

      {{/root}}"""

  val exampleTest: String =
    """
      |{{#root}}
      |/*
      |package {{javaPackage}}.example.test
      |
      |import {{javaPackage}}._
      |import org.scalatest.{Matchers, WordSpec}
      |import scala.concurrent.duration.Duration
      |import scala.concurrent.{Await, ExecutionContext}
      |
      |class ExampleSpec extends WordSpec with Matchers {
      |
      |  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
      |
      |// TODO: start server before testing (refer to example main in {{javaPackage}}.Main)
      |
      |{{#services}}
      |    {{#serviceMethods}}
      |        {{#value}}
      |
      |  "{{serviceName}}.{{methodName}} test. Example test against running server returning default empty {{methodOutputType}}()" in {
      |
      |    val futureResult = client.{{serviceName}}.{{methodName}}(
      |      request = {{methodInputType}}()
      |    )
      |    val result = Await.result(futureResult, Duration.Inf)
      |    val expectedResult = {{methodOutputType}}()
      |    result should be (expectedResult)
      |
      |  }
      |
      |        {{/value}}
      |    {{/serviceMethods}}
      |{{/services}}
      |
      |}
      |
      |*/
      |{{/root}}
      |""".stripMargin

  val exampleMain: String =
    """
      |{{#root}}
      |/*
      |package {{javaPackage}}
      |
      |import scala.concurrent.Future
      |import scala.concurrent.ExecutionContext
      |import scala.concurrent.ExecutionContext.global
      |
      |object Main extends App {
      |
      |    implicit val ec: ExecutionContext = global
      |
      |    server.run(
      |        {{#servicesAsArguments}}
      |            {{#value}}{{serviceName}} = new {{serviceTypeName}}Service{{/value}}{{separator}}
      |        {{/servicesAsArguments}}
      |    )
      |
      |}
      |
      |{{#serviceMethods}}
      |class {{serviceTypeName}}Service extends {{serviceTypeName}}Grpc.{{serviceTypeName}} {
      |{{#methods}}
      |    override def {{methodName}}(req: {{methodInputType}}): Future[{{methodOutputType}}] = {
      |        // TODO: implement me
      |        // example (to return default empty type): Future.successful({{methodOutputType}}())
      |        ???
      |    }
      |{{/methods}}
      |}
      |
      |{{/serviceMethods}}
      |*/
      |{{/root}}
      |""".stripMargin

  val client: String =
    """
      |{{#root}}
      |package {{javaPackage}}
      |
      |{{#services}}
      |import {{serviceTypeName}}Grpc._
      |{{/services}}
      |
      |import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
      |
      |object client extends GrpcClient {
      |
      |    private val host = "{{host}}"
      |    private val port = {{port}}
      |
      |    private val negotiationType: NegotiationType = {{negotiationType}}
      |
      |    {{#services}}
      |        lazy val {{serviceName}}: {{serviceTypeName}}Stub = {{serviceTypeName}}Grpc.stub(
      |        channel = NettyChannelBuilder
      |        .forAddress(host, port)
      |        .negotiationType(negotiationType)
      |        .build
      |        )
      |    {{/services}}
      |
      |}
      |
      |{{/root}}
      |""".stripMargin


  val mockserver =
    """
      |{{#root}}
      |package {{javaPackage}}
      |
      |{{#services}}
      |import {{serviceTypeName}}Grpc._
      |{{/services}}
      |
      |import io.grpc.Server
      |import io.grpc.netty.NettyServerBuilder
      |import io.grpc.protobuf.services.ProtoReflectionService
      |import scala.concurrent.ExecutionContext
      |
      |object mockserver { self =>
      |    private[this] var s: Server = null
      |    private val port = {{port}}
      |
      |    def run(
      |{{#servicesAsArguments}}
      |            {{#value}}{{serviceName}}: {{serviceTypeName}}Grpc.{{serviceTypeName}}{{/value}}{{separator}}
      |{{/servicesAsArguments}}
      |    )
      |    (implicit ec: ExecutionContext): Unit = {
      |        s = NettyServerBuilder
      |            .forPort(port)
      |    {{#services}}
      |            .addService({{serviceTypeName}}Grpc.bindService({{serviceName}}, ec))
      |    {{/services}}
      |            .addService(ProtoReflectionService.newInstance())
      |            .build
      |            .start
      |
      |        System.out.println(s"*** running mock gRPC server on port $port")
      |
      |        sys.addShutdownHook {
      |            System.err.println("*** shutting down mock gRPC server since JVM is shutting down")
      |            self.stop()
      |            System.err.println("*** mock server shut down")
      |        }
      |
      |        s.awaitTermination()
      |    }
      |
      |    def stop(): Unit = if (s != null) { s.shutdownNow() }
      |
      |}
      |{{/root}}
      |""".stripMargin

  private val m = Map(
    "client.mustache" -> client,
    "ExampleMain.mustache" -> exampleMain,
    "ExampleTest.mustache" -> exampleTest,
    "GrpcClient.mustache" -> grpcClient,
    "mockclient.mustache" -> mockclient,
    "mocks.mustache" -> mocks,
    "MockServerMain.mustache" -> mockServerMain,
    "server.mustache" -> server,
    "mockserver.mustache" -> mockserver
  )

}

