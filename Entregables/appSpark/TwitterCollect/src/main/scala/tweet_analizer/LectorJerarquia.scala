package tweet_analizer

import scala.xml.{Elem, Node, XML, Utility}

class LectorJerarquia(path : String) extends Serializable{
  
  var xml : Node = Utility.trim(XML.loadFile(path))
  
  var jerarquia : Jerarquia = new Jerarquia()
  var intereses : List[String] = List[String]()
  
  def getJerarquia = this.jerarquia
  def getIntereses = this.intereses
  
  def getTitulo = optionToString(this.xml.attribute("name"))
  
  
  def parseXML(rg : RelacionesGrafo) = {
    //this.jerarquia.add(this.xml\@"name")
    this.xml.child.foreach { x => 
      //print("Label: " + x.label + "\n")
      
      this.jerarquia.add(optionToString(x.attribute("name")), trataHijos(x, rg, optionToString(x.attribute("name"))), x.label)
      
    }
    
  }
  
  def trataHijos(nodo : Node, rg : RelacionesGrafo, padre : String) : Jerarquia = {
    var aux = new Jerarquia()
    nodo.child.foreach { x => rg.getTipo(this.getTitulo, x.label) match{
      case (true,"elemento") => {
        if(!this.intereses.contains(nodo.child(0).toString())) this.intereses = (optionToString(x.attribute("name")).toLowerCase() :: this.intereses)
      }
      case (true,"entidad") => {
        aux.add(optionToString(x.attribute("name")), trataHijos(x, rg, optionToString(x.attribute("name"))), x.label, optionToWeakString(x.attribute("weak"),padre))
      }
      case (false,r) => {
        print("Interno: " + r + "\n")
      }
    }}
    
    return aux
  }
  
  def trataNodoInterno(nombre : String, nodo : Node, rg : RelacionesGrafo, original : String) = rg.getTipo(original, nodo.label) match{
    case (true,"elemento") => {
      if(!this.intereses.contains(nodo.child(0).toString())) this.intereses = ((nodo.child(0).toString().toLowerCase()) :: this.intereses)
    }
    case (true,"entidad") => {
      this.jerarquia.move(nombre, nodo.child(0).toString(), nodo.label)
    }
    case (false,r) => {
      print("Interno: " + r + "\n")
    }
  }
  
  
  def optionToString(at : Option[Seq[Node]]) : String = at match {
    case Some(x:Seq[Node]) => x.text
    case _ => "error"
  }
  
  def optionToWeakString(at : Option[Seq[Node]], padre : String) : String = at match {
    case Some(x:Seq[Node]) =>{
      if(x.text.equalsIgnoreCase("true")) return padre
      else return ""
    }
    case _ => ""
  }
  
  
}