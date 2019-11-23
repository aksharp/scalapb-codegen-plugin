package io.nomadic.codegen.services

import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.{Field, Message, Method, Service, ServiceExt, ServiceMethod}

import scala.collection.JavaConverters._

object DomainService {

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


  //  def toMethods(
  //                 serviceTypeName: String,
  //                 fileDesc: FileDescriptor
  //               ): List[Method] = {
  //    fileDesc
  //      .getServices
  //      .asScala
  //      .find(_.getName == serviceTypeName)
  //      .map(_.getMethods.asScala.map(m => Method(
  //        methodName = s"${m.getName.head.toLower}${m.getName.tail}",
  //        inputType = m.getInputType.getName,
  //        outputType = m.getOutputType.getName
  //      )).toList).getOrElse(List.empty)
  //  }

  def toMethods(service: Descriptors.ServiceDescriptor): List[Method] = {
    service
      .getMethods
      .asScala
      .map(m => Method(
        methodName = s"${m.getName.head.toLower}${m.getName.tail}",
        methodInputType = m.getInputType.getName,
        methodOutputType = m.getOutputType.getName
      )).toList
  }

  def toMessages(service: Descriptors.ServiceDescriptor): List[Message] = {
    service
      .getMethods
      .asScala
      .flatMap(method =>
        List(method.getInputType, method.getOutputType)
          .flatMap(toMessageR))
      .toList
  }

  def toMessageR(message: Descriptors.Descriptor): Set[Message] = {
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
        .map(field => {
          val scalaType = toScalaType(field)
          val fieldName = s"${field.getName.head.toLower}${field.getName.tail}"
          Field(
            fieldName = fieldName,
            fieldTypeName = scalaType,
            fieldGenerator = toFieldGenerator(scalaType),
            fieldForExpressionGenerator = toFieldForExpressionGenerator(scalaType),
            fieldNameOrOptionFieldName = if (field.getJavaType == JavaType.MESSAGE) s"Option($fieldName)" else fieldName
          )
        }).toList

    innerMessage + Message(
      messageTypeName = message.getName,
      fields = withSeparator(fields)
    )

  }

  def toFieldGenerator(
                        scalaType: String
                      ): String = {
    val generators = Map(
      "String" -> "Gen.alphaNumStr.sample.get",
      "Boolean" -> "Gen.oneOf(Seq(true, false)).sample.get",
      "Double" -> "Gen.choose(min = Double.MinValue, max = Double.MaxValue).sample.get",
      "Float" -> "Gen.choose(min = Float.MinValue, max = Float.MaxValue).sample.get",
      "Int" -> "Gen.choose(min = Int.MinValue, max = Int.MaxValue).sample.get",
      "Long" -> "Gen.choose(min = Long.MinValue, max = Long.MaxValue).sample.get",
      "Array[Byte]" -> "Gen.alphaNumStr.sample.get.getBytes"
    )
    generators.getOrElse(
      scalaType,
      s"a$scalaType()"
    )
  }

  def toFieldForExpressionGenerator(
                                     scalaType: String
                                   ): String = {
    val generators = Map(
      "String" -> "Gen.alphaNumStr",
      "Boolean" -> "Gen.oneOf(Seq(true, false))",
      "Double" -> "Gen.choose(min = Double.MinValue, max = Double.MaxValue)",
      "Float" -> "Gen.choose(min = Float.MinValue, max = Float.MaxValue)",
      "Int" -> "Gen.choose(min = Int.MinValue, max = Int.MaxValue)",
      "Long" -> "Gen.choose(min = Long.MinValue, max = Long.MaxValue)",
      "Array[Byte]" -> "Gen.alphaNumStr.map(_.getBytes)"
    )
    generators.getOrElse(
      scalaType,
      s"${scalaType}Gen()"
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
      case JavaType.ENUM => "JavaType.ENUM is not yet supported"
      case javaType => m.getOrElse(javaType, s"Could not find match for JavaType: ${javaType.toString}")
    }
  }


  def toServicesExt(fileDesc: FileDescriptor): List[ServiceExt] = {
    fileDesc
      .getServices
      .asScala
      .foldLeft(List.empty[ServiceExt]) {
        (acc, s) =>
          acc :+
            ServiceExt(
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


  //  abstract class Separated[A] {
  //    val value: A
  //    val separator: String
  //  }
  //  case class ValueWithSeparator[A](value: A, separator: String = ",") extends Separated[A]
  //  case class ValueWithoutSeparator[A](value: A) extends Separated[A] { val separator: String = "" }
  //
  case class WithSeparator[A](value: A, separator: String)

  def toServicesAsArguments(services: List[Service]): List[WithSeparator[Service]] = {
    withSeparator(services)
    //    services match {
    //      case Nil => Nil
    //      case head :: Nil => List(WithSeparator(head, ""))
    //      case other => other.init.map(s => WithSeparator(s, ",")) :+ WithSeparator(other.last, "")
    //    }
  }

  def withSeparator[A](list: List[A], separator: String = ","): List[WithSeparator[A]] = {
    list match {
      case Nil => Nil
      case head :: Nil => List(WithSeparator(head, ""))
      case other => other.init.map(s => WithSeparator(s, separator)) :+ WithSeparator(other.last, "")
    }
  }

}
