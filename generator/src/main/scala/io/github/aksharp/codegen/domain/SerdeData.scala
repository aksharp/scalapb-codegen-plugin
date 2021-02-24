package io.github.aksharp.codegen.domain

case class SerdeData(
                      basePackageName: String,
                      javaPackage: String,
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