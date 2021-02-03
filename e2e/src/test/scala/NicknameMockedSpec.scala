
import aksharp.funWithNames.mocks.GenerateNicknameMock
import aksharp.funWithNames.{PersonReply, PersonRequest}
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class NicknameMockedSpec extends WordSpec with Matchers with Eventually {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "test" in {
    Await.result(for {
      res <- aksharp.funWithNames.mocks.mockclient(
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
          request = aksharp.funWithNames.mocks.aPersonRequest(
            person = aksharp.funWithNames.mocks.aPerson(
              name = "Alex",
              age = 42,
              `type` = "father"
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
