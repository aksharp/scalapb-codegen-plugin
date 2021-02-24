package io.github.aksharp.codegen.generators

import com.google.protobuf.Descriptors
import io.github.aksharp.codegen.domain
import io.github.aksharp.codegen.services.DomainService.reservedFieldNames
import io.github.aksharp.codegen.domain.{Message, ServiceExt, mocksData}
import io.github.aksharp.codegen.services.DomainService
import io.github.aksharp.codegen.util.{AppUtils, MustacheTemplateBase}
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mocks(
             implicit val engine: TemplateEngine,
             val descriptorImplicits: DescriptorImplicits
           ) extends MustacheTemplateBase[mocksData] with AppUtils {

  def updateFieldNames(messages: List[Message]): List[Message] = {

    //TODO: maybe finally use scala lenses for this. Also optimize not transforming if not needed
    val updatedMessages: List[Message] =
      messages.map(
        message =>
          message.copy(
            fields = message.fields.map(
              field =>
                field.copy(
                  value = field.value.copy(
                    fieldName =
                      if (reservedFieldNames.contains(field.value.fieldName)) {
                        s"`${field.value.fieldName}`"
                      } else {
                        field.value.fieldName
                      },
                    fieldNameOrOptionalOrSeq =
                      if (reservedFieldNames.contains(field.value.fieldNameOrOptionalOrSeq)) {
                        s"`${field.value.fieldNameOrOptionalOrSeq}`"
                      } else {
                        field.value.fieldNameOrOptionalOrSeq
                      }
                  )
                )
            )
          )
      )
    updatedMessages
  }


  override def getTemplateData(fileDesc: Descriptors.FileDescriptor): mocksData = {
    val services: List[ServiceExt] = DomainService.toServicesExt(fileDesc)
    domain.mocksData(
      basePackageName = fileDesc.getPackage,
      javaPackage = toPackageWithFileName(
        packageName = fileDesc.getPackage,
        fileName = fileDesc.getName
      ),
      services = services,
      allMessages = services.flatMap(serviceExt => updateFieldNames(serviceExt.messages)).distinct,
      allImports = DomainService.toImports(fileDesc)
    )
  }
}