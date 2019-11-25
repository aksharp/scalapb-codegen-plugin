package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class mockserverData(
                           port: String,
                           basePackageName: String,
                           javaPackage: String,
                           services: List[Service],
                           servicesAsArguments: List[WithSeparator[Service]]
                         )
