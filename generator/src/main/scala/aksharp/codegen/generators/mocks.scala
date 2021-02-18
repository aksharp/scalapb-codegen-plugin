package aksharp.codegen.generators

import com.google.protobuf.Descriptors
import aksharp.codegen.domain.{Message, ServiceExt, mocksData}
import aksharp.codegen.services.DomainService
import aksharp.codegen.services.DomainService.reservedFieldNames
import aksharp.codegen.util.MustacheTemplateBase
import org.fusesource.scalate.TemplateEngine
import scalapb.compiler.DescriptorImplicits

class mocks(
             implicit val engine: TemplateEngine,
             val descriptorImplicits: DescriptorImplicits
           ) extends MustacheTemplateBase[mocksData] {

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
    mocksData(
      javaPackage = fileDesc.getOptions.getJavaPackage,
      services = services,
      allMessages = services.flatMap(serviceExt => updateFieldNames(serviceExt.messages)).distinct,
      allImports = DomainService.toImports(fileDesc)
    )
  }
}
