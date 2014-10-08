package org.grimrose.gradle.scalikejdbc

import org.gradle.api.Project
import scalikejdbc.mapper.ScalikeJDBCMapperGenerator

import scala.util.control.Exception._


class ScalikeJDBCMapperGeneratorAdopter(val project: Project) {

  val generator = new ScalikeJDBCMapperGenerator

  def loadSettings() = {
    val path = targetOrDefaultDirectory("project", "project").getAbsolutePath
    generator.loadSettings(path)
  }

  def loadGenerator(tableName: String, className: Option[String], srcDir: AnyRef, testDir: AnyRef) = {
    val s = targetOrDefaultDirectory(srcDir, "src/main/scala")
    val t = targetOrDefaultDirectory(testDir, "src/test/scala")

    // TODO validate tableName

    generator.generator(tableName, className, s, t, loadSettings._1, loadSettings._2)
  }

  def allGenerators(srcDir: AnyRef, testDir: AnyRef) = {
    val s = targetOrDefaultDirectory(srcDir, "src/main/scala")
    val t = targetOrDefaultDirectory(testDir, "src/test/scala")

    generator.allGenerators(s, t, loadSettings._1, loadSettings._2)
  }

  def targetOrDefaultDirectory(target: AnyRef, defaultPath: String) = allCatch.opt(project.file(target)).getOrElse(project.file(defaultPath))

}
