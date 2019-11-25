package aksharp.codegen.generators

import aksharp.codegen.domain.{ExampleMainData, MockServerMainData}
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class MockServerMain(implicit val engine: TemplateEngine,
                     val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[MockServerMainData] {

  override def getTemplateData(fileDesc: FileDescriptor): MockServerMainData = {
    val services = DomainService.toServices(fileDesc)
    MockServerMainData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc),
      servicesAsArguments = DomainService.toServicesAsArguments(services)
    )
  }

}
