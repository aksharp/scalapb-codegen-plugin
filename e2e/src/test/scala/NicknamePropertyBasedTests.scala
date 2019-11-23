
import io.nomadic.funWithNames.{Person, PersonReply, PersonRequest}
import io.nomadic.funWithNames.mocks.{GenerateNicknameMock, PersonGen}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

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

      val client = io.nomadic.funWithNames.mocks.mockclient(
        generateNickname = new GenerateNicknameMock(
          createNicknameMock = req => Future.successful(
            PersonReply(
              nickname = s"${req.person.getOrElse(name, "")}, the $suffix"
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
