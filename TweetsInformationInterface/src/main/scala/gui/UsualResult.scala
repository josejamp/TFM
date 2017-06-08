package main.scala.gui

import javafx.beans.property.SimpleStringProperty

class UsualResult(name : String, punt_propia : String, punt_hijos : String, men : String, identificador : String) extends SearchResult(name : String, identificador : String) {
  
  var puntuacionPropia = new SimpleStringProperty(punt_propia)
  var puntuacionHijos = new SimpleStringProperty(punt_hijos)
  var menciones = new SimpleStringProperty(men)
  
  def getPuntuacionPropia = this.puntuacionPropia.get()
  def getPuntuacionHijos = this.puntuacionHijos.get()
  def getMenciones = this.menciones.get()
  
  def getNumElements() = 4
  
}