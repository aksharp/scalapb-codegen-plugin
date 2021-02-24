package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors
import io.github.aksharp.codegen.domain.{Message, SerdeData, ServiceExt}
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class serde(implicit val engine: TemplateEngine,
            val descriptorImplicits: DescriptorImplicits
                      ) extends MustacheTemplateBase[SerdeData] with AppUtils {

  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): SerdeData = {
    val allMessages: List[Message] = DomainService.getNonOneOfMessagesWithoutFields(fileDesc)
    SerdeData(
      basePackageName = fileDesc.getPackage,
      javaPackage = toPackageWithFileName(
        packageName = fileDesc.getPackage,
        fileName = fileDesc.getName
      ),
      messages =
        allMessages
        .filterNot(_.isOneOf)
    )
  }
}
