package main.scala.gui

class TreeItemPair(name : String, id : String) {
  
  var nombre = name
  var identificador = id
  
  def getNombre = nombre
  def getIdentificador = identificador
  
  override def toString() : String = nombre
  
}