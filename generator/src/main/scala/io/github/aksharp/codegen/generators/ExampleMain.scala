package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.ExampleMainData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class ExampleMain(implicit val engine: TemplateEngine,
                       val descriptorImplicits: DescriptorImplicits
                      ) extends MustacheTemplateBase[ExampleMainData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): ExampleMainData = {
    val services = DomainService.toServices(fileDesc)
    ExampleMainData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc),
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
