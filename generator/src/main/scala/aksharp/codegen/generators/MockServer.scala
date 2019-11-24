package aksharp.codegen.generators

import aksharp.codegen.domain.{ExampleMainData, MockServerData}
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class MockServer(implicit val engine: TemplateEngine,
                      val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[MockServerData] {

  override def getTemplateData(fileDesc: FileDescriptor): MockServerData = {
    val services = DomainService.toServices(fileDesc)
    MockServerData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc),
      servicesAsArguments = DomainService.toServicesAsArguments(services)
    )
  }

}
