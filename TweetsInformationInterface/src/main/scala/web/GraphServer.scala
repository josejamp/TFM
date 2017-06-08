package main.scala.web

import java.net.InetSocketAddress

import com.sun.net.httpserver.HttpServer

class GraphServer(port : Int) {
  
  var address : Int = port
  
  var server : HttpServer = null
  
  def getPort = this.address
  def getAddress = "http://localhost:" + this.address
  
  def start() = {
      server = HttpServer.create(new InetSocketAddress(address), 0)
      var context = server.createContext("/test", new GraphHandler());
      server.setExecutor(null); // creates a default executor
      server.start();
  }
  
  def stop() = {
    server.stop(0)
  }
  
}