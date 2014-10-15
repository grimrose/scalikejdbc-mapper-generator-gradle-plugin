package org.grimrose.gradle.scalikejdbc.tasks

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.{OutputDirectory, TaskAction}
import org.grimrose.gradle.scalikejdbc.ScalikeJDBCMapperGeneratorAdopter

class GenAllForceTask extends DefaultTask {

  @OutputDirectory
  var srcDir: File = _

  @OutputDirectory
  var testDir: File = _

  @TaskAction
  def process() = {
    val adopter = new ScalikeJDBCMapperGeneratorAdopter(getProject)

    adopter.allGenerators(srcDir, testDir).foreach { g =>
      g.writeModel()
      g.writeSpec(g.specAll())
    }
  }

  def getSrcDir = this.srcDir

  def setSrcDir(srcDir: File) = this.srcDir = srcDir

  def getTestDir = this.testDir

  def setTestDir(testDir: File) = this.testDir = testDir
}
