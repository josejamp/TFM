package main.scala.gui

import main.scala.gui.controller.SearchController

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.ComboBox
import javafx.scene.control.CheckBox
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.geometry.Insets
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener

class AdvanceSearch {
  
  var mainPane = new GridPane()
  var cabecera = new GridPane()
  var dateLine = new GridPane()
  var cuerpo = new GridPane()
  var cabeceraComboBox : ComboBox[String] = null
  
     
  var dateLabel = new Label("permitir fechas  ")
  var dateBox = new CheckBox()
  
  var searchComponent = new SearchComponent(0)
  
  
  def getMainPane = mainPane
  def getCuerpo = cuerpo
  
  def creaCabecera() = {
    cabecera.setPadding(new Insets(16))
    cabecera.add(new Label("Encuentra elementos que cumplan "), 0, 0)
    var options : ObservableList[String] = FXCollections.observableArrayList(
            "todas",
            "algunas"
    )
    cabeceraComboBox = new ComboBox(options)
    cabecera.add(cabeceraComboBox, 1, 0)
    cabecera.add(new Label(" de las siguientes condiciones"), 2, 0)
    mainPane.add(cabecera, 0, 0)
  }
  
  def creaSearchComponents(searchController : SearchController) {
     dateLabel.setPadding( new Insets(0,0,0,16))
     dateBox.setPadding( new Insets(0,0,0,0))
     dateBox.selectedProperty().addListener(new ChangeListener[java.lang.Boolean]() {
        override def changed( ov : ObservableValue[_ <: java.lang.Boolean], viejo : java.lang.Boolean, nuevo : java.lang.Boolean) = {
          if(nuevo) searchComponent.addControlFechas()
          else searchComponent.removeControlFechas()
        }
     })
    dateLine.add(dateLabel, 0, 0)
    dateLine.add(dateBox, 1, 0)
    searchComponent.inicializaSearchComponent(searchController)
    cuerpo.add(dateLine, 0, 0)
    cuerpo.add(searchComponent.getFrontEnd,0,1)
    mainPane.add(cuerpo, 0, 1)
  }
  
  def getIfAll() : Boolean = {
    return cabeceraComboBox.getSelectionModel.getSelectedItem == "todas"
  }
  
  def addDateControl() = searchComponent.addControlFechas()
  
  def removeDateControl() = searchComponent.removeControlFechas()
  
  def ejecutaBusqueda(searchController : SearchController) : List[SearchResult] = {
    searchComponent.ejecutaBusqueda(searchController)
  }
  
  
}