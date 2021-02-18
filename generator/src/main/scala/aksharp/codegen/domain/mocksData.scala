package aksharp.codegen.domain

case class mocksData(
                      javaPackage: String,
                      services: List[ServiceExt],
                      allMessages: List[Message]
                    ) {
  val messages = allMessages.filterNot(_.isOneOf)

  val messagesWithOneOf = allMessages.filter(_.isOneOf).map(
    m =>
      MessageWithOneOf(
        messageTypeName = m.messageTypeName,
        fields = m.fields
      )
  )
}
