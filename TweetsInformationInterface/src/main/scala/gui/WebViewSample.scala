package main.scala.gui

import javafx.application.Application
import javafx.scene.layout.StackPane
import javafx.scene.control.Label
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage



class WebViewSample extends Application {
  
  
    var scene : Scene = null
    
    override def start(stage : Stage)  = {
        // create the scene
        stage.setTitle("Web View")
        scene = new Scene(new Browser("file:///C:/Users/Javier/workspaceScala/Pruebas/src/main/resources/web/prueba.html"),750,500, Color.web("#666970"))
        stage.setScene(scene) 
        stage.show()
    }
    
}

class Browser() extends Region {

    val browser = new WebView()
    val webEngine = browser.getEngine()
    
    var path = ""

    def this(web : String) = {
        this()
        path = web
        //apply the styles
        getStyleClass().add("browser")
        // load the web page
        webEngine.load(web)
        //add the web view to the scene
        getChildren().add(browser)
    }
    
    def createSpacer() : Node = {
        var spacer = new Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)
        return spacer
    }
    
    def update() = browser.getEngine.load(path)

    override def layoutChildren() = {
        val w = getWidth()
        val h = getHeight()
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER)
    }

    override def computePrefWidth(height : Double) : Double = {
        return 750;
    }

    override def computePrefHeight(width : Double) : Double = {
        return 500;
    }
}