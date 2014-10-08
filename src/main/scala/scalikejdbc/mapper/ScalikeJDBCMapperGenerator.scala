/*
 * Copyright 2012 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package scalikejdbc.mapper

import java.io.{File, FileNotFoundException}
import java.util.Locale.{ENGLISH => en}

import scala.language.reflectiveCalls
import scala.util.control.Exception._

/**
 * ScalikeJDBC Mapper Generator
 *
 * @see scalikejdbc.mapper.SbtPlugin
 */
class ScalikeJDBCMapperGenerator {

  case class JDBCSettings(driver: String, url: String, username: String, password: String, schema: String)

  case class GeneratorSettings(packageName: String, template: String, testTemplate: String, lineBreak: String, caseClassOnly: Boolean, encoding: String, autoConstruct: Boolean, defaultAutoSession: Boolean)

  def loadSettings(projectDirectoryPath: String): (JDBCSettings, GeneratorSettings) = {
    val props = new java.util.Properties
    try {
      val file: File = new File(projectDirectoryPath, "scalikejdbc-mapper-generator.properties")
      println(file.getAbsolutePath)
      using(new java.io.FileInputStream(file)) {
        inputStream => props.load(inputStream)
      }
    } catch {
      case e: FileNotFoundException =>
    }
    if (props.isEmpty) {
      val file: File = new File(projectDirectoryPath, "scalikejdbc.properties")
      println(file.getAbsolutePath)
      using(new java.io.FileInputStream(file)) {
        inputStream => props.load(inputStream)
      }
    }
    val defaultConfig = GeneratorConfig()
    (JDBCSettings(
      driver = Option(props.get("jdbc.driver")).map(_.toString).getOrElse(throw new IllegalStateException("Add jdbc.driver to project/scalikejdbc-mapper-generator.properties")),
      url = Option(props.get("jdbc.url")).map(_.toString).getOrElse(throw new IllegalStateException("Add jdbc.url to project/scalikejdbc-mapper-generator.properties")),
      username = Option(props.get("jdbc.username")).map(_.toString).getOrElse(""),
      password = Option(props.get("jdbc.password")).map(_.toString).getOrElse(""),
      schema = Option(props.get("jdbc.schema")).map(_.toString).orNull[String]
    ), GeneratorSettings(
      packageName = Option(props.get("generator.packageName")).map(_.toString).getOrElse(defaultConfig.packageName),
      template = Option(props.get("generator.template")).map(_.toString).getOrElse(defaultConfig.template.name),
      testTemplate = Option(props.get("generator.testTemplate")).map(_.toString).getOrElse(GeneratorTestTemplate.specs2unit.name),
      lineBreak = Option(props.get("generator.lineBreak")).map(_.toString).getOrElse(defaultConfig.lineBreak.name),
      caseClassOnly = Option(props.get("generator.caseClassOnly")).map(_.toString.toBoolean).getOrElse(defaultConfig.caseClassOnly),
      encoding = Option(props.get("generator.encoding")).map(_.toString).getOrElse(defaultConfig.encoding),
      autoConstruct = Option(props.get("generator.autoConstruct")).map(_.toString.toBoolean).getOrElse(defaultConfig.autoConstruct),
      defaultAutoSession = Option(props.get("generator.defaultAutoSession")).map(_.toString.toBoolean).getOrElse(defaultConfig.defaultAutoSession)
    ))
  }

  private[this] def generatorConfig(srcDir: File, testDir: File, generatorSettings: GeneratorSettings) =
    GeneratorConfig(
      srcDir = srcDir.getAbsolutePath,
      testDir = testDir.getAbsolutePath,
      packageName = generatorSettings.packageName,
      template = GeneratorTemplate(generatorSettings.template),
      testTemplate = GeneratorTestTemplate(generatorSettings.testTemplate),
      lineBreak = LineBreak(generatorSettings.lineBreak),
      caseClassOnly = generatorSettings.caseClassOnly,
      encoding = generatorSettings.encoding,
      autoConstruct = generatorSettings.autoConstruct,
      defaultAutoSession = generatorSettings.defaultAutoSession
    )

  def generator(tableName: String, className: Option[String], srcDir: File, testDir: File, jdbc: JDBCSettings, generatorSettings: GeneratorSettings): Option[CodeGenerator] = {
    val config = generatorConfig(srcDir, testDir, generatorSettings)
    Class.forName(jdbc.driver) // load specified jdbc driver
    val model = Model(jdbc.url, jdbc.username, jdbc.password)
    model.table(jdbc.schema, tableName)
      .orElse(model.table(jdbc.schema, tableName.toUpperCase(en)))
      .orElse(model.table(jdbc.schema, tableName.toLowerCase(en)))
      .map { table =>
      Option(new CodeGenerator(table, className)(config))
    } getOrElse {
      println("The table is not found.")
      None
    }
  }

  def allGenerators(srcDir: File, testDir: File, jdbc: JDBCSettings, generatorSettings: GeneratorSettings): Seq[CodeGenerator] = {
    val config = generatorConfig(srcDir, testDir, generatorSettings)
    val className = None
    Class.forName(jdbc.driver) // load specified jdbc driver
    val model = Model(jdbc.url, jdbc.username, jdbc.password)
    model.allTables(jdbc.schema).map { table =>
      new CodeGenerator(table, className)(config)
    }
  }

  def using[R <: {def close()}, A](resource: R)(f: R => A): A = ultimately {
    ignoring(classOf[Throwable]) apply resource.close()
  } apply f(resource)

}
