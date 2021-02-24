package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.serverData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class server(
              port: Int
            )(implicit val engine: TemplateEngine,
              val descriptorImplicits: DescriptorImplicits
            ) extends MustacheTemplateBase[serverData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): serverData = {
    val services = DomainService.toServices(fileDesc)
    serverData(
      port = port.toString,
      basePackageName = fileDesc.getPackage,
      javaPackage = toPackageWithFileName(
        packageName = fileDesc.getPackage,
        fileName = fileDesc.getName
      ),
      services = services,
      servicesAsArguments = DomainService.withSeparator(services)
    )
  }

}
