
import io.nomadic.funWithNames.mocks.GenerateNicknameMock
import io.nomadic.funWithNames.{PersonReply, PersonRequest}
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class NicknameMockedSpec extends WordSpec with Matchers with Eventually {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "test" in {
    Await.result(for {
      res <- io.nomadic.funWithNames.mocks.GrpcMockClient(
        generateNickname = new GenerateNicknameMock(
          createNicknameMock = req => Future.successful(
            PersonReply(
              nickname = s"${req.person.get.name}, the man!"
            )
          )
        )
      )
        .generateNickname
        .createNickname(
          request = io.nomadic.funWithNames.mocks.aPersonRequest(
            person = io.nomadic.funWithNames.mocks.aPerson(
              name = "Alex",
              age = 42
            ),
            suggestedNickname = "X"
          )
        )
    } yield {
      res should be(PersonReply(
        nickname = "Alex, the man!"
      ))
    }, Duration.Inf)
  }





}
