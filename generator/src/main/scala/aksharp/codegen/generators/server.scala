package aksharp.codegen.generators

import aksharp.codegen.domain.serverData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class server(
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
