package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.serverData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class server(
              port: Int
            )(implicit val engine: TemplateEngine,
              val descriptorImplicits: DescriptorImplicits
            ) extends MustacheTemplateBase[serverData] {

  override def getTemplateData(fileDesc: FileDescriptor): serverData = {
    val services = DomainService.toServices(fileDesc)
    serverData(
      port = port.toString,
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = services,
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
