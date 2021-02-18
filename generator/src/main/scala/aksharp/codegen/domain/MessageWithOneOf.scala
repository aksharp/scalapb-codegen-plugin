package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class MessageWithOneOf(
                             messageTypeName: String,
                             fields: List[WithSeparator[Field]]
                           )
