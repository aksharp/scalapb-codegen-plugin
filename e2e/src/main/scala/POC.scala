import io.grpc.Metadata.BinaryMarshaller
import io.grpc.stub.MetadataUtils
import io.grpc._
import io.nomadic.funWithNames.PredictorGrpc.PredictorStub
import io.nomadic.funWithNames.mocks.aPersonReply
import io.nomadic.funWithNames.{GenerateNicknameGrpc, GrpcClient, NicknameReply, NicknameRequest, PersonReply, PersonRequest, PredictorGrpc}
import org.scalacheck.Gen

import scala.concurrent.{ExecutionContext, Future}

object Server extends App {

  io.nomadic.funWithNames.mocks.GenerateNicknameMock()
  val x = io.nomadic.funWithNames.mocks.PredictorMock()

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
      io.nomadic.funWithNames.mocks.aPersonReply()
//      PersonReply(
//        nickname = s"I predict your nickname to be ${request.name}"
//      )
    )
  }

  io.nomadic.funWithNames.server.run(
    generateNickname = io.nomadic.funWithNames.mocks.GenerateNicknameMock(), // generateNickname,
    predictor = io.nomadic.funWithNames.mocks.PredictorMock() // predictor
  )
}

class POC {

  import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
  import io.nomadic.funWithNames.GenerateNicknameGrpc._

  object client extends GrpcClient {

    private val host = "api.gateway.io.nomadic.funWithNames"
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
