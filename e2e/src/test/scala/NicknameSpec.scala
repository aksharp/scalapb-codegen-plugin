
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import io.github.aksharp.codegen.example.funWithNames._
import io.github.aksharp.codegen.example.funWithNames.mocks._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class NicknameSpec extends WordSpec with Matchers with Eventually {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "test" ignore {
    Await.result(for {
      res <- new io.github.aksharp.codegen.example.funWithNames.Client(
        host = "localhost",
        port = 8080
      )
        .generateNickname
        .createNickname(
        request = PersonRequest(
          person = Option(aPerson(
            name = "Alex",
            age = 42,
            `type` = "father"
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
