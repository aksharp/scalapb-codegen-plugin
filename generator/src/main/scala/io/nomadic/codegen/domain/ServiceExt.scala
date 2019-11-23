package io.nomadic.codegen.domain

import io.nomadic.codegen.services.DomainService.WithSeparator

case class ServiceExt(
                       serviceTypeName: String,
                       serviceMethods: List[WithSeparator[Method]],
                       messages: List[Message]
                     )
