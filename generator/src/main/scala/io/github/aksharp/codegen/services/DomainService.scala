package io.github.aksharp.codegen.services

import io.github.aksharp.codegen.domain._
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.{FieldDescriptor, FileDescriptor}
import io.github.aksharp.codegen.domain

import scala.jdk.CollectionConverters._

object DomainService {

  val reservedFieldNames = List("type")

  def toServiceMethods(fileDesc: FileDescriptor): List[ServiceMethod] = {
    fileDesc
      .getServices
      .asScala
      .foldLeft(List.empty[ServiceMethod]) {
        (acc, s) =>
          acc :+
            ServiceMethod(
              serviceTypeName = s.getName,
              methods = toMethods(s)
            )
      }
  }

  def toMethods(service: Descriptors.ServiceDescriptor): List[Method] = {
    service
      .getMethods
      .asScala
      .map(
        m => Method(
          methodName = s"${m.getName.head.toLower}${m.getName.tail}",
          methodInputType = m.getInputType.getName,
          methodOutputType = m.getOutputType.getName
        )
      ).toList
  }

  def getMessagesWithoutFields(fileDesc: FileDescriptor): List[Message] = {
    fileDesc
      .getMessageTypes
      .asScala
      .map(message =>
        Message(
          messageTypeName = message.getName,
          fields = List.empty,
          isOneOf = message.getRealOneofs != null && message.getRealOneofs.asScala.nonEmpty
        )
      )
      .toList
      .distinct
  }

  def toMessages(service: Descriptors.ServiceDescriptor): List[Message] = {
    service
      .getMethods
      .asScala
      .flatMap(
        method =>
          List(method.getInputType, method.getOutputType)
            .flatMap(toMessageR)
      )
      .toList
  }

  def toMessageR(message: Descriptors.Descriptor): Set[Message] = {
    val isOneOffMessage = message.getRealOneofs != null && message.getRealOneofs.asScala.nonEmpty

    val innerMessage: Set[Message] =
      message
        .getFields
        .asScala
        .filter(_.getJavaType == JavaType.MESSAGE)
        .map(_.getMessageType)
        .toSet
        .flatMap(toMessageR)

    val fields: List[Field] =
      message
        .getFields
        .asScala
        .map(
          field => {
            val scalaType = toScalaType(field)
            val fieldName = s"${field.getName.head.toLower}${field.getName.tail}"

            Field(
              fieldName = if (reservedFieldNames.contains(fieldName)) s"`$fieldName`" else fieldName,
              fieldTypeName = scalaType,
              fieldGenerator = toFieldGenerator(scalaType, field),
              fieldForExpressionGenerator = toFieldForExpressionGenerator(scalaType, field),
              fieldNameOrOptionalOrSeq = toFieldNameForAssignment(field)
            )
          }
        ).toList

    innerMessage + Message(
      messageTypeName = message.getName,
      fields = withSeparator(fields),
      isOneOf = isOneOffMessage
    )

  }

  def toFieldNameForAssignment(field: FieldDescriptor): String = {
    val fieldName = s"${field.getName.head.toLower}${field.getName.tail}"
    if (field.getJavaType == JavaType.MESSAGE) {
      if (field.isRepeated)
        s"Seq($fieldName)"
      else {
        val isOneOf: Boolean = field.getMessageType.getRealOneofs != null && field.getMessageType.getRealOneofs.asScala.map(_.getName).toList.nonEmpty
        if (isOneOf)
          fieldName
        else if (field.isOptional)
          s"Option($fieldName)"
        else
          fieldName
      }
    } else {
      fieldName
    }
  }

  private def toQualifiedCommaSeparatedListOfEnumValues(fileDescriptor: Descriptors.FieldDescriptor): String = {
    fileDescriptor.getEnumType.toProto.getValueList.asScala.map(_.getName).map(v => s"${fileDescriptor.getEnumType.getName}.$v" ).mkString(", ")
  }

  def toFieldGenerator(
                        scalaType: String,
                        fileDescriptor: Descriptors.FieldDescriptor
                      ): String = {
    val generators = Map(
      "String" -> "Gen.alphaNumStr.sample.get",
      "Boolean" -> "Gen.oneOf(Seq(true, false)).sample.get",
      "Double" -> "Gen.choose(min = Double.MinValue, max = Double.MaxValue).sample.get",
      "Float" -> "Gen.choose(min = Float.MinValue, max = Float.MaxValue).sample.get",
      "Int" -> "Gen.choose(min = Int.MinValue, max = Int.MaxValue).sample.get",
      "Long" -> "Gen.choose(min = Long.MinValue, max = Long.MaxValue).sample.get",

      "Option[String]" -> "Gen.option(Gen.alphaNumStr).sample.get",
      "Option[Boolean]" -> "Gen.option(Gen.oneOf(Seq(true, false))).sample.get",
      "Option[Double]" -> "Gen.option(Gen.choose(min = Double.MinValue, max = Double.MaxValue)).sample.get",
      "Option[Float]" -> "Gen.option(Gen.choose(min = Float.MinValue, max = Float.MaxValue)).sample.get",
      "Option[Int]" -> "Gen.option(Gen.choose(min = Int.MinValue, max = Int.MaxValue)).sample.get",
      "Option[Long]" -> "Gen.option(Gen.choose(min = Long.MinValue, max = Long.MaxValue)).sample.get",

      "Array[Byte]" -> "Gen.alphaNumStr.sample.get.getBytes"
    )
    generators
      .getOrElse(
      scalaType,

        if (fileDescriptor.getJavaType == JavaType.ENUM) {
          s"""Gen.oneOf(${toQualifiedCommaSeparatedListOfEnumValues(fileDescriptor)}).sample.get"""
        } else {
          s"a${scalaType.replace("[", "").replace("]", "")}()"
        }
    )
  }

