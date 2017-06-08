package main.scala.dataBase

import scala.collection.mutable.HashMap


class RelacionesYAtributos {
  
  var relTable = new HashMap[String,List[String]]()
  
  
  def getTable = relTable
  
  def getRelaciones() : List[String] = {
    var l = List[String]()
    relTable.foreach{ elem  =>
      l = elem._1 :: l
    }
    l
  }
  
  def getAtributos(rel : String) : List[String] = {
    var l = List[String]()
    optionToListOfString(relTable.get(rel)).foreach{ elem  =>
      l = elem :: l
    }
    l
  }
  
  def addRelation(rel : String) = {
    if(!relTable.contains(rel)) relTable.put(rel, List[String]())
  }
  
  def addAttribute(rel : String, att : String) = {
    if(relTable.contains(rel) && !optionToListOfString(relTable.get(rel)).contains(att)){
      var nueva = att :: optionToListOfString(relTable.get(rel))
      relTable.put(rel, nueva)
    }

  }
  
  
  def optionToListOfString(lista : Option[List[String]]) : List[String] = lista match{
    case (Some(x:List[String])) => x
    case _ => List[String]()
  }
  
}