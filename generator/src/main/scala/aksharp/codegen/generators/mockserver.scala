package aksharp.codegen.generators

import aksharp.codegen.domain.mockserverData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mockserver(implicit val engine: TemplateEngine,
                  val descriptorImplicits: DescriptorImplicits
                ) extends MustacheTemplateBase[mockserverData] {

  override def getTemplateData(fileDesc: FileDescriptor): mockserverData = {
    val services = DomainService.toServices(fileDesc)
    mockserverData(
      port = "9090",
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = services,
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
