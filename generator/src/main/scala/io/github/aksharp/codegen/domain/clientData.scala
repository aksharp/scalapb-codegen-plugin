package io.github.aksharp.codegen.domain

case class clientData(
                       port: String,
                       negotiationType: String,
                       basePackageName: String,
                       services: List[Service]
                     )
