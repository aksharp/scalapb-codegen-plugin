package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class MessageWithOneOf(
                             messageTypeName: String,
                             fields: List[WithSeparator[Field]]
                           )
