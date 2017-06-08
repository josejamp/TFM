package main.scala.gui.controller

import main.scala.Jerarquia
import main.scala.dataBase.Consultas
import main.scala.dataBase.TraduccionDatos
import main.scala.io.GraphFileManager
import main.scala.io.EscribeJSON
import main.scala.dataBase.Consultas

class MainController {
  
  def this(initJer : Boolean) = {
    this()
    if(initJer) jer = TraduccionDatos.generaJerarquiaSinModificadores()
  }
  
  var jer : Jerarquia = null
  
  var graphFileManager = new GraphFileManager(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/prueba.html", ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/data.json", ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/template.html")
  //var graphFileManager = new GraphFileManager("src/main/resources/web/prueba.html","src/main/resources/web/data.json", "src/main/resources/web/template.html")
  
  
  def getJerarquia = this.jer
  
  def getGraphFileManager = this.graphFileManager
  
  def initDataBaseConnection(path : String) : Boolean = {
    var ok = Consultas.setServer(path)
    if(ok){
      borraRepetidos()
      jer = TraduccionDatos.generaJerarquiaSinModificadores()
      return true
    }
    else return false
  }
  
  def borraRepetidos() = {
    Consultas.cleanLoops()
  }
  
  def initDataBaseConnection(path : String, user : String, password : String) : Boolean = {
    var ok = Consultas.setServer(path, user, password)
    if(ok){
      borraRepetidos()
      jer = TraduccionDatos.generaJerarquiaSinModificadores()
      return true
    }
    else return false
  }
  
  def initGraph() = {
     val (nodos, relaciones) = TraduccionDatos.getArbolParaHTML()
    EscribeJSON.escribe(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/data.json", nodos, relaciones)
    graphFileManager.restoreHTML()
    graphFileManager.writeHTML()
  }
  
}