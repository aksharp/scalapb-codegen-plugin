package io.github.aksharp.codegen.domain

case class ServiceMethod(
                          serviceTypeName: String,
                          methods: List[Method]
                        ) {
  val m = methods
}
