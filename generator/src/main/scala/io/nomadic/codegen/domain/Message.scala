package io.nomadic.codegen.domain

import io.nomadic.codegen.services.DomainService.WithSeparator

case class Message(
                    messageTypeName: String,
                    fields: List[WithSeparator[Field]]
                  )
