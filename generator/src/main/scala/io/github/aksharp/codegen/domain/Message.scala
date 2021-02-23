package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class Message(
                    messageTypeName: String,
                    fields: List[WithSeparator[Field]],
                    isOneOf: Boolean
                  )
