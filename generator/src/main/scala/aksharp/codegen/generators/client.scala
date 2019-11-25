package aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import aksharp.codegen.domain.clientData
import aksharp.codegen.services.DomainService
import aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class client(
              port: Int,
              host: String
            )(implicit val engine: TemplateEngine,
              val descriptorImplicits: DescriptorImplicits
            ) extends MustacheTemplateBase[clientData] {

  override def getTemplateData(fileDesc: FileDescriptor): clientData = {
    clientData(
      port = port.toString,
      host = host,
      negotiationType = "NegotiationType.PLAINTEXT",
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = DomainService.toServices(fileDesc)
    )
  }

}
