package main.scala.gui.controller

import main.scala.Jerarquia
import main.scala.Puntuacion
import main.scala.dataBase.TraduccionDatos
import main.scala.dataBase.Consultas
import main.scala.io.GraphFileManager
import main.scala.io.EscribeJSON
import main.scala.gui.Browser
import main.scala.gui.ChartCreator

import javafx.scene.control.Label
import javafx.scene.chart.XYChart
import javafx.scene.layout.Pane


class TreeController(jer : Jerarquia, graphFileManag : GraphFileManager, lNombre : Label, lPuntPropia : Label, lPuntHijos : Label, lMenciones : Label) {
  
  def this(jer : Jerarquia, graphFileManag : GraphFileManager, lNombre : Label, lPuntPropia : Label, lPuntHijos : Label, lMenciones : Label, browRef : Browser) = {
    this(jer,graphFileManag,lNombre,lPuntPropia,lPuntHijos,lMenciones)
    this.browserRef = browRef
  }
  
  def this(jer : Jerarquia, graphFileManag : GraphFileManager, lNombre : Label, lPuntPropia : Label, lPuntHijos : Label, lMenciones : Label, charRef : Pane) = {
    this(jer,graphFileManag,lNombre,lPuntPropia,lPuntHijos,lMenciones)
    this.chartRef = charRef
  }
  
  
  var jerarquiaModelo : Jerarquia = jer
  var graphFileManager : GraphFileManager = graphFileManag
  
  var labelNombre = lNombre
  var labelPuntPropia = lPuntPropia
  var labelPuntHijos = lPuntHijos
  var labelMenciones = lMenciones
  
  var browserRef : Browser = null
  var chartRef : Pane = null
  
  def getJerarquia = this.jerarquiaModelo
  
  
  def getDataByID(id : String) : (String,String,String,String) = {
    val res = jer.getNodeById(id)
    return (res._1,res._2._1.getPuntuacionPropia.toString, res._2._1.getPuntuacionHijos.toString(), res._2._1.getRepeticiones.toString)
  }
  
  def setLabels(nombre : String, puntPropia : String, puntHijos : String, menciones : String) = {
      labelNombre.setText(nombre)
      labelPuntPropia.setText(correctLabelContent(puntPropia))
      labelPuntHijos.setText(correctLabelContent(puntHijos))
      labelMenciones.setText(menciones)
  }
  
  def correctLabelContent(text : String) : String = {
    if(text=="-1.0") return "No hay datos"
    else return text
  }
  
  def updateHTML(id : String) = {
    val (nodos, relaciones) = if(id!="principal") TraduccionDatos.getArbolParaHTML(id) else TraduccionDatos.getArbolParaHTML()
    EscribeJSON.escribe(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "graphResources/data.json", nodos, relaciones)
    graphFileManager.restoreHTML()
    graphFileManager.writeHTML()
    browserRef.update()
  }
  
  def elementDoubleClicked(id : String, chartCreator : ChartCreator) = {
    if(browserRef==null){
      chartCreator.setLastID(id)
      this.updateChart(if(chartCreator.xAxisIsJerarquias()) chartCreator.getBarChartFromId() else chartCreator.getLineChartFromId())
    }
    else{
      this.updateHTML(id)
    }
  }
  
  
  def updateChart(newChart : XYChart[_,_]) = {
    this.chartRef.getChildren.clear()
    this.chartRef.getChildren.add(newChart)
  }
  
  def getChildrenByID(id : String, res : String, dateRequired : Boolean) : List[(String,Float)] = {
    if(id == "principal")
      this.jerarquiaModelo.getChildrenInfo().map{ x => 
        getAskedData(x, res)
      }
    else
      if(!dateRequired){
        this.jerarquiaModelo.getChildrenByParentId(id).map{ x =>
          getAskedData(x, res)
        }
      }
      else getDataByDataBaseAccess(id, res)
  }
  
  def getAskedData( original : (String, Puntuacion, Jerarquia, String, String, String), res : String) : (String, Float) = res match {
    case "Menciones" => (original._1,original._2.getRepeticiones)
    case "Resumen" => (original._1,original._2.getResumen)
    case "Puntuacion propia" => (original._1,original._2.getPuntuacionPropia)
    case "Puntuacion hijos" => (original._1,original._2.getPuntuacionHijos)
    case _ => (original._1,original._2.getResumen)
  }
  
  def getDataByDataBaseAccess(id : String, res : String) : List[(String,Float)] = res match{
    case "Menciones" => TraduccionDatos.mencionesGrafica(id)
    case "Resumen" => TraduccionDatos.resumenGrafica(id)
    case "Puntuacion propia" => TraduccionDatos.puntuacionPropiaGrafica(id)
    case "Puntuacion hijos" => TraduccionDatos.puntuacionHijosGrafica(id)
    case _ => TraduccionDatos.mencionesGrafica(id)
  }
  
}