package tweet_analizer

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap


class ModeloBajoNivel(jer : Jerarquia, inter : List[String]) extends Serializable{
  
  def this(jer : Jerarquia, inter : List[String], lee : Boolean) = {
    this(jer,inter)
    if(lee)
       this.sentimientos.leeDiccionarios()
  }
  
  
  var entidades : Jerarquia = jer
  
  var intereses : List[String] = inter
  
  var modificadores : HashMap[String, HashMap[String,(Jerarquia,List[(Int,String)],Int)]] = new HashMap()
  
  var sentimientos : Sentimientos = new Sentimientos()
  
  
  def getEntidades() : Jerarquia = {
    return entidades
  }
  
  def getIntereses() : List[String] = {
    return intereses
  }
  
  def getModificadores() : HashMap[String, HashMap[String,(Jerarquia,List[(Int,String)],Int)]] = {
    return modificadores
  }
  
  def getSentimientos = this.sentimientos
  
  def addModificador(word : String, jer : Jerarquia, ent : String, mod : String){
      var inside = this.OptionToHashMap(modificadores.get(word))
      if(this.hasWord(OptionToListOfStrings(inside.get(ent)), mod)){
        val rep = this.getRep(OptionToListOfStrings(inside.get(ent)),mod)
        val num_men = OptionToNumRep(inside.get(ent))
        var lista = List[(Int,String)]()
        OptionToListOfStrings(inside.get(ent)).foreach{ x =>
          if(!(x._1 == rep && x._2.equalsIgnoreCase(mod))) lista = (x._1,x._2)::lista
        }
        inside.put(ent, (jer, ((rep+1,mod)::lista),num_men))
      }
      else{
        inside.put(ent, (jer, (this.getRep(OptionToListOfStrings(inside.get(ent)),mod)+1,mod)::OptionToListOfStrings(inside.get(ent)),1))
      }
      modificadores.put(word, inside)
  }
  
  def addModificador(word : String, jer : Jerarquia, ent : String, mod : String, repet : Int){
      var inside = this.OptionToHashMap(modificadores.get(word))
      if(this.hasWord(OptionToListOfStrings(inside.get(ent)), mod)){
        val rep = this.getRep(OptionToListOfStrings(inside.get(ent)),mod)
        val num_men = OptionToNumRep(inside.get(ent))
        var lista = List[(Int,String)]()
        OptionToListOfStrings(inside.get(ent)).foreach{ x =>
          if(!(x._1 == rep && x._2.equalsIgnoreCase(mod))) lista = (x._1,x._2)::lista
        }
        inside.put(ent, (jer, ((rep+repet,mod)::lista),num_men))
      }
      else{
        inside.put(ent, (jer, (this.getRep(OptionToListOfStrings(inside.get(ent)),mod)+repet,mod)::OptionToListOfStrings(inside.get(ent)),1))
      }
      modificadores.put(word, inside)
  }
  
  def addModificadores(word : String, jer : Jerarquia, ent: String, mods : List[String]){
      mods.foreach { mod => this.addModificador(word, jer, ent, mod) }
  }
  
  def addModificadores(table : HashMap[String, HashMap[String,(Jerarquia,List[(Int,String)])]]) = {
    for((k,v)<-table){
      for((k2,v2)<-v){
        v2._2.foreach{ x =>
          this.addModificador(k,v2._1,k2,x._2,x._1)
        }
      }
    }
  }
  
  def hasWord(lista : List[(Int,String)], word : String) : Boolean = {
    lista.foreach{ x =>
      if(x._2.toLowerCase().replaceAll("\\s", "") == word.toLowerCase().replaceAll("\\s", "")) return true
    }
    return false
  }
  
  def getRep(lista : List[(Int,String)], word : String) : Int = {
    lista.foreach{ x =>
      if(x._2.toLowerCase().replaceAll("\\s", "") == word.toLowerCase().replaceAll("\\s", "")) return x._1
    }
    return 0
  }
  
  def OptionToListOfStrings(oj : Option[(Jerarquia,List[(Int,String)],Int)]) : List[(Int,String)] = oj match{
    case Some(x:(Jerarquia,List[(Int,String)],Int)) => x._2
    case _ => List()
  }
  
  def OptionToHashMap(oj : Option[HashMap[String,(Jerarquia,List[(Int,String)],Int)]]) : HashMap[String,(Jerarquia,List[(Int,String)],Int)] = oj match{
    case Some(x:HashMap[String,(Jerarquia,List[(Int,String)],Int)]) => x
    case _ => new HashMap()
  }
  
  def OptionToNumRep(oj : Option[(Jerarquia,List[(Int,String)],Int)]) : Int = oj match{
    case Some(x:(Jerarquia,List[(Int,String)],Int)) =>x._3
    case _ => 0
  }
  
  def identifyFullEntity(sentence : String) : Jerarquia = {
    var jer = identifyFullEntity(this.entidades, sentence)
    var max = 0
    /*
    for((k2,v2) <- jer.getNivel){
      var prof = v2._2.getProfundidad()
      if(prof > max){
        aux = new Jerarquia()
        aux.add(k2,v2._2,v2._1)
        max = prof
      }
    }
    * */
    //print("Jer antes:" + jer + "\n")
    var res = new Jerarquia()
    sentence.split("\\s+").foreach { x =>
      var aux = jerarquiaConPalabraRepetida(x, jer)
      if(aux._1){
        for((k,v)<- aux._2.getNivel){
          res.setOverride(k, v._2, v._1) 
        }
        //print("Jer actualizada \n")
      }
      //print("Jer devuelta:" +  x + ": " + res + "\n")
    }
    
    
    //print("Jer final:" + res + "\n")
    //return aux
    return res
  }
  
