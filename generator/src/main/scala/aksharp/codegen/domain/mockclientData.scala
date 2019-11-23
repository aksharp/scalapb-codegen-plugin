package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class mockclientData(
                           javaPackage: String,
                           servicesAsArguments: List[WithSeparator[Service]]
                         )
