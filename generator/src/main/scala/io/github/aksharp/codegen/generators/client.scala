package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.clientData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class client(
              port: Int
            )(implicit val engine: TemplateEngine,
              val descriptorImplicits: DescriptorImplicits
            ) extends MustacheTemplateBase[clientData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): clientData = {
    clientData(
      port = port.toString,
      negotiationType = "NegotiationType.PLAINTEXT",
      basePackageName = fileDesc.getPackage,
      services = DomainService.toServices(fileDesc)
    )
  }

}
