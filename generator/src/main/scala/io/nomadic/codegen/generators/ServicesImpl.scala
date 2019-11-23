package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.implData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class ServicesImpl(implicit val engine: TemplateEngine,
                        val descriptorImplicits: DescriptorImplicits
                   ) extends MustacheTemplateBase[implData] {

  override def getTemplateData(fileDesc: FileDescriptor): implData = {
    implData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      serviceMethods = DomainService.toServiceMethods(fileDesc)
    )
  }

}
