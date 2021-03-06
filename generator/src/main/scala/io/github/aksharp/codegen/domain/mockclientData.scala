package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class mockclientData(
                           basePackageName: String,
                           servicesAsArguments: List[WithSeparator[Service]]
                         )
