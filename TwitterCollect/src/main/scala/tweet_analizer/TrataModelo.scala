package tweet_analizer

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap

import edu.stanford.nlp.international.Language
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphEdge
import edu.stanford.nlp.trees.GrammaticalRelation
import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation
import edu.stanford.nlp.wordseg.CorpusDictionary

class TrataModelo(mod : ModeloBajoNivel, lista_negra : List[String], sentimientos : Sentimientos) extends Serializable{
  
  def this(mod : ModeloBajoNivel) = this(mod, List[String](), new Sentimientos())
  
  var model : ModeloBajoNivel = mod
  
  var ent : Jerarquia = new Jerarquia()
  var inter : List[String] = List()
  
  var listaNegra : List[String] = lista_negra
  
  var sent : Sentimientos = sentimientos
  
  def getEnt = this.ent
  def getInter = this.inter
  
  def initEntAndAdj(sentence : String){
    this.ent = this.model.identifyFullEntity(sentence)
    print("Entidad identificada: " + this.ent + "\n")
    this.inter = this.model.identifyIntereses(sentence)
  }
  
  def treatAll(original : String, sentence : SemanticGraph) = {
    this.treatEntidades(original, sentence)
    this.treatIntereses(original, sentence)
  }
  
  def treatEntidades(original : String, sentence : SemanticGraph) = {
    var res : List[String] = List()
    var repet : HashMap[String, String] = new HashMap()
    for((word,v)<-this.ent.nivel){
      //print("WORD: " + word + "\n")
      var adj_aux : List[String] = List()
      sentence.getAllNodesByWordPattern(word.toLowerCase()).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, repet)._2)))
        //print("Rep: " + repet + "\n")
      }
      sentence.getAllNodesByWordPattern(word.capitalize).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, repet)._2)))
        //print("Rep: " + repet + "\n")
      }
      if(adj_aux.length != 0){
        //print("Lower: " + this.ent.getDeepChildren(word) + "\n")
        if(this.leafRepeated(this.ent.getDeepChildren(word), repet)){
          repet.clear()
        }
        else {
          res = List.concat(res, adj_aux)
          this.model.addModificadores(word.toLowerCase(), this.ent.copy(), word.toLowerCase(), adj_aux)
        }
      }
      treatEntidadesAux(original, sentence, v._2, repet)
      repet.clear()
    }
    
  }
  
  def treatEntidadesAux(original : String, sentence : SemanticGraph, jer : Jerarquia, repet : HashMap[String, String]) : Unit = {
    var res : List[String] = List()
    var repet_local : HashMap[String, String] = new HashMap()
    
    for((w,_)<-repet){
      repet_local.put(w, w)
    }
    
    for((word,v)<-jer.getNivel){
      //print("WORD: " + word + "\n")
      var adj_aux : List[String] = List()
      sentence.getAllNodesByWordPattern(word.toLowerCase()).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, repet_local)._2)))
        //print("Rep: " + repet_local + "\n")
      }
      sentence.getAllNodesByWordPattern(word.capitalize).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, repet_local)._2)))
        //print("Rep: " + repet_local + "\n")
      }
      if(adj_aux.length != 0){
        //print("Lower: " + this.ent.getDeepChildren(word) + "\n")
        if(this.leafRepeated(this.ent.getDeepChildren(word), repet_local)){
          repet_local.clear()
        }
        else {
          res = List.concat(res, adj_aux)
          this.model.addModificadores(word.toLowerCase(), this.ent.copy(), word.toLowerCase(), adj_aux)
        }
      }
      treatEntidadesAux(original, sentence, v._2, repet_local)
      repet_local.clear()
    }
  }
  
  /*
  def treatEntidades(original : String, sentence : SemanticGraph) = {
    var res : List[String] = List()
    this.ent.foreach{ word => 
      //print("WORD: " + word + "\n")
      var adj_aux : List[String] = List()
      sentence.getAllNodesByWordPattern(word.toLowerCase()).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false)._2)))
        //print("Rep: " + this.rep + "\n")
      }
      sentence.getAllNodesByWordPattern(word.capitalize).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false)._2)))
        //print("Rep: " + this.rep + "\n")
      }
      if(adj_aux.length != 0){
        //print("Lower: " + this.ent.getDeepChildren(word) + "\n")
        if(this.leafRepeated(this.ent.getDeepChildren(word))){
          this.rep.clear()
        }
        else {
          res = List.concat(res, adj_aux)
          this.model.addModificadores(word.toLowerCase(), this.ent.copy(), word.toLowerCase(), adj_aux)
        }
      }
    }
  }
  */
  
  def leafRepeated(l : List[String], rep : HashMap[String,String]) : Boolean = {
    l.foreach{ x =>
      if(rep.contains(x.toLowerCase())) return true
    }
    return false
  }
  
  def treatIntereses(original : String, sentence : SemanticGraph) = {
    var res : List[String] = List()
    var rep : HashMap[String, String] = new HashMap()
    this.inter.foreach{ word => 
      var adj_aux : List[String] = List()
      sentence.getAllNodesByWordPattern(word.toLowerCase()).foreach { indexed => 
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, rep)._2)))
      }
      sentence.getAllNodesByWordPattern(word.capitalize).foreach { indexed =>  
        adj_aux = List.concat(adj_aux, this.orderMods(this.cleanMods(treatWordAdjectives(sentence, indexed, false, rep)._2)))
      }
      if(adj_aux.length != 0)
        res = List.concat(res, adj_aux)
        this.model.addModificadores(word.toLowerCase(), this.ent.copy() ,this.model.getLeafEntity(original), adj_aux)
    }
  }
  
  def treatWordAdjectives(sentence : SemanticGraph, word : IndexedWord, neg : Boolean, repet : HashMap[String,String]) : (List[IndexedWord],List[String]) = {
    
    var ent_aux : List[IndexedWord] = List()
    var mod_aux : List[String] = List()
    
    if(!repet.contains(word.word.toLowerCase())){
      repet.put(word.word.toLowerCase(),word.word.toLowerCase())
      
      if(this.addWord(word)){
          ent_aux = (word :: ent_aux)
      }
      
      val gramRel = List(("nsubj",false,null),("ccomp",false,null),("nmod",false,"of"),("dobj",false,null),
          ("neg",true,null),("amod",false,null),("nmod:poss",false,null),("appos",false,null),
          ("acl:relcl",false,null),("xcomp",false,null),("dep",false,null),("compound",false,null),
          ("advmod",true,null),("nmod:tmod",false,null))
          
       gramRel.foreach{elem => 
         val (ents, mods) = findRelns(sentence, elem._1, elem._2, elem._3, word, neg, repet)
           ent_aux = List.concat(ent_aux, ents)
           mod_aux = List.concat(mod_aux, mods)
       }
      
    }
    return (ent_aux, mod_aux);
  }
  
  def findRelns(sentence : SemanticGraph, reln : String, nueva_neg : Boolean, extra : String, word : IndexedWord, neg : Boolean, repet : HashMap[String,String]) : (List[IndexedWord],List[String]) = {
    
    var ent_aux = List[IndexedWord]()
    var mod_aux = List[String]()
    
    if(extra == null){
     sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,reln, null, null)).toList.foreach { x =>  
        val (ents, mods) = this.treatNode(x, sentence, word, neg, nueva_neg, repet)
        ent_aux = List.concat(ent_aux, ents)
        mod_aux = List.concat(mod_aux, mods)
      }
    }
    else{
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,reln, null, null,extra)).toList.foreach { x =>  
        val (ents, mods) = this.treatNode(x, sentence, word, neg, nueva_neg, repet)
        ent_aux = List.concat(ent_aux, ents)
        mod_aux = List.concat(mod_aux, mods)
      }
    }
    return (ent_aux, mod_aux)
    
  }
  
  
  def treatNode( x : SemanticGraphEdge, sentence : SemanticGraph, word : IndexedWord, old_neg : Boolean, new_neg : Boolean, repet : HashMap[String,String]) : (List[IndexedWord],List[String]) = {
    var ent_aux = List[IndexedWord]()
    var mod_aux = List[String]()
    if(x.getSource.word.toLowerCase().equals(word.word.toLowerCase()) && !repet.contains(x.getTarget().word().toLowerCase())){
          val (ents, mods) = treatWordAdjectives(sentence, x.getTarget(), new_neg, repet)
          ent_aux = List.concat(ent_aux, ents)
          if(this.addWord(x.getTarget())){
            if(!new_neg) mod_aux = (x.getTarget().word.toLowerCase() :: mods)
            else{
              mod_aux = ((word.word.toLowerCase() + " " + x.getTarget().word.toLowerCase()) :: mods)
            }
          }
          else mod_aux = mods
    }
    else if(x.getTarget.word.toLowerCase().equals(word.word.toLowerCase()) && !repet.contains(x.getSource().word().toLowerCase())){
      val (ents, mods) = treatWordAdjectives(sentence, x.getSource(), new_neg, repet)
      ent_aux = List.concat(ent_aux, ents)
      if(this.addWord(x.getSource())){
        if(!new_neg) mod_aux = (x.getSource().word.toLowerCase() :: mods)
        else{
          mod_aux = ((word.word.toLowerCase() + " " + x.getSource().word.toLowerCase()) :: mods)
        }
      }
      else mod_aux = mods
    }
    return (ent_aux, mod_aux)
  }
  
  def listOfIndexedToString(list : List[IndexedWord]) : List[String] = {
    return list.map { x => x.word().toLowerCase() }
  }
  
  def addWord(word : IndexedWord) : Boolean = {
      return (!this.ent.toList().contains(word.word.toLowerCase()) && !this.inter.contains(word.word.toLowerCase())
          && !this.listaNegra.contains(word.word().toLowerCase())
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("NNP")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("PRP")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("PRP$")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("DT")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("LS")
          && !isHashtag(word)
          && !isTweeterUser(word)
          && (!isVerb(word) || !sentimientos.sentimientoNeutroPalabra(word)))
  }
  
  def isVerb(word : IndexedWord) : Boolean = word.get(classOf[PartOfSpeechAnnotation]) match {
    case "VB" => true
    case "VBD" => true
    case "VBG" => true
    case "VBN" => true
    case "VBP" => true
    case "VBZ" => true
    case _ => false
  }
  
  def isHashtag(word : IndexedWord) : Boolean = {
    return word.word.startsWith("#")
  }
  
  def isTweeterUser(word : IndexedWord) : Boolean = {
    return word.word.startsWith("@")
  }
  
  
  def cleanMods(mods : List[String]) : List[String] = {
    var nueva_mods = List[String]()
    var enc = false
    mods.foreach { x => 
      mods.foreach { y =>  
        if(x != y){
          y.split("\\s+").foreach { word =>
            if(word == x) enc = true
          }
        }
      }
      if(!enc) nueva_mods = (x :: nueva_mods)
      else enc = false
    }
    return nueva_mods
  }
  
  def orderMods(mods : List[String]) : List[String] = {
   val nueva = mods.map { x => 
      this.changeTokens(x)
    }
   print("Lista ordenada: " + nueva + "\n")
   val acumulada = acumulaRecursivo(nueva,0)
   print("Lista acumulada: " + acumulada + "\n")
   return acumulada
  }
  
  def changeTokens(elem : String) : String = {
    var aux = new String()
    var dev = new String()
    elem.split("\\s+").foreach { word =>
        if(negationWord(word)) dev += "not"
        else aux += word + " "
    }
    return (dev + " " + aux).replaceAll("""(?m)\s+$""", "")
  }
  
  def negationWord(word : String) = {
    word match {
      case "not" => true
      case "n't" => true
      case _ => false
    }
  }
  
    
  def acumulaRecursivo(l : List[String], init : Int) : List[String] = {
    if(l.length == 0) return List[String]()
    else if(l.length == 1 || init >= l.length){
      return l
    }
    else{
      var pos = 1
      var nueva_init = init + 1
      var fin = false
      var acc = List[String]()
      var l_dev = List[String]()
      while(!fin && pos < l.length){
        val (nuevo,result) = acumulaPalabras(l(init),l(pos))
        if(result){
          fin = true
          l_dev = List.concat((nuevo :: acc), l.slice(pos+1,l.length))
          nueva_init = 0
        }
        else{
          acc = (l(pos) :: acc)
          pos += 1
        }
      }
      if(fin) return acumulaRecursivo(l_dev, nueva_init)
      else return acumulaRecursivo(l, nueva_init)
    }
  }
  
  
  def acumulaPalabras(word1 : String, word2 : String) : (String, Boolean) = {
    var l1 = word1.split("\\s+").toList
    var l2 = word2.split("\\s+").toList
    l1 = l1.filter { x => x != "" }
    l2 = l2.filter { x => x != "" }
    print("L1: " + l1 + "\n")
    print("L2: " + l2 + "\n")
    if((l1.length >1 && l2.length >= 1) || (l1.length >=1 && l2.length > 1)){
      if(l1(0) == l2(l2.length-1)){
        return (List.concat(l2.slice(0,l2.length-1),l1.slice(0,l1.length)).mkString(" "),true)
      }
      else if(l1(l1.length-1) == l2(0)){
        return (List.concat(l1.slice(0,l1.length-1),l2.slice(0,l2.length)).mkString(" "),true)
      }
      else return ("", false)
    }
    else return ("",false)
  }
  
}