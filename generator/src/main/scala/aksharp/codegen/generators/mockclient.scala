package aksharp.codegen.generators

import com.google.protobuf.Descriptors
import aksharp.codegen.domain.mockclientData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mockclient(implicit val engine: TemplateEngine,
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
