package tweet_analizer

import scala.collection.mutable.HashMap
import scala.xml.{Elem, Node, XML, Utility}

class RelacionesGrafo extends Serializable{
  
 var rel : HashMap[String, HashMap[String, String]] = new HashMap[String, HashMap[String, String]]()
  
  
  def getRelaciones = this.rel
  
  def leerPlugin(path : String) = {
    
   val inicial = Utility.trim(XML.loadFile(path))
   
   var interior : HashMap[String, String] = new HashMap[String, String]()
   inicial.child.foreach { x => 
       interior.put(optionToString(x.attribute("name")), x.child(0).toString)
   }
   
   this.rel.put(optionToString(inicial.attribute("name")), interior)
    
  }
  
  def getTipo(padre : String, relacion : String) : (Boolean,String) = this.rel.get(padre) match {
    case Some(x:HashMap[String,String]) => x.get(relacion) match {
      case Some(y:String) => (true,y)
      case _ => (false, relacion)
    }
    case _ => (false, relacion)
  }
  
  def optionToString(at : Option[Seq[Node]]) : String = at match {
    case Some(x:Seq[Node]) => x.text
    case _ => "error"
  }
  
  
}