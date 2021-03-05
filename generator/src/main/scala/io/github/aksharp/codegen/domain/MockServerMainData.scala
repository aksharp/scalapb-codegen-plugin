package io.github.aksharp.codegen.domain

import io.github.aksharp.codegen.services.DomainService.WithSeparator
import io.github.aksharp.codegen.services.DomainService.WithSeparator

case class MockServerMainData(
                               basePackageName: String,
                               serviceMethods: List[ServiceMethod],
                               servicesAsArguments: List[WithSeparator[Service]]
                             )