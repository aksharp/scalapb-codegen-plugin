package io.nomadic.codegen.domain

case class Field(
                  fieldName: String,
                  fieldTypeName: String,
                  fieldGenerator: String,
                  fieldNameOrOptionFieldName: String
                )
