package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class ServiceExt(
                       serviceName: String,
                       serviceTypeName: String,
                       serviceMethods: List[WithSeparator[Method]],
                       messages: List[Message]
                     )
