package main.scala.gui

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.stage.WindowEvent

import main.scala.gui.controller.MainController

class MenuConf(cont : MainController) {
  
  var controller = cont
  
  def getMenuConf : MenuBar = {
    var menuBar = new MenuBar()
    var menuFile = new Menu("Archivo")
    var menuEdit = new Menu("Configuracion")
    var menuView = new Menu("Ayuda")
    
    var conexion = new MenuItem("Conexion")
    conexion.setOnAction(new EventHandler[ActionEvent]() {
            def handle(t : ActionEvent) {
                var inputWindow = new InputWindow(controller,false)
                inputWindow.initWindow()
                var stage = new Stage()
                stage.setTitle("Direccion de la Base de Datos")
                stage.setScene(new Scene(inputWindow.getVBox, 320, 235))
                stage.show()
            }
    })
    var salir = new MenuItem("Salir")
    menuFile.getItems.addAll(conexion, salir)
    
    menuBar.getMenus().addAll(menuFile, menuEdit, menuView)
    menuBar
  }
  
}