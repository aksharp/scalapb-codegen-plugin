package io.github.aksharp.codegen.domain

case class ServiceData(
                        basePackageName: String,
                        javaPackage: String,
                        serviceMethods: List[ServiceMethod]
                      )