  def jerarquiaConPalabraRepetida(word : String, jer : Jerarquia) : (Boolean,Jerarquia) = {
    var apariciones = 0
    var res = new Jerarquia()
    var acc = false
    for((k2,v2) <- jer.getNivel){
      var (bool,tratada) = jerarquiaConPalabraRepetida(word,v2._2)
      res.setOverride(k2, tratada)
      //print("k: " + k2 + " ,res: " + res+ "\n")
      //print("k: " + k2 + " ,Children: " + res.getChildren() + "\n")
      if(res.getChildren().contains(word.toLowerCase())){
        apariciones += 1
        acc = true
      }
      else acc = acc || bool
    }
    if(res.empty()) res = jer
    
    //print("Apariciones: " + word + ": " + apariciones + "\n")
    //print("Jer repetida: " + word + ": " + res + "\n")
    
    if(apariciones > 1){
      var dev = new Jerarquia()
      var max = 0
      for((k2,v2) <- res.getNivel){
        var prof = v2._2.getProfundidad()
        if(prof > max){
          dev = new Jerarquia()
          dev.add(k2,v2._2,v2._1)
          max = prof
        }
      }
      return (true,dev)
    }
    else return (acc,jer)
  }
  
  def identifyFullEntity(entidad : Jerarquia, sentence : String) : Jerarquia = {
    var dev = new Jerarquia()
    for((k2,v2) <- entidad.getNivel){
      var aux = this.identifyFullEntity(v2._2,sentence)
      if(wordInSentence(k2, sentence) && (v2._4 == "" || wordInSentence(v2._4, sentence))) {
        //print("\n Word in sentence: " + k2 + "\n")
        var p = new Puntuacion(-1.0f,-1.0f)
        p.setRepeticiones(1)
        if(!dev.set(k2.toLowerCase(), aux, p)){
          dev.add(k2.toLowerCase(), aux, p)
        }
        else dev.setPuntuacion(k2.toLowerCase(), p)
      }
      else{
        for((k,v)<- aux.getNivel){
            var p = new Puntuacion(-1.0f,-1.0f)
            p.setRepeticiones(1)
           if(!dev.set(k.toLowerCase(),v._2,p)){
             dev.add(k.toLowerCase(), v._2, p)
           }
           else dev.setPuntuacion(k.toLowerCase(), p)
        }
      }
    }
    return dev
  }
  
  def wordInSentence(word : String, sentence : String) : Boolean = {
      sentence.split("\\s+").foreach { x =>
        if(x.toLowerCase().contains(word.toLowerCase())) return true
      }
      return false
  }
  
  def getLeafEntity(sentence : String) : String = {
    var dev = new String()
    this.entidades.getLeafs().foreach { x =>  
      sentence.split("\\s+").foreach { word =>
        if(word.equalsIgnoreCase(x)) dev = x.toLowerCase()
      }
    }
    return dev
  }
  
  def identifyIntereses(sentence : String) : List[String] = {
    var res = List[String]()
    intereses.foreach { interes => 
      sentence.split("\\s+").foreach { x =>
        if(x.toLowerCase().contains(interes.toLowerCase())){
          res = (interes.toLowerCase() :: res)
        }
      }
    }
    return res
  }
  
  def asignaPuntuaciones() = {
    this.sentimientos.leeDiccionarios()
    for((hijo,tabla_hijo) <- this.modificadores){
      for((padre, encontrados) <- tabla_hijo){
        var list_punt = List[Float]()
        var punt = new Puntuacion(-1.0f, -1.0f)
        var jer_ent = new Jerarquia()
        var jer_mods = new Jerarquia()
        var jer_original = encontrados._1
        var men_interes = encontrados._3
        encontrados._2.foreach { mod =>
          val puntuacion = this.sentimientos.sentimientosMezcla(mod._2)
          (1 to mod._1).foreach{ _ =>
            list_punt = ( puntuacion :: list_punt)
          }
          var p = new Puntuacion(puntuacion, -1.0f)
          p.setRepeticiones(mod._1)
          jer_mods.add(palabraRelevante(mod._2), p, "modificador")
        }
        punt.setResumenHijos(list_punt)
        if(this.intereses.contains(hijo)){
          jer_ent.add(hijo, jer_mods, new Puntuacion(-1.0f,-1.0f,men_interes),"elemento")
          jer_original.set(padre,jer_ent)
          for((k,v)<-jer_original.getNivel){
            if(!this.entidades.set(k, v._2)) this.entidades.add(k, v._2, v._1, v._3)
            else this.entidades.setPuntuacion(k, v._1)
          }
          this.entidades.setPuntuacionManteRepeticiones(hijo, punt)
        }
        else{
          jer_original.set(padre,jer_mods)
          for((k,v)<-jer_original.getNivel){
            if(!this.entidades.set(k, v._2, v._1)) this.entidades.add(k, v._2, v._1, v._3)
          }
          this.entidades.setPuntuacionManteRepeticiones(padre, punt)
        }
      }
    }
    this.entidades.calculaPuntuacion()
  }
  
  def negationWord(word : String) = {
    word match {
      case "not" => true
      case "n't" => true
      case _ => false
    }
  }
  
  def palabraRelevante(word : String) : String = {
    var dev = new String()
    val l = word.split("\\s+")
    if(negationWord(l(0))){
      dev += "not"
      return dev + " " + l(1)
    }
    else if(l(0)=="") return l(1)
    else return l(0)
  }
  

  
  
}