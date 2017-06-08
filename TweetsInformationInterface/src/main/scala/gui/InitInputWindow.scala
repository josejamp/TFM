package main.scala.gui

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Scene

import main.scala.gui.controller.MainController

class InitInputWindow extends Application{
  
  override def start(primaryStage : Stage) = {
    print("Path: " + ClassLoader.getSystemClassLoader().getResource(".").getPath() + "\n")
    var mainController = new MainController()
    var inputWindow = new InputWindow(mainController,true)
    inputWindow.initWindow()
    primaryStage.setTitle("Direccion de la Base de Datos")
    primaryStage.setScene(new Scene(inputWindow.getVBox, 320, 235))
    primaryStage.show()
  }
  
}