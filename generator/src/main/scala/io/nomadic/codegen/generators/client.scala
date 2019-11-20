package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.clientData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class client(
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
