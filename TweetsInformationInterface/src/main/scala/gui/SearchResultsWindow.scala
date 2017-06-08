package main.scala.gui

import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class SearchResultsWindow {
  
  var tabla1 = new TableView[UsualResult]()
  var tabla2 = new TableView[ModifierResult]()
  var vBox = new VBox()
  
  
  def getVBox = this.vBox
  
  def inicializaTablaUsual(lista : List[UsualResult]) = {
    
      var data : ObservableList[UsualResult] = FXCollections.observableArrayList[UsualResult]()
      lista.foreach { x => data.add(x) }
 
      tabla1.setEditable(false)
 
      var firstCol = new TableColumn[UsualResult, String]("Nombre")
      firstCol.setCellValueFactory(new PropertyValueFactory[UsualResult, String]("nombre"))
      var secondCol = new TableColumn[UsualResult, String]("Puntuacion hijos")
      secondCol.setCellValueFactory(new PropertyValueFactory[UsualResult, String]("puntuacionHijos"))
      var thirdCol = new TableColumn[UsualResult, String]("Puntuacion propia")
      thirdCol.setCellValueFactory(new PropertyValueFactory[UsualResult, String]("puntuacionPropia"))
      var fourthCol = new TableColumn[UsualResult, String]("Menciones")
      fourthCol.setCellValueFactory(new PropertyValueFactory[UsualResult, String]("menciones"))
      
      tabla1.setItems(data)
      tabla1.getColumns().addAll(firstCol, secondCol, thirdCol, fourthCol)
      
  }
  
  def inicializaTablaMods(lista : List[ModifierResult]) = {
    
      var data : ObservableList[ModifierResult] = FXCollections.observableArrayList[ModifierResult]()
      lista.foreach { x => data.add(x) }
 
      tabla2.setEditable(false)
 
      var firstCol = new TableColumn[ModifierResult, String]("Nombre")
      firstCol.setCellValueFactory(new PropertyValueFactory[ModifierResult, String]("nombre"))
      var thirdCol = new TableColumn[ModifierResult, String]("Puntuacion")
      thirdCol.setCellValueFactory(new PropertyValueFactory[ModifierResult, String]("puntuacion"))
      var fourthCol = new TableColumn[ModifierResult, String]("Repeticiones")
      fourthCol.setCellValueFactory(new PropertyValueFactory[ModifierResult, String]("repeticiones"))
      
      tabla2.setItems(data)
      tabla2.getColumns().addAll(firstCol, thirdCol, fourthCol)
      
  }
  
    def addVBox() = {
      var label = new Label("No se encontraron resultados")
      label.setFont(new Font("Arial", 20))
    
      vBox.setSpacing(5)
      vBox.setPadding(new Insets(10, 0, 0, 10))
      vBox.getChildren().addAll(label)
  }
  
  
  def addVBox(tabla : TableView[_]) = {
      var label = new Label("Resultados")
      label.setFont(new Font("Arial", 20))
    
      vBox.setSpacing(5)
      vBox.setPadding(new Insets(10, 0, 0, 10))
      vBox.getChildren().addAll(label, tabla)
  }
  
  def addVBox(tablaUsual : TableView[_], tablaMods : TableView[_]) = {
      var label = new Label("Resultados")
      label.setFont(new Font("Arial", 20))
    
      vBox.setSpacing(5)
      vBox.setPadding(new Insets(10, 0, 0, 10))
      vBox.getChildren().addAll(label, tablaUsual, tablaMods)
  }
  
  def inicializaDosTablas(listaUsual : List[UsualResult], listaMods : List[ModifierResult]) = {
    if(listaUsual.size==0 && listaMods.size==0){
      addVBox()
    }
    else if(listaUsual.size==0 ){
      inicializaTablaMods(listaMods)
      addVBox(tabla2)
    }
    else if(listaMods.size==0){
      inicializaTablaUsual(listaUsual)
      addVBox(tabla1)
    }
    else{
      inicializaTablaUsual(listaUsual)
      inicializaTablaMods(listaMods)
      addVBox(tabla1,tabla2)
    }

  }
  
  
}