package io.github.aksharp.codegen.domain

case class SerdeData(
                      basePackageName: String,
                      javaPackage: String,
                      messages: List[Message]
                    )