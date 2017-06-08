package main.scala.gui

import javafx.beans.property.SimpleStringProperty

class ModifierResult(name : String, punt : String, rep : String) extends SearchResult(name : String, name : String) {
  
  var puntuacion = new SimpleStringProperty(punt)
  var repeticiones = new SimpleStringProperty(rep)
  
  def getPuntuacion = this.puntuacion.get()
  def getRepeticiones = this.repeticiones.get()
  
  def getNumElements() = 3
  
}