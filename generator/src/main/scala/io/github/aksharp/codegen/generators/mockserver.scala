package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.mockserverData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mockserver(implicit val engine: TemplateEngine,
                  val descriptorImplicits: DescriptorImplicits
                ) extends MustacheTemplateBase[mockserverData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): mockserverData = {
    val services = DomainService.toServices(fileDesc)
    mockserverData(
      port = "9090",
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getPackage,
      services = services,
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
