package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.ServiceData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class services(
                implicit val engine: TemplateEngine,
                val descriptorImplicits: DescriptorImplicits
              ) extends MustacheTemplateBase[ServiceData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): ServiceData = {
    //    val services = DomainService.toServices(fileDesc)
    ServiceData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc)
    )
  }

}
