package org.grimrose.gradle.scalikejdbc

import java.io.{ByteArrayOutputStream, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.gradle.tooling.GradleConnector
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}
import scalikejdbc.{ConnectionPool, DB, SQL}

import scala.collection.mutable
import scala.util.control.Exception._

@RunWith(classOf[JUnitRunner])
class ScalikeJDBCMapperGeneratorPlugin_IntegrationTestSpec extends FunSpec with Matchers {

  describe("scalikejdbcGenEcho") {
    it("should echo") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)
        createTables(tmp.toPath)

        val actual = runTasks(tmp, Array("scalikejdbcGenEcho"), Array("--project-prop", "tableName=member_group")).right.getOrElse("")

        actual should include("case class MemberGroup")
        actual should include("object MemberGroup")
        actual should include("class MemberGroupSpec")
      }
    }
    it("should echo with table name and class name") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)
        createTables(tmp.toPath)

        val actual = runTasks(tmp, Array("scalikejdbcGenEcho"), Array("-P", "tableName=member_group", "-P", "className=CustomMemberGroup")).right.getOrElse("")

        actual should include("case class CustomMemberGroup")
        actual should include("object CustomMemberGroup")
        actual should include("class CustomMemberGroupSpec")
      }
    }
    it("should fail") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)
        createTables(tmp.toPath)

        val actual = runTasks(tmp, Array("scalikejdbcGenEcho"), Array("-P", "tableName=")).left.toOption

        actual should be(defined)
      }
    }

  }

  describe("scalikejdbcGen") {
    it("should create file") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("src/main/scala"))
        Files.createDirectories(tmp.toPath.resolve("src/test/scala"))

        createTables(tmp.toPath)

        runTasks(tmp, Array("scalikejdbcGen"), Array("-P", "tableName=member_group"))
        //        recursiveFiles(tmp).foreach(println(_))

        val files = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        files.find(_.getName == "MemberGroup.scala") should be(defined)
        files.find(_.getName == "MemberGroupSpec.scala") should be(defined)
      }
    }
    it("should be able to compile") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath,
          """
            |apply plugin: 'scala'
            |repositories {
            |  mavenCentral()
            |}
            |dependencies {
            |  compile "org.scala-lang:scala-library:2.11.+"
            |  compile "org.scalikejdbc:scalikejdbc_2.11:+"
            |  runtime 'ch.qos.logback:logback-classic:1.1.2'
            |  testCompile "org.scalikejdbc:scalikejdbc-test_2.11:+"
            |  testCompile "org.scalatest:scalatest_2.11:2.2.2"
            |  testRuntime "org.scala-lang.modules:scala-xml_2.11:1.0.2"
            |}
          """.stripMargin)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("src/main/scala"))
        Files.createDirectories(tmp.toPath.resolve("src/test/scala"))

        createTables(tmp.toPath)

        val actual = runTasks(tmp, Array("scalikejdbcGen", "classes", "testClasses"), Array("-P", "tableName=member"))
        actual should be('right)

        val dirs = recursiveFiles(tmp).filter(_.isDirectory)
        dirs.find(_.getName == "build") should be(defined)
        dirs.find(_.getName == "classes") should be(defined)
      }
    }
    it("should create file at custom directory") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath,
          """
            |scalikejdbcGen.srcDir = file("srcDir")
            |scalikejdbcGen.testDir = file("testDir")
          """.stripMargin)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("srcDir"))
        Files.createDirectories(tmp.toPath.resolve("testDir"))

        createTables(tmp.toPath)

        runTasks(tmp, Array("scalikejdbcGen"), Array("-P", "tableName=member_group"))
        //        recursiveFiles(tmp).foreach(println(_))

        val files = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        files.find(_.getName == "MemberGroup.scala") should be(defined)
        files.find(_.getName == "MemberGroupSpec.scala") should be(defined)

        files.find(_.getName == "MemberGroup.scala").map(_.toPath.toString).getOrElse("") should include("srcDir")
        files.find(_.getName == "MemberGroupSpec.scala").map(_.toPath.toString).getOrElse("") should include("testDir")
      }
    }
  }

  describe("scalikejdbcGenAll") {
    it("should create all table file") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("src/main/scala"))
        Files.createDirectories(tmp.toPath.resolve("src/test/scala"))

        createTables(tmp.toPath)

        runTasks(tmp, Array("scalikejdbcGenAll"), Array())
        //        recursiveFiles(tmp).foreach(println(_))

        val files = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        files.find(_.getName == "MemberGroup.scala") should be(defined)
        files.find(_.getName == "MemberGroupSpec.scala") should be(defined)
        files.find(_.getName == "Member.scala") should be(defined)
        files.find(_.getName == "MemberSpec.scala") should be(defined)
      }
    }
  }

  describe("scalikejdbcGenForce") {
    it("should overwrite table file") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("src/main/scala"))
        Files.createDirectories(tmp.toPath.resolve("src/test/scala"))

        createTables(tmp.toPath)

        runTasks(tmp, Array("scalikejdbcGen"), Array("-P", "tableName=member_group"))
        val beforeFiles = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        val beforeModelModified = beforeFiles.find(_.getName == "MemberGroup.scala").map(_.lastModified())
        val beforeSpecModified = beforeFiles.find(_.getName == "MemberGroupSpec.scala").map(_.lastModified())

        Thread.sleep(500)

        runTasks(tmp, Array("scalikejdbcGenForce"), Array("-P", "tableName=member_group"))

        val files = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        val modelFile = files.find(_.getName == "MemberGroup.scala")
        val specFile = files.find(_.getName == "MemberGroupSpec.scala")

        modelFile should be(defined)
        specFile should be(defined)
        beforeModelModified should be < modelFile.map(_.lastModified())
        beforeSpecModified should be < specFile.map(_.lastModified())
      }
    }
  }

  describe("scalikejdbcGenAllForce") {
    it("should overwrite all table file") {
      withTempDirectory { tmp =>
        makeBuildScript(tmp.toPath)
        makeScalikeJDBCPropertiesFile(tmp.toPath)

        Files.createDirectories(tmp.toPath.resolve("src/main/scala"))
        Files.createDirectories(tmp.toPath.resolve("src/test/scala"))

        createTables(tmp.toPath)

        runTasks(tmp, Array("scalikejdbcGen"), Array("-P", "tableName=member_group"))
        val beforeFiles = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        val beforeModelModified = beforeFiles.find(_.getName == "MemberGroup.scala").map(_.lastModified())
        val beforeSpecModified = beforeFiles.find(_.getName == "MemberGroupSpec.scala").map(_.lastModified())

        runTasks(tmp, Array("scalikejdbcGenAllForce"), Array())

        val files = recursiveFiles(tmp).filter(_.isFile).filter(_.getName.endsWith("scala"))
        val modelFile = files.find(_.getName == "MemberGroup.scala")
        val specFile = files.find(_.getName == "MemberGroupSpec.scala")

        modelFile should be(defined)
        specFile should be(defined)
        beforeModelModified should be < modelFile.map(_.lastModified())
        beforeSpecModified should (be < specFile.map(_.lastModified()))

        files.find(_.getName == "Member.scala") should be(defined)
        files.find(_.getName == "MemberSpec.scala") should be(defined)
      }
    }
  }

  def withTempDirectory(task: (File) => Any) {
    val dir = Files.createTempDirectory(getClass.getSimpleName)
    try {
      task(dir.toFile)
    }
    finally {
      deleteFile(dir.toFile)
      println(s"temp dir exists? -> ${dir.toFile.exists()}")
    }
  }

  def deleteFile(f: File): Unit = {
    if (f.isFile) {
      f.delete()
    } else {
      f.listFiles().foreach(deleteFile)
      f.delete()
    }
  }

  def runTasks(projectDir: File, tasks: Array[String], args: Array[String]): Either[Throwable, String] = {
    val out = new ByteArrayOutputStream()
    val connector = GradleConnector.newConnector()
    connector.forProjectDirectory(projectDir)
    val connection = connector.connect()
    allCatch.andFinally {
      connection.close()
    } either {
      val a = Array("--stacktrace", "-q") ++ args
      connection.newBuild().setStandardOutput(out).forTasks(tasks: _*).withArguments(a: _*).run()
      new String(out.toByteArray, StandardCharsets.UTF_8)
    }
  }

  def recursiveFiles(f: File): Seq[File] = {
    val files: mutable.WrappedArray[File] = f.listFiles()
    files ++ files.filter(_.isDirectory).flatMap(recursiveFiles)
  }

  def createTables(path: Path) = {
    Class.forName("org.h2.Driver")
    val url = s"jdbc:h2:${path}/db;MODE=PostgreSQL;AUTO_SERVER=TRUE"
    val username = "sa"
    val password = ""
    ConnectionPool.singleton(url, username, password)

    DB autoCommit { implicit session =>
      // language=SQL
      SQL( """
        create table member_group (
          id int generated always as identity,
          name varchar(30) not null,
          _underscore varchar(30),
          primary key(id)
        )
           """).execute().apply()
      SQL( """
        create table member (
          id int
          generated always as identity,
          name varchar(30) not null,
          member_group_id int,
          description varchar(1000),
          birthday date,
          created_at timestamp not null,
          primary key(id)
        )
           """).execute.apply()
    }

  }

  def makeScalikeJDBCPropertiesFile(path: Path) = {
    val dir = Files.createDirectory(path.resolve("project"))
    Files.write(Files.createFile(dir.resolve("scalikejdbc.properties")),
      s"""
      |jdbc.driver=org.h2.Driver
      |jdbc.url=jdbc:h2:${path}/db;MODE=PostgreSQL;AUTO_SERVER=TRUE
      |jdbc.username=sa
      |jdbc.password=
      |jdbc.schema=
      |generator.packageName=models
      |geneartor.lineBreak=LF
      |generator.template=queryDsl
      |generator.testTemplate=ScalaTestFlatSpec
      |generator.encoding=UTF-8
      |generator.caseClassOnly=true
      """.stripMargin.getBytes(StandardCharsets.UTF_8))
  }

  def scriptHeader(path: Path = Paths.get("."), appendix: String) = { s"""
      |buildscript {
      |  repositories {
      |    mavenCentral()
      |  }
      |  dependencies {
      |    classpath files('${path.toAbsolutePath}/build/classes/main')
      |    classpath files('${path.toAbsolutePath}/build/resources/main')
      |    classpath "com.h2database:h2:+"
      |    classpath "org.scala-lang:scala-library:2.11.+"
      |    classpath "org.scalikejdbc:scalikejdbc-mapper-generator-core_2.11:+"
      |  }
      |}
      |apply plugin: 'org.grimrose.gradle.scalikejdbc'
      |
      |$appendix
      """.stripMargin
  }

  def makeBuildScript(path: Path, appendix: String = ""): Unit = {
    val buildScript = Files.createFile(path.resolve("build.gradle"))
    Files.write(buildScript, scriptHeader(appendix = appendix).getBytes(StandardCharsets.UTF_8))
    //    println(Source.fromFile(buildScript.toFile, "UTF-8").mkString)
  }

}
