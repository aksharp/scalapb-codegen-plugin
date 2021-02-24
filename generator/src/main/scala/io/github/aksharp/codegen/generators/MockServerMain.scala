package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.MockServerMainData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class MockServerMain(implicit val engine: TemplateEngine,
                     val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[MockServerMainData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): MockServerMainData = {
    val services = DomainService.toServices(fileDesc)
    MockServerMainData(
      basePackageName = fileDesc.getPackage,
      javaPackage = toPackageWithFileName(
        packageName = fileDesc.getPackage,
        fileName = fileDesc.getName
      ),
      serviceMethods = DomainService.toServiceMethods(fileDesc),
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
