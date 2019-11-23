package aksharp.codegen.domain

case class Field(
                  fieldName: String,
                  fieldTypeName: String,
                  fieldGenerator: String,
                  fieldForExpressionGenerator: String,
                  fieldNameOrOptionalOrSeq: String
                )
