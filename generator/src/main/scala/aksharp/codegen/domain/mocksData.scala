package aksharp.codegen.domain

case class mocksData(
                      javaPackage: String,
                      services: List[ServiceExt],
                      allMessages: List[Message],
                      allImports: List[ImportExt]
                    ) {
  val messages = allMessages.filterNot(_.isOneOf)

  val imports = allImports.filterNot(_.fqdnImport.contains("scalapb.options"))

  val messagesWithOneOf = allMessages.filter(_.isOneOf).map(
    m =>
      MessageWithOneOf(
        messageTypeName = m.messageTypeName,
        fields = m.fields
      )
  )
}
