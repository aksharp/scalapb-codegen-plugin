
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import io.github.aksharp.codegen.example.funWithNames.mocks._
import io.github.aksharp.codegen.example.funWithNames._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object NicknamePropertyBasedTests extends Properties("Nickname generator property tests") {

  property("Should generate nicknames") = forAll(
    PersonGen(),
    Gen.alphaNumStr,
    Gen.alphaNumStr
  ) {
    (
      person: Person,
      suffix: String,
      suggestedNickname: String
    ) => {
      implicit val ec = ExecutionContext.global

      val expectedTransformation = s"${person.name}, the $suffix"
      println(s"%%% expectedTransformation = $expectedTransformation")

      val client = mockclient(
        generateNickname = new GenerateNicknameMock(
          createNicknameMock = req => Future.successful(
            PersonReply(
              nickname = s"${req.person.map(_.name).getOrElse(name, "")}, the $suffix"
            )
          )
        )
      )

      val futureResponse = client
        .generateNickname
        .createNickname(
          request = PersonRequest(
            person = Option(person),
            suggestedNickname = suggestedNickname
          )
        )

      val response: PersonReply = Await.result(futureResponse, Duration.Inf)
      println(s"%%% actual = ${response.nickname}")

      val expected = PersonReply(
        nickname = expectedTransformation
      )

      //      println(s"### check response $response == $expected")


      response == expected
    }

  }
}
