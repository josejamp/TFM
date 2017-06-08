package main.scala.gui


import main.scala.gui.controller.SearchController

import javafx.collections.FXCollections
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.CheckBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.geometry.Insets

class SearchComponent(left : Int) {
  
  var leftSpace = left
  var frontEnd = new GridPane()
  
  var line = new GridPane()
  var secondLine = new GridPane()
  var frontLine = new GridPane()
  var backLine = new GridPane()
  var dateLine = new GridPane()
  
  var elementoDevolverBox = new ComboBox[String]()
  var condicionBox = new ComboBox[String]()
  
  var tipoOrArithBox = new ComboBox[String]()
  var condicionTextField = new TextField()
  
  var dateNeeded = false
  var labelDate = new Label(" entre fechas ")
  var dateInput1 = new DatePicker()
  var dateInput2 = new DatePicker()
  
  var subConsulta : AdvanceSearchWindow = null
  
  
  def getFrontEnd = frontEnd
  
  def inicializaSearchComponent(searchController : SearchController) : Unit = {
    line.setPadding(new Insets(16,16,16,16 + leftSpace))
    var items = FXCollections.observableArrayList[String]()
    searchController.getRelationships().foreach { x => items.add(x) }
    elementoDevolverBox.setItems(items)
    frontLine.add(elementoDevolverBox,0,0)
    frontLine.add(new Label(" tal que "), 1, 0)
    var conditionElements = FXCollections.observableArrayList[String](
      "pertenece"
    )
    condicionBox.setItems(conditionElements)
    elementoDevolverBox.valueProperty().addListener(new ChangeListener[String]() {
          override def changed(ov : ObservableValue[_ <: String] ,viejo : String , nuevo : String) {
            var nuevosItems = FXCollections.observableArrayList[String]()
            condicionBox.getItems.clear()
            condicionBox.getItems.add("pertenece")
            searchController.getAttributes(nuevo).foreach { x => condicionBox.getItems.add(x) }
          }
    })
    condicionBox.valueProperty().addListener(new ChangeListener[String]() {
          override def changed(ov : ObservableValue[_ <: String] ,viejo : String , nuevo : String) {
            backLine.getChildren.clear()
            secondLine.getChildren.clear()
            frontEnd.getChildren.clear()
            frontEnd.add(line, 0, 0)
            if(nuevo != "pertenece"){
                if(nuevo != "nombre"){
                  var arithmConditions = FXCollections.observableArrayList(
                          ">",
                          ">=",
                          "<",
                          "<=",
                          "="
                  )
                  tipoOrArithBox.setItems(arithmConditions)
                }
                else{
                  var arithmConditions = FXCollections.observableArrayList(
                          "contiene",
                          "="
                  )
                  tipoOrArithBox.setItems(arithmConditions)
                }
                backLine.add(tipoOrArithBox,0,0)
                backLine.add(condicionTextField,1,0)
            }
            else{
                backLine.add(new Label(" a subconsulta:  "), 0, 0)
                subConsulta = new AdvanceSearchWindow(leftSpace + 16)
                subConsulta.creaAdvanceSearchWindow(searchController)
                secondLine.add(subConsulta.getMainPane,0,0)
                frontEnd.add(secondLine,0,1)
            }
          } 
      })
      frontLine.add(condicionBox,2,0)
      
      line.add(frontLine,0,0)
      line.add(backLine,1,0)
      line.add(dateLine, 2, 0)
      
      frontEnd.add(line, 0, 0)
    
  }
  
  def addControlFechas() = {
    this.dateNeeded = true
    dateLine.add(labelDate, 0, 0)
    dateLine.add(dateInput1, 1, 0)
    dateLine.add(dateInput2, 2, 0)
  }
  
  def removeControlFechas() = {
    this.dateNeeded = false
    dateLine.getChildren.clear()
  }
  
  
  def ejecutaBusqueda(searchController : SearchController) : List[SearchResult] = {
    if(!compruebaErrores()){
      if(condicionBox.getSelectionModel.getSelectedItem=="pertenece") {
        searchController.searchPertenencia(elementoDevolverBox.getSelectionModel.getSelectedItem, subConsulta.ejecutaBusqueda(searchController))
      }
      else if(!dateNeeded){
        searchController.search(elementoDevolverBox.getSelectionModel.getSelectedItem, condicionBox.getSelectionModel.getSelectedItem, tipoOrArithBox.getSelectionModel.getSelectedItem, condicionTextField.getText.toLowerCase())
      }
      else searchController.search(elementoDevolverBox.getSelectionModel.getSelectedItem, condicionBox.getSelectionModel.getSelectedItem, tipoOrArithBox.getSelectionModel.getSelectedItem, condicionTextField.getText.toLowerCase(), dateInput1.getValue, dateInput2.getValue)
    }
    else return List[SearchResult]()
  }
  
  def compruebaErrores() : Boolean = {
    if(elementoDevolverBox.getSelectionModel.isEmpty()){
      muestraError("Debe seleccionar el elemento a buscar")
      return true
    }
    else if(condicionBox.getSelectionModel.isEmpty()){
      muestraError("Debe seleccionar la condicion de la busqueda")
      return true
    }
    else if(condicionBox.getSelectionModel.getSelectedItem!="pertenece"){
      if(tipoOrArithBox.getSelectionModel.isEmpty()){
        muestraError("Debe seleccionar un operador para la busqueda")
        return true
      }
      else if(condicionTextField.getText.size == 0){
        muestraError("Debe introducir el valor del operando para la busqueda")
        return true
      }
      else return false
    }
    else return false
  }
  
  def muestraError(mensaje : String) = {
    var alert = new Alert(AlertType.INFORMATION)
    alert.setTitle("Error Dialog")
    alert.setHeaderText(null)
    alert.setContentText(mensaje)
    alert.showAndWait()
  }
  
  
  
}