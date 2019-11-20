package io.nomadic.codegen.domain

import io.nomadic.codegen.services.DomainService.WithSeparator

case class serverData(
                       port: String,
                       basePackageName: String,
                       javaPackage: String,
                       services: List[Service],
                       servicesAsArguments: List[WithSeparator[Service]]
                     )
