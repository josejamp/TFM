package main.scala


import scala.collection.mutable.HashMap
import Jerarquia._


class Jerarquia extends Iterable[String] with Serializable{
  
  var nivel : HashMap[String, (Puntuacion,Jerarquia,String,String,String)] = new HashMap()
  
  def add(name : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), new Jerarquia(), "none","",""))
  }
  
  def add(name : String, relPadre : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), new Jerarquia(), relPadre,"",""))
  }
  
  def add(name : String, relPadre : String, dependencia : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), new Jerarquia(), relPadre, dependencia,""))
  }
  
  def add(name : String, punt : Puntuacion) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, new Jerarquia(), "none","",""))
  }
  
  def add(name : String, punt : Puntuacion, relPadre : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, new Jerarquia(), relPadre,"",""))
  }
  
  def add(name : String, jerarquia : Jerarquia) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), jerarquia, "none","",""))
  }
  
  def add(name : String, jerarquia : Jerarquia, relPadre : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), jerarquia, relPadre,"",""))
  }
  
  def add(name : String, jerarquia : Jerarquia, relPadre : String, dependencia : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (new Puntuacion(-1.0f,-1.0f), jerarquia, relPadre, dependencia,""))
  }
  
  def add(name : String, jerarquia : Jerarquia, punt : Puntuacion) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, jerarquia, "none","",""))
  }
  
  def add(name : String, jerarquia : Jerarquia, punt : Puntuacion, relPadre : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, jerarquia, relPadre,"",""))
  }
  
  def add(name : String, jerarquia : Jerarquia, punt : Puntuacion, relPadre : String, dependencia : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, jerarquia, relPadre, dependencia,""))
  }
  
  def add(name : String, jerarquia : Jerarquia, punt : Puntuacion, relPadre : String, dependencia : String, identificador : String) = {
    if(!this.nivel.contains(name.toLowerCase())) this.nivel.put(name.toLowerCase(), (punt, jerarquia, relPadre, dependencia,identificador))
  }
  
  def getNivel : HashMap[String, (Puntuacion,Jerarquia,String,String,String)] = {
    return this.nivel
  }
  
  def getDebil(name : String) : Boolean = {
    return optionToJerarquia(this.nivel.get(name))._4 != ""
  }
  
  def getDependencia(name : String) : String = {
    return optionToJerarquia(this.nivel.get(name))._4
  }
  
  def setOverride(name : String, jerarquia : Jerarquia) = {
    if(this.nivel.contains(name)){
      val v2 = optionToJerarquia(this.nivel.get(name))
      this.nivel.remove(name)
      this.add(name, jerarquia, v2._1, v2._3, v2._4)
    }
    else this.add(name, jerarquia)
  }
  
  def setOverride(name : String, jerarquia : Jerarquia, puntuacion : Puntuacion) = {
    if(this.nivel.contains(name)){
      val v2 = optionToJerarquia(this.nivel.get(name))
      this.nivel.remove(name)
      this.add(name, jerarquia, puntuacion, v2._3, v2._4)
    }
    else this.add(name, jerarquia, puntuacion)
  }
  
  def set(name : String, jerarquia : Jerarquia) : Boolean = {
      if(this.nivel.contains(name.toLowerCase())){
        for((k1,v1)<-jerarquia.getNivel){
          if(!optionToJerarquia(this.nivel.get(name.toLowerCase()))._2.set(k1,v1._2)) optionToJerarquia(this.nivel.get(name.toLowerCase()))._2.add(k1,v1._2,v1._1,v1._3,v1._4)
        }
        return true
      }
      else{
        var res = false
        for((k2,v2) <- this.nivel){
           res = res || v2._2.set(name, jerarquia)
        }
        return res
      }
      
  }
  
  def set(name : String, jerarquia : Jerarquia, puntuacion : Puntuacion) : Boolean = {
      if(this.nivel.contains(name.toLowerCase())){
        optionToJerarquia(this.nivel.get(name.toLowerCase()))._1.setPuntuacion(puntuacion)
        for((k1,v1)<-jerarquia.getNivel){
          if(!optionToJerarquia(this.nivel.get(name.toLowerCase()))._2.set(k1,v1._2,v1._1)) optionToJerarquia(this.nivel.get(name.toLowerCase()))._2.add(k1,v1._2,v1._1,v1._3,v1._4)
        }
        return true
      }
      else{
        var res = false
        for((k2,v2) <- this.nivel){
           res = res || v2._2.set(name, jerarquia, puntuacion)
        }
        return res
      }
      
  }
  
  def setPuntuacion(name : String, punt : Puntuacion) : Unit = {
    if(this.nivel.contains(name.toLowerCase())){
      optionToJerarquia(this.nivel.get(name.toLowerCase()))._1.setPuntuacion(punt)
    }
    else{
      for((k2,v2) <- this.nivel){
         v2._2.setPuntuacion(name, punt)
      }
    }
  }
  
  def setPuntuacionManteRepeticiones(name : String, punt : Puntuacion) : Unit = {
    if(this.nivel.contains(name.toLowerCase())){
      optionToJerarquia(this.nivel.get(name.toLowerCase()))._1.setPuntuacionSinRepeticiones(punt)
    }
    else{
      for((k2,v2) <- this.nivel){
         v2._2.setPuntuacionManteRepeticiones(name, punt)
      }
    }
  }
  
  def move(name : String, destino : String, relPadre : String) = {
    if(this.nivel.contains(name)) this.set(destino, optionToJerarquia(this.nivel.get(name))._2)
    else{
      var aux = new Jerarquia()
      aux.add(name, relPadre)
      if(!this.set(destino, aux)) this.add(destino, aux)
    }
  }
  
  def getProfundidad() : Integer = {
    var ac = 0
    if(!this.empty){
      for((k2,v2) <- this.nivel){
        ac += v2._2.getProfundidad()
      }
    }
    ac += 1
    return ac
  }
  
  def calculaPuntuacion() : Unit = {
    for((k1,v1) <- this.nivel){
      v1._2.calculaPuntuacion()
      var puntsHijos = List[Float]()
      var puntPersonal = List[Float]()
      for((k2,v2) <- v1._2.getNivel){
        if(v2._3 == "modificador"){
          (1 to v2._1.getRepeticiones).foreach{ _ =>
             if(v2._1.getRepeticiones > 0){
                puntPersonal = (v2._1.getResumen :: puntPersonal)
             }
             else  puntPersonal = (v2._1.getResumen :: puntPersonal)
          }
        }
        else if (v2._3 == "elemento"){
          puntPersonal = (v2._1.getResumen :: puntPersonal)
        }
        else {
          puntsHijos = (v2._1.getResumen :: puntsHijos)
        }
      }
      //print(k1 + " hijos: " + puntsHijos + "\n")
      //print(k1 + " personal: " + puntPersonal + "\n")
      v1._1.setResumenHijos(puntsHijos)
      v1._1.setResumenPropio(puntPersonal)
    }
  }
  
  def empty() : Boolean = {
    return this.nivel.size == 0
  }
  
  def exists(name : String) : Boolean = {
    return this.nivel.contains(name.toLowerCase())
  }
  
  def notExistsWholeTree(name : String) : Boolean =  {
    if(this.nivel.contains(name.toLowerCase())) return false
    else {
      for((k,v) <- this.nivel){
         if(!v._2.notExistsWholeTree(name)) return false
      }
    }
    return true
  }
  
  def hasChild(child : String) : Boolean = {
    return this.nivel.contains(child.toLowerCase())
  }
  
  def getChild(child : String) : Option[(Puntuacion,Jerarquia,String,String,String)] = {
    return this.nivel.get(child.toLowerCase())
  }
  
  def getDeepChildren(child : String) : List[String] = {
    var l = List[String]()
    for((k,v) <- this.nivel){
      if(k.equalsIgnoreCase(child)){
         l = List.concat(l,v._2.getChildren())
      }
      else{
         l = List.concat(l , v._2.getDeepChildren(child))
      }
    }
    return l
  }
  
  def getNodeById(id : String) : (String,(Puntuacion,Jerarquia,String,String,String)) = {
    var ret = ("",(new Puntuacion(-1.0f,-1.0f), new Jerarquia,"","",""))
    var enc = false
    for((k,v) <- this.nivel){
      if(v._5.equalsIgnoreCase(id)){
         ret = (k,v)
         enc = true
      }
      else if(!enc){
        var aux = v._2.getNodeByIdAux(id)
        enc = aux._3
        if(enc) ret = (aux._1,aux._2)
      }
    }
    return ret
  }
  
  def getNodeByIdAux(id : String) : (String,(Puntuacion,Jerarquia,String,String,String),Boolean) = {
    var ret = ("",(new Puntuacion(-1.0f,-1.0f), new Jerarquia,"","",""),false)
    var enc = false
    for((k,v) <- this.nivel){
      if(v._5.equalsIgnoreCase(id)){
         ret = (k,v,true)
      }
      else if(!enc){
        var aux = v._2.getNodeByIdAux(id)
        enc = aux._3
        if(enc) ret = aux
      }
    }
    return ret
  }
  
  def getChildrenInfo() : List[(String,Puntuacion,Jerarquia,String,String,String)] = {
     var l = List[(String,Puntuacion,Jerarquia,String,String,String)]()
     for((k,v) <- this.nivel){
       l = (k,v._1,v._2,v._3,v._4,v._5) :: l
     }
     return l
  }
  
  def getChildrenByParentId(id : String) : List[(String,Puntuacion,Jerarquia,String,String,String)] = {
    var l = List[(String,Puntuacion,Jerarquia,String,String,String)]()
    var enc = false
    for((k,v) <- this.nivel){
      if(v._5.equalsIgnoreCase(id)){
         for((k2,v2) <- v._2.nivel){
           l = (k2,v2._1,v2._2,v2._3,v2._4,v2._5) :: l
         }
         enc = true
      }
      else if(!enc){
        l = List.concat(v._2.getChildrenByParentId(id),l)
      }
    }
    return l
  }
  
  def getLowerNodes() : List[String] = {
    var l = this.getChildren()
    var res = List[String]()
    for((k,v) <- this.nivel){
      res = List.concat(res, l.filterNot { x => x.equalsIgnoreCase(k) })
    }
    return res
  }
  
  def getLowerNodes(child : String) : List[String] = {
    var l = this.getDeepChildren(child)
    var res = List[String]()
    for((k,v) <- this.nivel){
      res = List.concat(res, l.filterNot { x => x.equalsIgnoreCase(k) })
    }
    return res
  }
  
  def getChildren() : List[String] = {
    var l = List[String]()
    for((k,v) <- this.nivel){
      l = List.concat(l,(k :: v._2.getChildren()))
    }
    return l
  }
  
  def getLeafs() : List[String] = {
    var lista = List[String]()
    for((k,v) <- this.nivel){
      if(v._2.empty()) lista = ( k :: lista)
      else lista = ( lista ::: v._2.getLeafs() )
    }
    return lista
  }
  
  def copy() : Jerarquia = {
    var jer = new Jerarquia()
    for((k1,v1) <- this.nivel){
      jer.add(k1, v1._2.copy(), v1._1.copy(), v1._3, v1._4)
    }
    return jer
  }
  
  
  override def toString() : String = {
     var acc = ""
     var i = 3
     for((k,v) <- this.nivel){
       acc += "Puntuacion: " + v._1 + "\n"
       acc += "Relacion: " + v._3 + "\n"
       acc += "Dependencia: " + v._4 + "\n"
       acc += k + " [ " + "\n"
       acc += v._2.toStringAux(i)
       acc += "] \n"
     }
     return acc
  }
  
  def toStringAux(i : Int) : String = {
     var acc = ""
     var l = i + 3
     for((k,v) <- this.nivel){
       for(j <- 0 to i) acc += " "
       acc += "Puntuacion: " + v._1 + "\n"
       acc += "Relacion: " + v._3 + "\n"
       acc += "Dependencia: " + v._4 + "\n"
       acc += k + " [ " + "\n"
       acc += v._2.toStringAux(l)
       for(j <- 0 to i) acc += " "
       acc += "] \n"
     }
     return acc
  }
  
  override def toList() : List[String] = {
    var acc : List[String] = List()
    for((k,v) <- this.nivel){
      acc = List.concat(acc, (k :: v._2.toList())) 
    }
    return acc
  }
  
  def toListStrings() : List[String] = {
    var acc : List[String] = List()
    for((k,v) <- this.nivel){
      acc = List.concat(acc, (k :: v._2.toList())) 
    }
    return acc
  }
  
  override def iterator = this.toList().iterator
  

}

object Jerarquia {
  
  def optionToJerarquia(oj : Option[(Puntuacion,Jerarquia,String,String,String)]) : (Puntuacion,Jerarquia,String,String,String) = oj match{
    case Some(x:(Puntuacion,Jerarquia,String,String,String)) => x
    case _ => (new Puntuacion(0.0f,0.0f),new Jerarquia(),"none","","")
  }
  
}
