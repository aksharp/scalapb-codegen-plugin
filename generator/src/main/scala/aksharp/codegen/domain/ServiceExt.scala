package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class ServiceExt(
                       serviceTypeName: String,
                       serviceMethods: List[WithSeparator[Method]],
                       messages: List[Message]
                     )
