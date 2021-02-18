package aksharp.codegen.generators

import aksharp.codegen.domain.ExampleMainData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class ExampleMain(implicit val engine: TemplateEngine,
                       val descriptorImplicits: DescriptorImplicits
                      ) extends MustacheTemplateBase[ExampleMainData] {

  override def getTemplateData(fileDesc: FileDescriptor): ExampleMainData = {
    val services = DomainService.toServices(fileDesc)
    ExampleMainData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc),
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
