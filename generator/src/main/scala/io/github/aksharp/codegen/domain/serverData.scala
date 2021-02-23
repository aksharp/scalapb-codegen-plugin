package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class serverData(
                       port: String,
                       basePackageName: String,
                       javaPackage: String,
                       services: List[Service],
                       servicesAsArguments: List[WithSeparator[Service]]
                     )
