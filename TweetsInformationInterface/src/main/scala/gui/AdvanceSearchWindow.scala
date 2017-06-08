package main.scala.gui

import main.scala.gui.controller.SearchController

import scala.collection.mutable.ArrayBuffer

import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.CheckBox
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.event.ActionEvent
import javafx.event.EventHandler


class AdvanceSearchWindow(left : Int) {
  
   var leftSpace = left
   var mainPane = new GridPane()
   var topPane = new GridPane()
   var lowPane = new GridPane()
   
     
   var removeButton = new Button("Quita condicion")
   var addButton = new Button("Añade condicion")
   var num = 1
   var listaHijos = new ArrayBuffer[GridPane]()
   
   var firstAdvanceSearch : AdvanceSearch = null
   var listaAdvanceSearch =  new ArrayBuffer[AdvanceSearch]()
   
   
   def getMainPane = mainPane
   
   def creaAdvanceSearchWindow(searchController : SearchController) = {
     mainPane.setPadding(new Insets(16,16,16,16 + leftSpace))
     val black = new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2)))
     mainPane.setBorder(black)
     addButton.setOnAction(new EventHandler[ActionEvent]() {
     override def handle(e : ActionEvent) {
        addAdvanceSearch(searchController,false)
      }
     })
     removeButton.setOnAction(new EventHandler[ActionEvent]() {
     override def handle(e : ActionEvent) {
        removeAdvanceSearch()
      }
     })
     topPane.add(addButton, 0, 0)
     topPane.add(removeButton, 1, 0)
     mainPane.add(topPane,0,0)
     mainPane.add(lowPane,0,1)
     addAdvanceSearch(searchController,true)
   }
   
   def addAdvanceSearch(searchController : SearchController, first : Boolean) = {
     if(first){
       firstAdvanceSearch = new AdvanceSearch()
       firstAdvanceSearch.creaCabecera()
       firstAdvanceSearch.creaSearchComponents(searchController)
       lowPane.add(firstAdvanceSearch.getMainPane, 0, num)
       listaHijos.append(firstAdvanceSearch.getMainPane)
       listaAdvanceSearch.append(firstAdvanceSearch)
     }
     else{
       var advanceSearch = new AdvanceSearch()
       advanceSearch.creaCabecera()
       advanceSearch.creaSearchComponents(searchController)
       lowPane.add(advanceSearch.getCuerpo, 0, num)
       listaHijos.append(advanceSearch.getCuerpo)
       listaAdvanceSearch.append(advanceSearch)
     }
     num += 1
   }
   
   def removeAdvanceSearch() = {
     if(listaHijos.size > 1){
       lowPane.getChildren.remove(listaHijos.last)
       listaHijos.remove(listaHijos.size-1)
       listaAdvanceSearch.remove(listaAdvanceSearch.size-1)
     }
   }
   
   def ejecutaBusqueda(searchController : SearchController) : List[SearchResult] = {
     var l = List[List[SearchResult]]()
     listaAdvanceSearch.foreach{ search =>
       l = (search.ejecutaBusqueda(searchController)) :: l
     }
     val res = searchController.unifyResults(l, firstAdvanceSearch.getIfAll())
     print(res)
     return res
   }
  
  
}