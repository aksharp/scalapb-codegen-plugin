package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors
import io.nomadic.codegen.domain.mocksData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class mocks(implicit val engine: TemplateEngine,
                 val descriptorImplicits: DescriptorImplicits
                  ) extends MustacheTemplateBase[mocksData] {

  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): mocksData = {
    val services = DomainService.toServicesExt(fileDesc)
    mocksData(
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = services,
      messages = services.flatMap(_.messages).distinct
    )
  }
}
