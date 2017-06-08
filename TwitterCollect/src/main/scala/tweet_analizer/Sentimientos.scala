package tweet_analizer

import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._
import scala.io.Source

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.IndexedWord
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations


class Sentimientos extends Serializable{
  
  var dict : HashMap[String, Float] = new HashMap[String, Float]()
  
  
  val PATH_DICT = "pruebas/TwitterCollect/src/main/resources/diccionario/"
  val NAME_NEGATIVE = "negative-words.txt"
  val NAME_POSITIVE = "positive-words.txt"
  val START = 36
  
  val NEGATIVE_VALUE = 1
  val POSITIVE_VALUE = 3
  
  def getDict = this.dict
  
  
  def leeDiccionarios() = {
    
    var inc = 1
    for(line <- Source.fromFile(PATH_DICT + NAME_NEGATIVE)(scala.io.Codec("ISO-8859-1")).getLines){
      if(inc > 35){
        this.dict.put(line, NEGATIVE_VALUE)
      }
      else inc += 1
    }
    
    inc = 1
    for(line <- Source.fromFile(PATH_DICT + NAME_POSITIVE)(scala.io.Codec("ISO-8859-1")).getLines){
      if(inc > 35){
        this.dict.put(line, POSITIVE_VALUE)
      }
      else inc += 1
    }
    
  }
  
  def optionToFloat(value : Option[Float]) : Float = value match {
    case Some(x:Float) => x
    case _ => 2.0f
  }
  
  
  def niega(punt : Float) : Float = punt match{
    case 1.0 => 3.0f
    case 3.0 => 1.0f
    case (x:Float) => x
  }
  
  def sentimientosMezcla(text : String) : Float = {
    val sentDicc = this.sentimientosDiccionario(text)
    val sentStand = this.sentimientoStandford(text)
    if(sentDicc==2 && sentStand==2) return 2.0f
    else if(sentDicc==2) return 0.8f*sentStand + 0.3f*sentDicc
    else if(sentStand==2) return 0.8f*sentDicc + 0.3f*sentStand
    else return (sentDicc + sentStand)/2
  }
  
  def sentimientosMezcla(sentDicc : Float, sentStand : Float) : Float = {
    if(sentDicc==2 && sentStand==2) return 2.0f
    else if(sentDicc==2) return 0.8f*sentStand + 0.3f*sentDicc
    else if(sentStand==2) return 0.8f*sentDicc + 0.3f*sentStand
    else return (sentDicc + sentStand)/2
  }
  
  def sentimientosDiccionario(text : String) : Float = {
     var words = text.split("\\s+")
     if(words.length > 2){
       if(words(0)=="not") return niega(trataListaModificadores(words.toList.slice(1,words.toList.length)))
       else return trataListaModificadores(words.toList)
     }
     else if(words.length == 2){
       if(words(0)=="not") return niega(optionToFloat(this.dict.get(words(1))))
       else return trataModificadorDiccionario(words(0), words(1))
     }
     else if(words.length == 1){
       return optionToFloat(this.dict.get(words(0)))
     }
     else return 2.0f
  }
  
  def trataModificadorDiccionario(word : String, mod : String) : Float = {
    val sentMod = sentimientosDiccionario(mod)
    val sentWord = sentimientosDiccionario(word)
    if(mod!=""){
      if(sentWord == 1){
        if(sentMod == 1) return 3
        else return sentWord
      }
      else if(sentWord == 3){
        if(sentMod == 3) return 1
        return return sentWord
      }
      else return sentWord
    }
    else return sentWord
  }
  
  def trataModificadorDiccionario(word : String, sentMod : Float) : Float = {
    val sentWord = sentimientosDiccionario(word)
    if(sentWord == 1){
      if(sentMod == 1) return 3
      else return sentWord
    }
    else if(sentWord == 3){
      if(sentMod == 3) return 1
      return return sentWord
    }
    else return sentWord
  }
  
  def trataListaModificadores(lista : List[String]) : Float = {
    lista.foldRight(2.0f)(trataModificadorDiccionario)
  }
  
  def sentimientoStandford(text : String) : Float = {
    var props = new java.util.Properties();
	  props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
	  
	  var pipeline = new StanfordCoreNLP(props);
    
  	var document = new Annotation(text);
  	pipeline.annotate(document);
  	
  	var sentiment = 0
  	var sentences = document.get(classOf[SentencesAnnotation]);
  	for(sentence <- sentences) {
  	  val treeSent = sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree]);
      sentiment = RNNCoreAnnotations.getPredictedClass(treeSent);
  	}
  	return sentiment
  }
  
  def sentimientoNeutroPalabra( word : IndexedWord) : Boolean = {
    val sent = sentimientosMezcla(sentimientosDiccionario(word.word),sentimientoStandford(word.word))
    return (sent >= 1.5 && sent < 2.5)
  }
  
  
}