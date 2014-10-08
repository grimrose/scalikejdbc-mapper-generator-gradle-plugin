package org.grimrose.gradle.scalikejdbc

import org.gradle.testfixtures.ProjectBuilder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class ScalikeJDBCMapperGeneratorPluginSpec extends FlatSpec with Matchers {

  "GenTask" should "be applied" in {
    val project = ProjectBuilder.builder().build()

    project.apply(Map("plugin" -> "org.grimrose.gradle.scalikejdbc"))

    val task = project.getTasks.findByName("scalikejdbcGen")
    task should not(be(null))
    task.getDescription should be("Generates a model for a specified table")

    task.hasProperty("tableName") should be(true)
    task.hasProperty("className") should be(true)

    task.setProperty("tableName", "gen_task")

    task.property("tableName").toString should be("gen_task")
    task.property("className") should be(null)
  }

  "GenForceTask" should "be applied" in {
    val project = ProjectBuilder.builder().build()

    project.apply(Map("plugin" -> "org.grimrose.gradle.scalikejdbc"))

    val task = project.getTasks.findByName("scalikejdbcGenForce")
    task should not(be(null))
    task.getDescription should be("Generates and overwrites a model for a specified table")

    task.hasProperty("tableName") should be(true)
    task.hasProperty("className") should be(true)

    task.setProperty("tableName", "gen_force_task")
    task.setProperty("className", "GenForceTask")

    task.property("tableName").toString should be("gen_force_task")
    task.property("className").toString should be("GenForceTask")
  }

  "GenAllTask" should "be applied" in {
    val project = ProjectBuilder.builder().build()

    project.apply(Map("plugin" -> "org.grimrose.gradle.scalikejdbc"))

    val task = project.getTasks.findByName("scalikejdbcGenAll")
    task should not(be(null))
    task.getDescription should be("Generates models for all tables")

    task.hasProperty("tableName") should be(false)
    task.hasProperty("className") should be(false)
  }

  "GenAllForceTask" should "be applied" in {
    val project = ProjectBuilder.builder().build()

    project.apply(Map("plugin" -> "org.grimrose.gradle.scalikejdbc"))

    val task = project.getTasks.findByName("scalikejdbcGenAllForce")
    task should not(be(null))
    task.getDescription should be("Generates and overwrites models for all tables")

    task.hasProperty("tableName") should be(false)
    task.hasProperty("className") should be(false)
  }

  "GenEchoTask" should "be applied" in {
    val project = ProjectBuilder.builder().build()
    project.apply(Map("plugin" -> "org.grimrose.gradle.scalikejdbc"))

    val task = project.getTasks.findByPath("scalikejdbcGenEcho")
    task should not(be(null))
    task.getDescription should be("Prints a model for a specified table")

    task.hasProperty("tableName") should be(true)
    task.hasProperty("className") should be(true)

    task.setProperty("tableName", "gen_echo_task")

    task.property("tableName").toString should be("gen_echo_task")
    task.property("className") should be(null)
  }

}
