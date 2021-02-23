package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors
import io.github.aksharp.codegen.domain.ExampleTestData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class ExampleTest(implicit val engine: TemplateEngine,
                       val descriptorImplicits: DescriptorImplicits
                      ) extends MustacheTemplateBase[ExampleTestData] {

  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): ExampleTestData = {
    ExampleTestData(
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = DomainService.toServicesExt(fileDesc)
    )
  }
}
