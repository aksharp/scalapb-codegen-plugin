package io.github.aksharp.codegen.domain

case class ServiceData(
                        basePackageName: String,
                        serviceMethods: List[ServiceMethod],
                        services: List[ServiceExt]
                      )

