package main.scala.gui

import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.chart.LineChart

import main.scala.Jerarquia
import main.scala.dataBase.TraduccionDatos
import main.scala.gui.controller.TreeController

class ChartCreator(cont : TreeController) {
  
  var controller = cont
  
  var lastID : String = "principal"
  var xAxis = "Subjerarquias"
  var yAxis = "Resumen"
  
  
  def setLastID(id : String) = {
    this.lastID = id
  }
  
  def setYAxis(nuevo : String) = {
    this.yAxis = nuevo
  }
  
  def setXAxis(nuevo : String) = {
    this.xAxis = nuevo
  }
  
  def xAxisIsJerarquias() : Boolean = this.xAxis == "Subjerarquias"
  
  
  def getDefaultChart() : BarChart[String,Number] = {
    
    var x = new CategoryAxis()
    var y = new NumberAxis()
    x.setLabel("Jerarquias")
    y.setLabel("Puntuacion")
    y.setAutoRanging(false)
    y.setLowerBound(0)
    y.setUpperBound(5)
    
    var bc = new BarChart[String,Number](x,y)
    bc.setTitle("Sentimientos")
    
    var info = new XYChart.Series[String,Number]()
    info.setName("Sentimientos")
    val data = this.controller.getChildrenByID("principal","Resumen",false)
    print("Data: "  + data + "\n")
    data.foreach{ x =>
      info.getData().add(new XYChart.Data(x._1,transformUnknownData(x._2)))
    }
    
    bc.getData.addAll(info)
    
    return bc
  }
  
  def getBarChartFromId() : BarChart[String,Number] = {
    var x = new CategoryAxis()
    var y = new NumberAxis()
    x.setLabel("Jerarquias")
    if(yAxis != "Menciones") y.setLabel("Puntuacion " + yAxis)
    else y.setLabel(yAxis)
    
    if(yAxis != "Menciones"){
      y.setAutoRanging(false)
      y.setLowerBound(0)
      y.setUpperBound(5)
    }
    else  y.setAutoRanging(true)
    
    var bc = new BarChart[String,Number](x,y)
    bc.setTitle("Sentimientos")
    
    var info = new XYChart.Series[String,Number]()
    info.setName(yAxis)
    val data = this.controller.getChildrenByID(lastID,yAxis,false)
    print("Data: "  + data + "\n")
    data.foreach{ x =>
      info.getData().add(new XYChart.Data(x._1,transformUnknownData(x._2)))
    }
    
    bc.getData.addAll(info)
    
    return bc
  }
  
  def getLineChartFromId() : LineChart[String,Number] = {
    var x = new CategoryAxis()
    var y = new NumberAxis()
    x.setLabel("Tiempo")
    if(yAxis != "Menciones") y.setLabel("Puntuacion " + yAxis)
    else y.setLabel(yAxis)
    
    if(yAxis != "Menciones"){
      y.setAutoRanging(false)
      y.setLowerBound(0)
      y.setUpperBound(5)
    }
    else  y.setAutoRanging(true)
      
    var bc = new LineChart[String,Number](x,y)
    bc.setTitle("Sentimientos")
    
    var info = new XYChart.Series[String,Number]()
    info.setName(yAxis)
    val data = this.controller.getChildrenByID(lastID,yAxis,true).sortBy(_._1)
    print("Data: "  + data + "\n")
    data.foreach{ x =>
      info.getData().add(new XYChart.Data(x._1,transformUnknownData(x._2)))
    }
    
    bc.getData.addAll(info)
    
    return bc
    
  }
  
  
  def transformUnknownData(data : Float) : Float = {
    if(data < 0) 2.0f
    else data
  }
  
}