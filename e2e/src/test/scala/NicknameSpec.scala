
import io.nomadic.funWithNames.{PersonReply, PersonRequest}
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class NicknameSpec extends WordSpec with Matchers with Eventually {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "test" in {
    Await.result(for {
      res <- io.nomadic.funWithNames.client.generateNickname.createNickname(
        request = PersonRequest(
          person = Option(io.nomadic.funWithNames.mocks.aPerson(
            name = "Alex",
            age = 42
          )),
          suggestedNickname = "blah"
        )
      )
    } yield {
      res should be(PersonReply(
        nickname = "Alex, the man!"
      ))
    }, Duration.Inf)
  }


}
