package aksharp.codegen.domain

case class mocksData(
                      javaPackage: String,
                      services: List[ServiceExt],
                      messages: List[Message]
                    )