  def toFieldForExpressionGenerator(
                                     scalaType: String,
                                     fileDescriptor: Descriptors.FieldDescriptor
                                   ): String = {
    val generators = Map(
      "String" -> "Gen.alphaNumStr",
      "Boolean" -> "Gen.oneOf(Seq(true, false))",
      "Double" -> "Gen.choose(min = Double.MinValue, max = Double.MaxValue)",
      "Float" -> "Gen.choose(min = Float.MinValue, max = Float.MaxValue)",
      "Int" -> "Gen.choose(min = Int.MinValue, max = Int.MaxValue)",
      "Long" -> "Gen.choose(min = Long.MinValue, max = Long.MaxValue)",

      "Option[String]" -> "Gen.option(Gen.alphaNumStr)",
      "Option[Boolean]" -> "Gen.option(Gen.oneOf(Seq(true, false)))",
      "Option[Double]" -> "Gen.option(Gen.choose(min = Double.MinValue, max = Double.MaxValue))",
      "Option[Float]" -> "Gen.option(Gen.choose(min = Float.MinValue, max = Float.MaxValue))",
      "Option[Int]" -> "Gen.option(Gen.choose(min = Int.MinValue, max = Int.MaxValue))",
      "Option[Long]" -> "Gen.option(Gen.choose(min = Long.MinValue, max = Long.MaxValue))",
      
      "Array[Byte]" -> "Gen.alphaNumStr.map(_.getBytes)"
    )
    generators.getOrElse(
      scalaType,
      if (fileDescriptor.getJavaType == JavaType.ENUM) {
        s"""Gen.oneOf(${toQualifiedCommaSeparatedListOfEnumValues(fileDescriptor)})"""
      } else {
        s"${scalaType.replace("[","").replace("]","")}Gen()"
      }
    )
  }


  def toScalaType(fileDescriptor: Descriptors.FieldDescriptor): String = {

    val m = Map[JavaType, String](
      JavaType.BOOLEAN -> "Boolean",
      JavaType.BYTE_STRING -> "Array[Byte]",
      JavaType.DOUBLE -> "Double",
      JavaType.FLOAT -> "Float",
      JavaType.INT -> "Int",
      JavaType.LONG -> "Long",
      JavaType.STRING -> "String"
    )

    fileDescriptor.getJavaType match {
      case JavaType.MESSAGE => fileDescriptor.toProto.getTypeName.split("\\.").last
      //fileDescriptor.getEnumType.toProto.getValueList.asScala.map(_.getName).mkString(",") //
      case JavaType.ENUM => fileDescriptor.getEnumType.getName // "JavaType.ENUM is not yet supported"
      case javaType if fileDescriptor.hasOptionalKeyword => m.get(javaType).map(t => s"Option[$t]").getOrElse(s"Could not find match for JavaType: ${javaType.toString}")
      case javaType => m.getOrElse(javaType, s"Could not find match for JavaType: ${javaType.toString}")
    }
  }

  def toImports(fileDesc: FileDescriptor): List[ImportExt] = {
    fileDesc
      .getDependencies.asScala
      .map(
        d =>
          ImportExt(
            fqdnImport = d.getPackage
          )
      )
      .toList
  }

  def toServicesExt(fileDesc: FileDescriptor): List[ServiceExt] = {
    fileDesc
      .getServices
      .asScala
      .foldLeft(List.empty[ServiceExt]) {
        (acc, s) =>
          acc :+
            domain.ServiceExt(
              serviceName = s"${s.getName.head.toLower}${s.getName.tail}",
              serviceTypeName = s.getName,
              serviceMethods = withSeparator(toMethods(s)),
              messages = toMessages(s)
            )
      }
  }

  def toServices(fileDesc: FileDescriptor): List[Service] = {
    fileDesc
      .getServices
      .asScala
      .foldLeft(List.empty[Service]) {
        (acc, s) =>
          acc :+
            Service(
              serviceName = s"${s.getName.head.toLower}${s.getName.tail}",
              serviceTypeName = s.getName
            )
      }
  }

  case class WithSeparator[A](
                               value: A,
                               separator: String
                             )

  def withSeparator[A](
                        list: List[A],
                        separator: String = ","
                      ): List[WithSeparator[A]] = {
    list match {
      case Nil => Nil
      case head :: Nil => List(WithSeparator(head, ""))
      case other => other.init.map(s => WithSeparator(s, separator)) :+ WithSeparator(other.last, "")
    }
  }

}
