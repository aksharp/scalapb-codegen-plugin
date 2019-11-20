package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.serverData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class server(
                   port: Int
                 )(implicit val engine: TemplateEngine,
                   val descriptorImplicits: DescriptorImplicits
                 ) extends MustacheTemplateBase[serverData] {

  override def getTemplateData(fileDesc: FileDescriptor): serverData = {
    val services = DomainService.toServices(fileDesc)
    serverData(
      port = port.toString,
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = services,
      servicesAsArguments = DomainService.toServicesAsArguments(services)
    )
  }

}
