package org.grimrose.gradle.scalikejdbc

import org.gradle.api.{InvalidUserDataException, Project}
import scalikejdbc.mapper.ScalikeJDBCMapperGenerator

import scala.util.control.Exception._


class ScalikeJDBCMapperGeneratorAdopter(project: Project) {

  val generator = new ScalikeJDBCMapperGenerator

  def loadSettings() = {
    val path = targetOrDefaultDirectory("project", "project").getAbsolutePath
    generator.loadSettings(path)
  }

  def loadGenerator(taskName: String, tableName: String, className: Option[String], srcDir: AnyRef, testDir: AnyRef) = {
    if (Option(tableName).getOrElse("").isEmpty) {
      val log = project.getLogger
      log.error(s"Not a valid command: $taskName")
      log.error(s"Usage: $taskName -PtableName=table_name (-PclassName=class_name)")
      throw new InvalidUserDataException(s"$taskName: tableName is empty.")
    }

    val s = targetOrDefaultDirectory(srcDir, "src/main/scala")
    val t = targetOrDefaultDirectory(testDir, "src/test/scala")

    generator.generator(tableName, className, s, t, loadSettings._1, loadSettings._2)
  }

  def allGenerators(srcDir: AnyRef, testDir: AnyRef) = {
    val s = targetOrDefaultDirectory(srcDir, "src/main/scala")
    val t = targetOrDefaultDirectory(testDir, "src/test/scala")

    generator.allGenerators(s, t, loadSettings._1, loadSettings._2)
  }

  def targetOrDefaultDirectory(target: AnyRef, defaultPath: String) = allCatch.opt(project.file(target)).getOrElse(project.file(defaultPath))

}

object ScalikeJDBCMapperGeneratorAdopter {
  def apply(project: Project) = new ScalikeJDBCMapperGeneratorAdopter(project)
}
