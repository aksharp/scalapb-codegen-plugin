package aksharp.codegen.domain

import aksharp.codegen.services.DomainService.WithSeparator

case class ExampleMainData(
                            basePackageName: String,
                            javaPackage: String,
                            serviceMethods: List[ServiceMethod],
                            servicesAsArguments: List[WithSeparator[Service]]
                          )