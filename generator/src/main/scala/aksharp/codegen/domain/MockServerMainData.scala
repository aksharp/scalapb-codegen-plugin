package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class MockServerMainData(
                               basePackageName: String,
                               javaPackage: String,
                               serviceMethods: List[ServiceMethod],
                               servicesAsArguments: List[WithSeparator[Service]]
                             )