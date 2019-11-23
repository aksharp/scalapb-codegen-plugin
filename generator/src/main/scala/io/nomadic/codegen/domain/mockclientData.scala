package io.nomadic.codegen.domain

import io.nomadic.codegen.services.DomainService.WithSeparator

case class mockclientData(
                           javaPackage: String,
                           servicesAsArguments: List[WithSeparator[Service]]
                         )
