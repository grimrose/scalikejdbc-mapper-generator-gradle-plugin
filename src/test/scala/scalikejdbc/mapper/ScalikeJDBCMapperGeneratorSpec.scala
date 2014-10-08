package scalikejdbc.mapper

import java.nio.file.Paths

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class ScalikeJDBCMapperGeneratorSpec extends FlatSpec with Matchers {

  "ScalikeJDBCMapperGenerator" should "load properties file" in {
    val resource = getClass.getResource("/scalikejdbc.properties")
    val file = Paths.get(resource.toURI)
    val parent = file.getParent

    val g = new ScalikeJDBCMapperGenerator
    val settings = g.loadSettings(parent.toString)
    
    val jdbc = settings._1

    jdbc.driver should be("org.h2.Driver")
    jdbc.url should be("jdbc:h2:./db;MODE=PostgreSQL;AUTO_SERVER=TRUE")
    jdbc.username should be("sa")
    jdbc.password should be(empty)
    jdbc.schema should be(empty)

    val generator = settings._2
    generator.packageName should be("models")
    generator.template should be("queryDsl")
    generator.testTemplate should be("ScalaTestFlatSpec")
    generator.caseClassOnly should be(true)
    generator.encoding should be("UTF-8")
    generator.autoConstruct should be(false)
    generator.defaultAutoSession should be(true)
  }


}
