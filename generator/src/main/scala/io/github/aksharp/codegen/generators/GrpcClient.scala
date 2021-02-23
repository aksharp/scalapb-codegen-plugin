package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.GrpcClientData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class GrpcClient(implicit val engine: TemplateEngine,
                      val descriptorImplicits: DescriptorImplicits
                     ) extends MustacheTemplateBase[GrpcClientData] {

  override def getTemplateData(fileDesc: FileDescriptor): GrpcClientData = {
    GrpcClientData(
      basePackageName = fileDesc.getPackage,
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = DomainService.toServices(fileDesc)
    )
  }

}
