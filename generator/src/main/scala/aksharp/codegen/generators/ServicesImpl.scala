package aksharp.codegen.generators

import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import com.google.protobuf.Descriptors.FileDescriptor
import aksharp.codegen.domain.implData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
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
