package io.nomadic.codegen.domain

case class implData(
                     basePackageName: String,
                     javaPackage: String,
                     serviceMethods: List[ServiceMethod]
                   )

case class ServiceMethod(
                          serviceTypeName: String,
                          methods: List[Method]
                        )