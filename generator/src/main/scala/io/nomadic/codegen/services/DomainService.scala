package io.nomadic.codegen.services

import com.google.protobuf.Descriptors.FileDescriptor
import io.nomadic.codegen.domain.Service
import scala.collection.JavaConverters._

object DomainService {

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
    services match {
      case Nil => Nil
      case head :: Nil => List(WithSeparator(head, ""))
      case other => other.init.map(s => WithSeparator(s, ",")) :+ WithSeparator(other.last, "")
    }
  }

}
