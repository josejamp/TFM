package main.scala.web

import java.io.IOException
import java.io.FileInputStream
import java.io.File

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange

class GraphHandler extends HttpHandler{
  
  @throws(classOf[IOException])
  override def handle(t : HttpExchange) = {
      var h = t.getResponseHeaders();
      t.sendResponseHeaders(200, 0);    
      h.set("Content-Type","text/html")
      var os = t.getResponseBody()
      var file = new File("src/main/resources/web/prueba.html")
      var fs = new FileInputStream(file)
      val buffer : Array[Byte] = new Array[Byte](0x10000)
      var count = 0
      count = fs.read(buffer)
      while (count >= 0) {
        os.write(buffer,0,count);
        count = fs.read(buffer)
      }
      fs.close()
      os.close();
  }
  
  
}