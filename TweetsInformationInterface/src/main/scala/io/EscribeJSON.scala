package main.scala.io


import scala.collection.mutable.ListBuffer

import org.anormcypher.CypherResultRow
import org.anormcypher.MetaDataItem
import org.anormcypher.MetaData

import java.io.IOException
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

object EscribeJSON {
  
  def escribe(path : String, valueNodes : Stream[CypherResultRow], valueEdges : Stream[CypherResultRow]) = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(creaString(valueNodes, true)+creaString(valueEdges, false))
    bw.close() 
  }
  
  def creaString(values : Stream[CypherResultRow], nodes : Boolean) : String = {
    var acc = ""
    if(nodes) acc = "{\n"
    if(nodes) acc += """"nodes":[""" + "\n"
    else acc += """"edges":[""" + "\n"
    values.foreach { row => 
      var metaData = List[(String,Boolean,String)]()
      var data = List[Any]()
      var labels = List[String]()
      var pos_label = -1
      var pos_type = -1
      for(n <-0 to row.productArity-1){
        //print("Row " + row.productElement(n) + ", tipo: " + row.productElement(n).getClass() + "\n")
        
        if(n%2==0){ //MetaData
          var pos = 0
          optionToMetaDataItem(MetaData.unapply(row.productElement(n).asInstanceOf[MetaData])).foreach { x => 
            val (name, bool, tipo) = optionToTuple(MetaDataItem.unapply(x))
            if(nodes){
              if(name == "label"){
                metaData = ("label", bool, tipo) :: metaData
                pos_label = pos
              }
              else if(name == "type"){
                metaData = ("type", bool, tipo) :: metaData
                pos_type = pos
              }
              else{
                  metaData = (name, bool, tipo) :: metaData
              }
            }
            else metaData = (name, bool, tipo) :: metaData
            pos += 1
          }
          //print("Pos label: " + pos_label + "\n")
        }
        else{ //Data
          var pos = 0
          row.productElement(n).asInstanceOf[List[Any]].foreach { x =>
            if(pos == pos_label){
              data = labelToCluster(x.asInstanceOf[ListBuffer[String]]) :: data
              pos_label = -1
              
            }
            else if(pos == pos_type){
              data = x.asInstanceOf[String] :: data
              pos_type = -1
            }
            else{
              data = x :: data
            }
            pos += 1
          }
        }
        
        
      }
      for(n <- 0 to metaData.length-1){
        if(n == 0) acc += "{\n" + """"""" + metaData(n)._1 + """": """" + data(n) + """"""" + ",\n"
        else if(n < metaData.length-1) acc += """"""" + metaData(n)._1 + """": """" + data(n) + """"""" + ",\n"
        else acc += """"""" + metaData(n)._1 + """": """" + data(n) + """"""" + "\n}"
      }
      acc += ",\n"
    }
    acc = acc.dropRight(2)
    acc += "\n"
    if(nodes) acc += "],\n"
    else acc += "]\n"
    if(!nodes) acc += "}"
    return acc
  }
  
  def optionToTuple(op : Option[(String,Boolean,String)]) : (String,Boolean,String) = op match {
    case Some(x:(String,Boolean,String)) => x
    case _ => ("",false,"")
  }
  
  def optionToMetaDataItem(md : Option[List[MetaDataItem]]) : List[MetaDataItem] = md match {
    case Some(x:List[MetaDataItem]) => x
    case _ => List[MetaDataItem]()
  }
  
  def labelToCluster(label : ListBuffer[String]) : String = label(0) match{
    case x => x
  }
  
  
}