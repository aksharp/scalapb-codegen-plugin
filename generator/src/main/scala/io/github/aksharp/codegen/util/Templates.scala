package io.github.aksharp.codegen.util

object Templates {

  def getTemplateContent(templateName: String): String = m.getOrElse(templateName, "Template not found")

  val server: String =
    """
      |{{#root}}
      |package {{javaPackage}}
      |
      |{{#services}}
      |import {{basePackageName}}.{{serviceTypeName}}Grpc._
      |{{/services}}
      |
      |import io.grpc.Server
      |import io.grpc.netty.NettyServerBuilder
      |import io.grpc.protobuf.services.ProtoReflectionService
      |import scala.concurrent.ExecutionContext
      |import {{basePackageName}}._
      |
      |object server { self =>
      |    private[this] var s: Server = null
      |
      |    def run(
      |        port: Int = {{port}},
      |        awaitTermination: Boolean = false, // true if grpc server is the only entry point in your app
      |{{#servicesAsArguments}}
      |            {{#value}}{{serviceName}}: {{serviceTypeName}}Grpc.{{serviceTypeName}}{{/value}}{{separator}}
      |{{/servicesAsArguments}}
      |
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
      |        if (awaitTermination) s.awaitTermination()
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
      |/*
      |package {{javaPackage}}
      |
      |import {{javaPackage}}.mocks._
      |import scala.concurrent.Future
      |import scala.concurrent.ExecutionContext
      |import scala.concurrent.ExecutionContext.global
      |import {{basePackageName}}._
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
      |        Future.successful(a{{methodOutputType}}())
      |    }
      |{{/methods}}
      |}
      |
      |{{/serviceMethods}}
      |*/
      |{{/root}}
      |""".stripMargin

  val mocks: String =
    """
      |{{#root}}
      |
      |    package {{javaPackage}}.mocks
      |
      |    import {{javaPackage}}._
      |    import org.scalacheck.{Arbitrary, Gen}
      |    {{#imports}}
      |    import {{fqdnImport}}._
      |    {{/imports}}
      |    import scala.concurrent.Future
      |    import {{basePackageName}}._
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
      |    // Arbitraries
      |
      |    object Arbitraries {
      |    {{#messages}}
      |      implicit val arb{{messageTypeName}}: Arbitrary[{{messageTypeName}}] = Arbitrary({{messageTypeName}}Gen())
      |    {{/messages}}
      |
      |    {{#messagesWithOneOf}}
      |      implicit val arb{{messageTypeName}}: Arbitrary[{{messageTypeName}}] = Arbitrary({{messageTypeName}}Gen())
      |    {{/messagesWithOneOf}}
      |    }
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
      |    import {{basePackageName}}._
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

  val grpcClient: String =
    """{{#root}}
      package {{javaPackage}}

      {{#services}}
      import {{basePackageName}}.{{serviceTypeName}}Grpc._
      {{/services}}
      import {{basePackageName}}._


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
      |import {{basePackageName}}._
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
      |import {{basePackageName}}._
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

  val services: String =
    """
      |{{#root}}
      |/* WIP!!!
      |package {{javaPackage}}.services
      |
      |import cats.data.EitherT
      |import {{javaPackage}}._
      |import com.tremorvideo.lib.api.ObservableAndTraceable
      |import com.tremorvideo.lib.api.fp.util.ObservableAndTraceableService
      |import monix.eval.Task
      |import monix.execution.Scheduler
      |import io.github.aksharp.tc._
      |import scala.concurrent.Future
      |import com.tremorvideo.lib.feature.flags.{FeatureFlags, FromBytes}
      |import io.circe.generic.auto._
      |import monix.eval.Task
      |import {{basePackageName}}._
      |
      |
      |{{#serviceMethods}}
      |{{#methods}}
      |
      |case class {{methodName}}FeatureFlags()
      |
      |object {{methodName}}FeatureFlags extends FeatureFlags[Task, {{methodName}}FeatureFlags] {
      |  override def fromBytes(bytes: Array[Byte]): Either[Throwable, {{methodName}}FeatureFlags] =
      |    FromBytes[{{methodName}}FeatureFlags](bytes)
      |}
      |
      |class {{serviceTypeName}}{{methodInputType}}{{methodOutputType}}Service(
      |                      {{methodInputType}}Validator: Validator[Task, {{methodInputType}}, {{methodOutputType}}],
      |                      {{methodInputType}}Processor: Processor[Task, {{methodName}}FeatureFlags, {{methodInputType}}, {{methodOutputType}}]
      |                    )(
      |                    implicit observableAndTraceableService: com.tremorvideo.lib.api.fp.util.ObservableAndTraceableService[Task],
      |                     featureFlagsConfig: com.tremorvideo.lib.feature.flags.FeatureFlagsConfig,
      |                     s: Scheduler
      |                    ) extends {{serviceTypeName}}Grpc.{{serviceTypeName}} {
      |
      |{{#m}}
      |  override def {{methodName}}(req: {{methodInputType}}): Future[{{methodOutputType}}] = {
      |    implicit val ot: ObservableAndTraceable = req.observableAndTraceable
      |
      |    (for {
      |      finalResponse <- {{methodName}}FeatureFlags.runAndObserve(
      |        action = validateAndProcess,
      |        input = req
      |      )
      |    } yield {
      |      finalResponse
      |    }).runToFuture(s)
      |  }
      |
      |  private def validateAndProcess(
      |                                  {{methodName}}FeatureFlags: {{methodName}}FeatureFlags,
      |                                  input: {{methodInputType}}
      |                                ): Task[{{methodOutputType}}] = {
      |    (for {
      |      validatedRequest <- EitherT[Task, {{methodOutputType}}, {{methodInputType}}](
      |        {{methodInputType}}Validator.validate(
      |          item = input
      |        )
      |      )
      |      response <- EitherT.liftF[Task, {{methodOutputType}}, {{methodOutputType}}](
      |        {{methodInputType}}Processor.process(
      |          featureFlags = {{methodName}}FeatureFlags,
      |          validatedRequest = validatedRequest
      |        )
      |      )
      |    } yield {
      |      response
      |    }).value.map(_.merge)
      |  }
      |{{/m}}
      |}
      |{{/methods}}
      |{{/serviceMethods}}
      |*/
      |{{/root}}
      """.stripMargin

  val client: String =
    """
      |{{#root}}
      |package {{javaPackage}}
      |
      |{{#services}}
      |import {{basePackageName}}.{{serviceTypeName}}Grpc._
      |{{/services}}
      |import {{basePackageName}}._
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
      |import {{basePackageName}}.{{serviceTypeName}}Grpc._
      |{{/services}}
      |import {{basePackageName}}._
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
    "services.mustache" -> services,
    "ExampleTest.mustache" -> exampleTest,
    "GrpcClient.mustache" -> grpcClient,
    "mockclient.mustache" -> mockclient,
    "mocks.mustache" -> mocks,
    "MockServerMain.mustache" -> mockServerMain,
    "server.mustache" -> server,
    "mockserver.mustache" -> mockserver
  )

}

