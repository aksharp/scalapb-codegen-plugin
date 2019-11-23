package aksharp.codegen.generators

import aksharp.codegen.domain.ExampleTestData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class ExampleTest(implicit val engine: TemplateEngine,
                       val descriptorImplicits: DescriptorImplicits
                      ) extends MustacheTemplateBase[ExampleTestData] {

  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): ExampleTestData = {
    ExampleTestData(
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = DomainService.toServicesExt(fileDesc)
    )
  }
}
