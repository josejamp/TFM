package main.scala.gui.controller

import main.scala.dataBase.TraduccionDatos
import main.scala.dataBase.RelacionesYAtributos
import main.scala.DateUtils
import main.scala.gui.SearchResult
import main.scala.gui.UsualResult
import main.scala.gui.ModifierResult

import java.time.LocalDate


class SearchController {
  
  var attributesAndRelationships = TraduccionDatos.llenaRelacionesYAtributos()
  
  
  def getRelationships() = this.attributesAndRelationships.getRelaciones()
  def getAttributes(rel : String) = this.attributesAndRelationships.getAtributos(rel)
  
  def search(relName : String, attName : String, op : String, value : String) : List[SearchResult] = {
    if(relName == "modificador"){
      TraduccionDatos.traduceConsultaAritmeticaModificador(relName,attName,op,value).map( x => new ModifierResult(x._1,x._2,x._3))
    }
    else{
      TraduccionDatos.traduceConsultaAritmetica(relName,attName,op,value).map( x => new UsualResult(x._1,x._2,x._3,x._4,x._5))
    }
  }
  
  def search(relName : String, attName : String, op : String, value : String, date1 : LocalDate, date2 : LocalDate) : List[SearchResult] = {
    if(attName == "pertenece" || attName == "nombre" || attName == "puntuacion")
      search(relName,attName,op,value)
    else attName match{
      case "repeticiones" => TraduccionDatos.traduceConsultaFechaMencionesModificador(relName, attName, op, value, DateUtils.toHoursFromDays(date1.toEpochDay()), DateUtils.toHoursFromDays(date2.toEpochDay())).map{ x => new ModifierResult(x._1,x._2,x._3)}
      case "menciones" => TraduccionDatos.traduceConsultaFechaMenciones(relName, attName, op, value, DateUtils.toHoursFromDays(date1.toEpochDay()), DateUtils.toHoursFromDays(date2.toEpochDay())).map{ x => new UsualResult(x._1,x._2,x._3,x._4,x._5)}
      case "puntuacion hijos" => TraduccionDatos.traduceConsultaFechaPuntHijos(relName, attName, op, value, DateUtils.toHoursFromDays(date1.toEpochDay()), DateUtils.toHoursFromDays(date2.toEpochDay())).map{ x => new UsualResult(x._1,x._2,x._3,x._4,x._5)}
      case "puntuacion propia" => TraduccionDatos.traduceConsultaFechaPuntPropia(relName, attName, op, value, DateUtils.toHoursFromDays(date1.toEpochDay()), DateUtils.toHoursFromDays(date2.toEpochDay())).map{ x => new UsualResult(x._1,x._2,x._3,x._4,x._5)}
    }
  }
  
  def searchPertenencia(relName : String, depend : List[SearchResult]) : List[SearchResult] = {
    var res = List[SearchResult]()
    if(relName == "modificador"){
      depend.foreach { searchResult => 
        res = List.concat(res, TraduccionDatos.traduceConsultaPertenenciaModificador(relName,searchResult.getID).map( x => new ModifierResult(x._1,x._2,x._3))).distinct
      }
    }
    else{
      depend.foreach { searchResult => 
        res = List.concat(res, TraduccionDatos.traduceConsultaPertenencia(relName,searchResult.getID).map( x => new UsualResult(x._1,x._2,x._3,x._4,x._5))).distinct
      }
    }
    return res
  }
  
  def unifyResults(res : List[List[SearchResult]], and : Boolean) : List[SearchResult] = {
    print("Res: " + res + "\n")
    if(and){
      if(res.length > 1)
        res.tail.foldLeft(res.head){(l1,l2) => List.concat(l2.filter { x => l1.contains(x) },l1.filter { x => l2.contains(x) })}
      else
        res.head
    }
    else{
      res.foldLeft(List[SearchResult]()){(l1,l2) => List.concat(l2.filterNot { x => l1.contains(x) },l1 )}
    }
  }
  
  
}