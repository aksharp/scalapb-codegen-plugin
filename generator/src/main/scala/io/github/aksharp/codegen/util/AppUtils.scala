package io.github.aksharp.codegen.util

trait AppUtils {

  def toPackageWithFileName(
                             packageName: String,
                             fileName: String
                           ): String = {
    s"${packageName}.${fileName.replace(".proto","Api")}"
  }

}
