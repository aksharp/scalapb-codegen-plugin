package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors
import io.nomadic.codegen.domain.mockclientData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class mockclient(implicit val engine: TemplateEngine,
                      val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[mockclientData] {
  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): mockclientData = {
    val services = DomainService.toServices(fileDesc)
    mockclientData(
      javaPackage = fileDesc.getOptions.getJavaPackage,
      servicesAsArguments = DomainService.toServicesAsArguments(services)
    )
  }
}
