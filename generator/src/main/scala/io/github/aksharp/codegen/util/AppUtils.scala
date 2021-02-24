package io.github.aksharp.codegen.util

import io.github.aksharp.codegen.domain.Message
import io.github.aksharp.codegen.services.DomainService.reservedFieldNames

trait AppUtils {

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

}
