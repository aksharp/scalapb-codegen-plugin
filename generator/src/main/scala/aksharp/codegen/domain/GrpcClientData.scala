package aksharp.codegen.domain

case class GrpcClientData(
                            basePackageName: String,
                            javaPackage: String,
                            services: List[Service]
                          )
