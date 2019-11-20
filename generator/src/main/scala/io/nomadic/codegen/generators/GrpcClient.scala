package io.nomadic.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.GrpcClientData
import io.nomadic.codegen.services.DomainService
import io.nomadic.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

case class GrpcClient(implicit val engine: TemplateEngine,
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
