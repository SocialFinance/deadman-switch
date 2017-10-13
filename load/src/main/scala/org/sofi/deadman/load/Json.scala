package org.sofi.deadman.load

// JSON Utility object
object Json {
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  // Init and configure a mapper
  private final val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  // Decode some value from a string
  def decode[T](value: String, path: String, clazz: Class[T]): T = {
    var node = mapper.readTree(value.getBytes())
    path.split("/").foreach { item ⇒ node = node.get(item) }
    mapper.readValue(node.toString, clazz)
  }
}
