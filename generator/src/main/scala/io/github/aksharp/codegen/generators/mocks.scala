package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors
import io.github.aksharp.codegen.domain
import io.github.aksharp.codegen.services.DomainService.reservedFieldNames
import io.github.aksharp.codegen.domain.{Message, ServiceExt, mocksData}
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mocks(
             implicit val engine: TemplateEngine,
             val descriptorImplicits: DescriptorImplicits
           ) extends MustacheTemplateBase[mocksData] with AppUtils {




  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): mocksData = {
    val services: List[ServiceExt] = DomainService.toServicesExt(fileDesc)
    domain.mocksData(
      basePackageName = fileDesc.getPackage,
      services = services,
      allMessages = services.flatMap(serviceExt => updateFieldNames(serviceExt.messages)).distinct,
      allImports = DomainService.toImports(fileDesc)
    )
  }
}
