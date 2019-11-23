package aksharp.codegen.util

import java.io.File

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import org.fusesource.scalate.{TemplateEngine, TemplateSource}
import scalapb.compiler.DescriptorImplicits

trait MustacheTemplateBase[A] {
  val rootElementName: String = "root"
  lazy val name: String = getClass.getSimpleName
  val templateName: String = s"$name.mustache"
  val engine: TemplateEngine
  val descriptorImplicits: DescriptorImplicits

  def getTemplateData(fileDesc: FileDescriptor): A

  def generateFile(fileDesc: FileDescriptor): CodeGeneratorResponse.File = {
    import descriptorImplicits._
    val b = CodeGeneratorResponse.File.newBuilder()
    b.setName(s"${fileDesc.scalaDirectory}/${name}.scala")


    val templateFile = new File(s"generator/templates/$templateName")

    val content: String = engine.layout(
      source = TemplateSource.fromFile(templateFile),
      attributes = Map[String, Any](rootElementName -> getTemplateData(fileDesc))
    )

    b.setContent(content)
    b.build()
  }


}
