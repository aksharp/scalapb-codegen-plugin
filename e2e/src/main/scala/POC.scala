import io.grpc.Metadata.BinaryMarshaller
import io.grpc._
import io.grpc.stub.MetadataUtils
import aksharp.funWithNames.PredictorGrpc.PredictorStub
import aksharp.funWithNames._

import scala.concurrent.{ExecutionContext, Future}

object Server extends App {

  aksharp.funWithNames.mocks.GenerateNicknameMock()
  val x = aksharp.funWithNames.mocks.PredictorMock()

  implicit val ec = ExecutionContext.global
  val generateNickname = new GenerateNicknameGrpc.GenerateNickname {
    override def createNickname(request: PersonRequest): Future[PersonReply] = Future.successful(
      PersonReply(
        nickname = s"${request.person.get.name}, the man!"
      )
    )

    override def guessName(request: NicknameRequest): Future[NicknameReply] = Future.successful(
      NicknameReply()
    )
  }
  val predictor = new PredictorGrpc.Predictor {
    override def predictNickname(request: PersonRequest): Future[PersonReply] = Future.successful(
      aksharp.funWithNames.mocks.aPersonReply()
//      PersonReply(
//        nickname = s"I predict your nickname to be ${request.name}"
//      )
    )
  }

  aksharp.funWithNames.server.run(
    generateNickname = aksharp.funWithNames.mocks.GenerateNicknameMock(), // generateNickname,
    predictor = aksharp.funWithNames.mocks.PredictorMock() // predictor
  )
}

class POC {

  import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
  import aksharp.funWithNames.GenerateNicknameGrpc._

  object client extends GrpcClient {

    private val host = "api.gateway.aksharp.funWithNames"
    private val port = 8080

    private val negotiationType: NegotiationType = NegotiationType.PLAINTEXT

    val channel: ManagedChannel = NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(negotiationType)
      .build

    val metadata = new Metadata()
    val binaryMarshaller: BinaryMarshaller[String] = new BinaryMarshaller[String] {
      override def toBytes(value: String): Array[Byte] = value.getBytes()

      override def parseBytes(serialized: Array[Byte]): String = new String(serialized)
    }

    //TODO this is how to send information to API Gateway so it knows which service to forward to.
    metadata.put(Metadata.Key.of("abTest", binaryMarshaller), "variantA")

    val interceptor: ClientInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)

    val channelWithInterceptor: Channel = ClientInterceptors.intercept(channel, interceptor)

    lazy val generateNickname: GenerateNicknameStub = GenerateNicknameGrpc.stub(channelWithInterceptor)
    lazy val predictor: PredictorStub = PredictorGrpc.stub(channelWithInterceptor)

  }

}
