package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class Message(
                    messageTypeName: String,
                    fields: List[WithSeparator[Field]]
                  )
