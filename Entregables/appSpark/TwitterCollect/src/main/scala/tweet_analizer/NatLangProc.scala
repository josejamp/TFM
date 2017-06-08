package tweet_analizer

import java.util.Properties
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.util.CoreMap

object NatLangProc{

	var props = new java.util.Properties();
	props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	  
	var pipeline = new StanfordCoreNLP(props);

  def naturalLang(text: String) : List[SemanticGraph] = {

	var document = new Annotation(text);

	pipeline.annotate(document);
	
	var sentences = document.get(classOf[SentencesAnnotation]);
  	var devolver : List[SemanticGraph] = List[SemanticGraph]();

    for(sentence <- sentences) {
		devolver = sentence.get(classOf[CollapsedCCProcessedDependenciesAnnotation]) :: devolver;
	}
	
	return devolver
  }





}