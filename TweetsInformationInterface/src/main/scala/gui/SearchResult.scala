package main.scala.gui

import javafx.beans.property.SimpleStringProperty

abstract class SearchResult(name : String, identificador : String) {
  
  var nombre : SimpleStringProperty = new SimpleStringProperty(name)
  var id = new SimpleStringProperty(identificador)
  
  def setNombre(nuevoNombre : String) = {
    nombre.set(nuevoNombre)
  }
  
  def getNombre = nombre.get()
  def getID = id.get()
  
  def getNumElements() : Int
  
  override def toString() : String = nombre.toString()
  
  override def equals(other: Any): Boolean = other match {
    case other:SearchResult => this.nombre.get == other.nombre.get
    case _ => false
  }
  
  def eraseParenthesis(chain : String) : String = {
    var acc = ""
    var enc = false
    chain.foreach { x =>
      if(x==' ') enc = true
      if(!enc) acc = acc + x
    }
    return acc
  }
  
}