package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors
import io.github.aksharp.codegen.domain.mockclientData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mockclient(implicit val engine: TemplateEngine,
                      val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[mockclientData] with AppUtils {
  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): mockclientData = {
    val services = DomainService.toServices(fileDesc)
    mockclientData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getPackage,
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }
}
