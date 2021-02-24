package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors.FileDescriptor
import io.github.aksharp.codegen.domain.GrpcClientData
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class GrpcClient(
                  implicit val engine: TemplateEngine,
                  val descriptorImplicits: DescriptorImplicits
                ) extends MustacheTemplateBase[GrpcClientData] with AppUtils {

  override def getTemplateData(fileDesc: FileDescriptor): GrpcClientData = {
    GrpcClientData(
      basePackageName = fileDesc.getPackage,
      javaPackage = toPackageWithFileName(
        packageName = fileDesc.getPackage,
        fileName = fileDesc.getName
      ),
      services = DomainService.toServices(fileDesc)
    )
  }

}
