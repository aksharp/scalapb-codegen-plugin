import io.grpc.Metadata.BinaryMarshaller
import io.grpc.stub.MetadataUtils
import io.grpc._
import io.nomadic.funWithNames.{GenerateNicknameGrpc, GrpcClient, NicknameReply, NicknameRequest, PersonReply, PersonRequest}

import scala.concurrent.{ExecutionContext, Future}

object Server extends App {
  implicit val ec = ExecutionContext.global
  val generateNickname = new GenerateNicknameGrpc.GenerateNickname {
    override def createNickname(request: PersonRequest): Future[PersonReply] = Future.successful(
      PersonReply(
        nickname = s"${request.name}, the man!"
      )
    )

    override def guessName(request: NicknameRequest): Future[NicknameReply] = Future.successful(
      NicknameReply()
    )
  }
  io.nomadic.funWithNames.server.run(
    generateNickname = generateNickname
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

  }

}
