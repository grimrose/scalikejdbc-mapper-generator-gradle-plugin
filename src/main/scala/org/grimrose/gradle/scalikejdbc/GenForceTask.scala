package org.grimrose.gradle.scalikejdbc

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.{Input, Optional, OutputDirectory, TaskAction}

class GenForceTask extends DefaultTask {

  @OutputDirectory
  var srcDir: File = _

  @OutputDirectory
  var testDir: File = _

  @Input
  var tableName: String = _

  @Input
  @Optional
  var className: String = _

  @TaskAction
  def process() = {
    val adopter = new ScalikeJDBCMapperGeneratorAdopter(getProject)

    val gen = adopter.loadGenerator(getTableName, Option(getClassName), srcDir, testDir)
    gen.foreach { g =>
      g.writeModel()
      g.writeSpec(g.specAll())
    }
  }

  def getTableName = this.tableName

  def setTableName(tableName: String) = this.tableName = tableName

  def getClassName = this.className

  def setClassName(className: String) = this.className = className

  def getSrcDir = this.srcDir

  def setSrcDir(srcDir: File) = this.srcDir = srcDir

  def getTestDir = this.testDir

  def setTestDir(testDir: File) = this.testDir = testDir
}
