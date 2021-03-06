package io.github.aksharp.codegen.util

object Templates {

  def getTemplateContent(templateName: String): String = m.getOrElse(templateName, "Template not found")

  val server: String =
    """
      |{{#root}}
      |package {{basePackageName}}
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
      |package {{basePackageName}}
      |
      |import {{basePackageName}}.mocks._
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
      |    package {{basePackageName}}.mocks
      |
      |    import {{basePackageName}}._
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
      |    package {{basePackageName}}.mocks
      |
      |    import {{basePackageName}}._
      |    import {{basePackageName}}.mocks._
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
      package {{basePackageName}}

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
      |package {{basePackageName}}.example.test
      |
      |import {{basePackageName}}._
      |import org.scalatest.{Matchers, WordSpec}
      |import scala.concurrent.duration.Duration
      |import scala.concurrent.{Await, ExecutionContext}
      |import {{basePackageName}}._
      |import com.tremorvideo.api.observable._
      |
      |class ExampleSpec extends WordSpec with Matchers {
      |
      |  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
      |
      |// TODO: start server before testing (refer to example main in {{basePackageName}}.Main)
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
      |package {{basePackageName}}
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
       {{#root}}

       package {{basePackageName}}.services

       import cats.data.EitherT
       import {{basePackageName}}._
       import monix.eval.Task
       import monix.execution.Scheduler
       import scala.concurrent.Future
       import io.circe.generic.auto._
       import monix.eval.Task
       import {{basePackageName}}._
       import com.tremorvideo.api.models._
       import com.tremorvideo.api.services._
       import com.tremorvideo.api.interfaces._
       import com.tremorvideo.api.observable._


       {{#services}}

       class {{serviceTypeName}}[FF](

        {{#serviceMethods}}
          {{#value}}
             {{methodInputType}}Validator: Validator[Task, {{methodInputType}}, {{methodOutputType}}],
             {{methodInputType}}Processor: Processor[Task, FF, {{methodInputType}}, {{methodOutputType}}]
          {{/value}}{{separator}}
        {{/serviceMethods}}

       )(
         implicit observableAndTraceableService: ObservableAndTraceableService[Task],
         featureFlagsConfig: FeatureFlagsConfig,
         scheduler: Scheduler,

        {{#serviceMethods}}
          {{#value}}
            {{methodName}}RunnerAndObserver: RunnerAndObserver[Task, FF, {{methodInputType}}, {{methodOutputType}}]{{separator}}
          {{/value}}
        {{/serviceMethods}}

       ) extends {{serviceTypeName}}Grpc.{{serviceTypeName}} {

        {{#serviceMethods}}
          {{#value}}
           override def {{methodName}}(input: {{methodInputType}}): Future[{{methodOutputType}}] = {
              implicit val ot: ObservableAndTraceable = input.observableAndTraceable
              (for {
               finalResponse <- {{methodName}}RunnerAndObserver.runAndObserve(
                 action = {{methodName}}ValidateAndProcess,
                 input = input
               )
              } yield {
               finalResponse
              }).runToFuture(scheduler)
           }


            private def {{methodName}}ValidateAndProcess(
              featureFlags: FF,
              input: {{methodInputType}}
            ): Task[{{methodOutputType}}] = {
              (for {
                validatedRequest <- EitherT[Task, {{methodOutputType}}, {{methodInputType}}](
                  {{methodInputType}}Validator.validate(
                    item = input
                  )
                )
                response <- EitherT.liftF[Task, {{methodOutputType}}, {{methodOutputType}}](
                  {{methodInputType}}Processor.process(
                    featureFlags = featureFlags,
                    validatedRequest = validatedRequest
                  )
                )
              } yield {
                response
              }).value.map(_.merge)
            }


          {{/value}}
        {{/serviceMethods}}

       }
       {{/services}}


       {{/root}}
      """.stripMargin


  val client: String =
    """
      |{{#root}}
      |package {{basePackageName}}
      |
      |{{#services}}
      |import {{basePackageName}}.{{serviceTypeName}}Grpc._
      |{{/services}}
      |import {{basePackageName}}._
      |
      |import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
      |
      |class Client(
      |    host: String, // get from config. example: "[service name].service.[region].consul"
      |    port: Int = {{port}}
      |) extends GrpcClient {
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
      |package {{basePackageName}}
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

  val serde =
    """
      |{{#root}}
      |
      |package {{basePackageName}}.serde
      |
      |
      |{{#messages}}
      |
      |object {{messageTypeName}}Serde {
      |
      |    implicit val {{messageTypeName}}Serializer: org.apache.kafka.common.serialization.Serializer[{{basePackageName}}.{{messageTypeName}}] =
      |        new org.apache.kafka.common.serialization.Serializer[{{basePackageName}}.{{messageTypeName}}] {
      |            override def serialize(topic: String, data: {{basePackageName}}.{{messageTypeName}}): Array[Byte] =
      |                data.toByteArray
      |        }
      |
      |    implicit val {{messageTypeName}}Deserializer: org.apache.kafka.common.serialization.Deserializer[{{basePackageName}}.{{messageTypeName}}] =
      |        new org.apache.kafka.common.serialization.Deserializer[{{basePackageName}}.{{messageTypeName}}] {
      |            override def deserialize(topic: String, data: Array[Byte]): {{basePackageName}}.{{messageTypeName}} =
      |                {{basePackageName}}.{{messageTypeName}}.parseFrom(data)
      |        }
      |
      |}
      |
      |{{/messages}}
      |
      |{{#messagesWithOneOf}}
      |
      |object {{messageTypeName}}Serde {
      |
      |    implicit val {{messageTypeName}}Serializer: org.apache.kafka.common.serialization.Serializer[{{basePackageName}}.{{messageTypeName}}] =
      |        new org.apache.kafka.common.serialization.Serializer[{{basePackageName}}.{{messageTypeName}}] {
      |            override def serialize(topic: String, data: {{basePackageName}}.{{messageTypeName}}): Array[Byte] =
      |                data.asMessage.toByteArray
      |        }
      |
      |    implicit val {{messageTypeName}}Deserializer: org.apache.kafka.common.serialization.Deserializer[{{basePackageName}}.{{messageTypeName}}] =
      |        new org.apache.kafka.common.serialization.Deserializer[{{basePackageName}}.{{messageTypeName}}] {
      |            override def deserialize(topic: String, data: Array[Byte]): {{basePackageName}}.{{messageTypeName}} =
      |                {{basePackageName}}.{{messageTypeName}}Message.parseFrom(data).to{{messageTypeName}}
      |        }
      |
      |}
      |
      |{{/messagesWithOneOf}}
      |
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
    "mockserver.mustache" -> mockserver,
    "serde.mustache" -> serde
  )

}

