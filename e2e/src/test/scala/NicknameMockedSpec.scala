
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import io.github.aksharp.codegen.example.funWithNames._
import io.github.aksharp.codegen.example.funWithNames.mocks._

class NicknameMockedSpec extends WordSpec with Matchers with Eventually {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "test" in {
    Await.result(for {
      res <- new io.github.aksharp.codegen.example.funWithNames.mocks.mockclient(
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
          request = aPersonRequest(
            person = aPerson(
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
