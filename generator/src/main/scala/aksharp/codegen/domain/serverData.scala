package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator
import aksharp.codegen.services.DomainService.WithSeparator

case class serverData(
                       port: String,
                       basePackageName: String,
                       javaPackage: String,
                       services: List[Service],
                       servicesAsArguments: List[WithSeparator[Service]]
                     )
