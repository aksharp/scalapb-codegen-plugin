package aksharp.codegen.domain

case class clientData(
                       port: String,
                       host: String,
                       negotiationType: String,
                       basePackageName: String,
                       javaPackage: String,
                       services: List[Service]
                     )
