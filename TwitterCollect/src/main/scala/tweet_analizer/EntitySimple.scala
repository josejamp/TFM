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

class EntitySimple {
  
  var ent : List[IndexedWord] = List()
  var adj : List[IndexedWord] = List()
  var rep : HashMap[String, String] = new HashMap()
  
  
  def entityCreator(sentence : SemanticGraph, word : String) = {
    this.ent = List()
    this.rep.clear()
    var it = sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"compound", null, null)).toList
    if(it.length == 0) treatEmpty(word, sentence)
    else{
      it.foreach { x => 
  
        treatEdge(x, word, sentence)
          
      }
    }
  }
  
  
  def treatEdge (x : SemanticGraphEdge, word : String, sentence : SemanticGraph) = {
          
    if(x.getSource.word.equalsIgnoreCase(word)){
      this.ent = (x.getSource() :: this.ent)
      this.rep.put(word, word)
      this.ent = List.concat(this.ent, treatSentenceAux(sentence, x.getTarget()))
    }
    else if(x.getTarget.word.equalsIgnoreCase(word)){
      this.ent = (x.getTarget() :: this.ent)
      this.rep.put(word, word)
      this.ent = List.concat(this.ent, treatSentenceAux(sentence, x.getSource()))
    }
    
  }
  
  def treatEmpty(word : String, sentence : SemanticGraph) = {
      this.ent = (sentence.getNodeByWordPattern(word) :: this.ent)
      this.rep.put(word, word)
  }
  
  def treatSentenceAux(sentence : SemanticGraph, word : IndexedWord) : List[IndexedWord] = {
    
    var ent_aux : List[IndexedWord] = List()
    
    if(!this.rep.contains(word.word)){
      //if(word.get(classOf[NamedEntityTagAnnotation]).equals("ORGANIZATION") || 
       //  word.get(classOf[PartOfSpeechAnnotation]).equals("NNP")) 
            ent_aux = (word :: ent_aux)
      this.rep.put(word.word,word.word)
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"compound", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"conj", null, null, "and")).toList.foreach { x =>  

        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"conj", null, null, "&")).toList.foreach { x =>  

        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatSentenceAux(sentence, x.getSource()))
        }
      }
      
    }
    return ent_aux;
  }
  
  
  def treatAdj(sentence : SemanticGraph) = {
    this.rep.clear()
    this.adj = List()
    this.ent.foreach{ word => 
      this.adj = List.concat(this.adj, treatWordAdjectives(sentence, word))
    }
  }
  
  def treatWordAdjectives(sentence : SemanticGraph, word : IndexedWord) : List[IndexedWord] = {
    
    var ent_aux : List[IndexedWord] = List()
    
    if(!this.rep.contains(word.word)){
      this.rep.put(word.word,word.word)
      if(!this.ent.contains(word) 
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("NNP")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("PRP")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("PRP$")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("DT")
          && !word.get(classOf[PartOfSpeechAnnotation]).equals("LS"))
          ent_aux = (word :: ent_aux)
       sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"nsubj", null, null)).toList.foreach { x =>  
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"ccomp", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"nmod", null, null, "of")).toList.foreach { x =>  
        
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"dobj", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      //Negacion -> mover?
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"neg", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"amod", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"nmod:poss", null, null)).toList.foreach { x =>  

        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"appos", null, null)).toList.foreach { x =>  

        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"acl:relcl", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
      //Negacion -> mover?
      sentence.findAllRelns(new GrammaticalRelation(Language.UniversalEnglish,"xcomp", null, null)).toList.foreach { x =>  
        
        if(x.getSource.word.equals(word.word) && !this.rep.contains(x.getTarget().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getTarget()))
        }
        else if(x.getTarget.word.equals(word.word) && !this.rep.contains(x.getSource().word())){
          ent_aux = List.concat(ent_aux, treatWordAdjectives(sentence, x.getSource()))
        }
      }
      
    }
    return ent_aux;
  }
  
  def getEntity() : List[IndexedWord] = {
    return this.ent
  }
  
  def getModifiers() : List[IndexedWord] = {
    return this.adj
  }
  
  def getPrettyEntity() : String = {
    var aux = ""
    for(w <- this.ent){
      aux += w.word() + " "
    }
    return aux
  }
  
  def getPrettyModifiers() : String = {
    var aux = ""
    for(w <- this.adj){
      if(w.word.equals("n't"))
          aux += "not" + ", "
      else aux += w.word() + ", "
    }
    return aux
  }
  
  
}