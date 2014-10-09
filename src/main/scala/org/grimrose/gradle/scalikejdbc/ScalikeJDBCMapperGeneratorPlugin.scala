package org.grimrose.gradle.scalikejdbc

import org.gradle.api.{Plugin, Project, Task}

import scala.collection.JavaConverters._
import scala.language.existentials

class ScalikeJDBCMapperGeneratorPlugin extends Plugin[Project] {

  val group = "ScalikeJDBC Mapper Generator"

  var project: Project = _

  override def apply(project: Project) = {
    this.project = project
    // scalikejdbc-gen
    makeTask[GenTask]("scalikejdbcGen") { task =>
      task.setDescription("Generates a model for a specified table")

      task.setSrcDir(project.file("src/main/scala"))
      task.setTestDir(project.file("src/test/scala"))

      task.setTableName(findProperty("tableName"))
      task.setClassName(findProperty("className"))
    }

    // scalikejdbc-gen-force
    makeTask[GenForceTask]("scalikejdbcGenForce") { task =>
      task.setDescription("Generates and overwrites a model for a specified table")

      task.setSrcDir(project.file("src/main/scala"))
      task.setTestDir(project.file("src/test/scala"))

      task.setTableName(findProperty("tableName"))
      task.setClassName(findProperty("className"))
    }

    // scalikejdbc-gen-all
    makeTask[GenAllTask]("scalikejdbcGenAll") { task =>
      task.setDescription("Generates models for all tables")

      task.setSrcDir(project.file("src/main/scala"))
      task.setTestDir(project.file("src/test/scala"))
    }

    // scalikejdbc-gen-all-force
    makeTask[GenAllForceTask]("scalikejdbcGenAllForce") { task =>
      task.setDescription("Generates and overwrites models for all tables")

      task.setSrcDir(project.file("src/main/scala"))
      task.setTestDir(project.file("src/test/scala"))
    }

    // scalikejdbc-gen-echo
    makeTask[GenEchoTask]("scalikejdbcGenEcho") { task =>
      task.setDescription("Prints a model for a specified table")

      task.setSrcDir(project.file("src/main/scala"))
      task.setTestDir(project.file("src/test/scala"))

      task.setTableName(findProperty("tableName"))
      task.setClassName(findProperty("className"))
    }
  }

  def findProperty(key: String) : String = findProperty(this.project, key) match {
    case Some(x) => x.toString
    case None => null
  }

  def findProperty(p: Project, key: String): Option[Any] = p.getProperties.asScala.get(key)

  def makeTask[T <: Task](name: String)(configure: (T) => Unit)(implicit m: Manifest[T]): T = makeTask[T](this.project, name)(configure)(m)

  def makeTask[T <: Task](project: Project, name: String)(configure: (T) => Unit)(implicit m: Manifest[T]): T = {
    val map = Map("type" -> m.runtimeClass)
    val t = project.task(map.asJava, name).asInstanceOf[T]
    t.setGroup(group)
    configure(t)
    t
  }

}
