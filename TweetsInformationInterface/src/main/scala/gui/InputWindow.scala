package main.scala.gui

import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.control.PasswordField
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.control.TextField
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

import main.scala.gui.controller.MainController

class InputWindow(cont : MainController, esConf : Boolean) {
  
  var tipoConf = esConf
  var controller = cont
  
  var vBox = new VBox()
  
  var infoLabel = new Label("Introduce la nueva direccion del servidor")
  var textInput = new TextField()
  var usuarioLabel = new Label("Usuario: ")
  var userInput = new TextField()
  var passwordLabel = new Label("Password: ")
  var passwordInput = new PasswordField()
  var okButton = new Button("Aceptar")
  var cancelButton = new Button("Cancelar")
  
  def getVBox = this.vBox
  
  
  def initWindow() = {
     var gridPane = new GridPane()
     gridPane.setPadding(new Insets(16))
     
     var topPane = new GridPane()
     topPane.setPadding(new Insets(16))
     var centerPane = new GridPane()
     centerPane.setPadding(new Insets(16))
     var lowerPane = new GridPane()
     lowerPane.setPadding(new Insets(16))
     lowerPane.setHgap(120)
     
     okButton.setOnAction(new EventHandler[ActionEvent]() {
          override def handle(e : ActionEvent) {
            var ok = true
            try{
              if(userInput.getText.size == 0) {
                if(!controller.initDataBaseConnection(textInput.getText)){
                  ok = false
                  muestraError("Conexion rechazada. Compruebe que la direccion es correcta.")
                }
              }
              else{
                if(!controller.initDataBaseConnection(textInput.getText, userInput.getText, passwordInput.getText)){
                  ok = false
                  muestraError("Conexion rechazada. Compruebe que la direccion es correcta.")
                }
                print(userInput.getText, passwordInput.getText)
              }
            }
            catch{
              case badURL : java.net.MalformedURLException => {
                ok = false
                muestraError("URL mal formada.")
              }
            }
            if(ok){
              if(tipoConf){
                var mainWindow = new MainWindow()
                var stage = new Stage()
                controller.borraRepetidos()
                mainWindow.start(stage, controller)
              }
              else controller.initGraph()
              var stage = okButton.getScene().getWindow().asInstanceOf[Stage]
              stage.close()
            }
          }
        })
     
     cancelButton.setOnAction(new EventHandler[ActionEvent]() {
          override def handle(e : ActionEvent) {
            var stage = cancelButton.getScene().getWindow().asInstanceOf[Stage]
            stage.close()
          }
        })
     
     topPane.add(infoLabel, 0, 0)
     topPane.add(textInput, 0, 1)
     
     centerPane.add(usuarioLabel, 0, 0)
     centerPane.add(userInput, 1, 0)
     centerPane.add(passwordLabel, 0, 1)
     centerPane.add(passwordInput, 1, 1)
     
     lowerPane.add(okButton, 0, 0)
     lowerPane.add(cancelButton, 1, 0)
     
     gridPane.add(topPane, 0, 0)
     gridPane.add(centerPane, 0, 1)
     gridPane.add(lowerPane, 0, 2)
    
     vBox.setSpacing(5)
     vBox.setPadding(new Insets(10, 0, 0, 10))
     vBox.getChildren().addAll(gridPane)
  }
  
  def muestraError(mensaje : String) = {
    var alert = new Alert(AlertType.INFORMATION)
    alert.setTitle("Error Dialog")
    alert.setHeaderText(null)
    alert.setContentText(mensaje)
    alert.showAndWait()
  }
  
}