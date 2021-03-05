package io.github.aksharp.codegen.domain

case class GrpcClientData(
                            basePackageName: String,
                            services: List[Service]
                          )
