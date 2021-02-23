package aksharp.codegen.generators

import aksharp.codegen.domain.{ExampleMainData, ServiceData}
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class services(
                implicit val engine: TemplateEngine,
                val descriptorImplicits: DescriptorImplicits
              ) extends MustacheTemplateBase[ServiceData] {

  override def getTemplateData(fileDesc: FileDescriptor): ServiceData = {
    //    val services = DomainService.toServices(fileDesc)
    ServiceData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc)
    )
  }

}
