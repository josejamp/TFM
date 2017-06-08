package tweet_analizer

import java.io.File

import com.google.gson._
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


/**
 * Collect at least the specified number of tweets into json text files.
 */
object Collect {
  private var numTweetsCollected = 0L
  private var partNum = 0
  private var gson = new Gson()
  
  def filtraTweets(stream: Array[String], lookFor: List[String]) : Boolean ={
	var enc = false
	var i = 0
	while(!enc && (i<lookFor.length)){
		if(stream.contains(lookFor(i).toLowerCase)) enc = true
		else i=i+1
	}
	return (enc || (lookFor.length == 0))
  }
  
  def creaModelo() : ModeloBajoNivel = {
	val listaOntologias = List("pruebas/TwitterCollect/src/main/resources/jerarquias/politicos.xml")
    val listaPlugins = List("pruebas/TwitterCollect/src/main/resources/plugins/plugin_politicos.xml")
    
    var relacionesGrafo = new RelacionesGrafo()
    listaPlugins.foreach { path => relacionesGrafo.leerPlugin(path) }
    
    var jer = new Jerarquia()
    var inter = List[String]()
    listaOntologias.foreach { ont => 
        var trataXML = new LectorJerarquia(ont)
        trataXML.parseXML(relacionesGrafo)
        for((k1,v1) <- trataXML.getJerarquia.nivel){ jer.add(k1, v1._2, v1._1, v1._3) }
        inter = trataXML.getIntereses ++ inter
    }
    var modelo = new ModeloBajoNivel(jer,inter.distinct)
    
    return modelo
  }

  def main(args: Array[String]) {
    // Process program arguments and set properties
    if (args.length < 7) {
      System.err.println("Usage: " + this.getClass.getSimpleName +
        "<databaseDirection> <databaseUser> <databasePassword> <timeToExecute> <intervalInSeconds> <partitionsEachInterval> <wordsToLookFor>")
      System.exit(1)
    }
    val Array(databaseDirection, databaseUser, databasePassword, Utils.IntParam(timeToExecute),  Utils.IntParam(intervalSecs), Utils.IntParam(partitionsEachInterval), wordsToLookFor) =
      Utils.parseCommandLineWithTwitterCredentials(args)
    if (!ManejadorBaseDatos.setServer(databaseDirection.toString, databaseUser.toString, databasePassword.toString)) {
      System.err.println("ERROR - conecting to database")
    }

    println("Initializing Streaming Spark Context...")
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(intervalSecs))

	val stream = TwitterUtils.createStream(ssc, Utils.getAuth)
	val keyWords = Utils.parseWordsToLookFor(wordsToLookFor.toString)
	
	/*
	val tweetStream = stream.filter {t =>
		t.getLang == "en"
	}.foreachRDD{ (rdd, time) => ManejadorBaseDatos.guardaPrueba(DateUtils.toHoursEpoch(System.currentTimeMillis/1000)) }
	*/
	
	
	val modeloReferencia = creaModelo()
	print(ManejadorBaseDatos.guardaJerarquia(modeloReferencia.getEntidades(),DateUtils.toHoursEpoch(System.currentTimeMillis/1000)))
	
	val tweetStream = stream.filter {t =>
		t.getLang == "en"
		
	}.filter{ t =>
		filtraTweets(t.getText.split(" ").map(_.toLowerCase), List.concat(keyWords, modeloReferencia.getEntidades.toList))
		
   }.foreachRDD { (rdd, time) =>
		rdd.foreach{ t =>
		
			var modelo = creaModelo()
				print("Modelo entidades: " + modelo.getEntidades() + "\n")
				print("Modelo intereses: " + modelo.getIntereses() + "\n")
			val blackList = List("has","have","having","had","rt")
			var trataModelo = new TrataModelo(modelo, blackList, modelo.getSentimientos)
			val sentence = Utils.cleanTweet(t.getText.split("\\s+|$").map(_.toLowerCase))
			val treatedSentences = NatLangProc.naturalLang(sentence)
			treatedSentences.foreach{ treatedSentence =>
					print("Sentence: " + sentence + "\n")
					print("treated sentence: " + treatedSentence + "\n")
				trataModelo.initEntAndAdj(sentence)
					print("Trata Modelo entidades: " + trataModelo.getEnt + "\n")
					print("Trata Modelo intereses: " + trataModelo.getInter + "\n")
				trataModelo.treatEntidades(sentence, treatedSentence)
					print("Modelo modificadores: " + modelo.getModificadores() + "\n")
				trataModelo.treatIntereses(sentence, treatedSentence)
					print("Modelo modificadires: " + modelo.getModificadores() + "\n")
			}
			//( modelo.getModificadores().toString(), t.getText)
			modelo.asignaPuntuaciones()
			print(ManejadorBaseDatos.guardaJerarquia(modelo.getEntidades(), DateUtils.toHoursEpoch(DateUtils.parseDateString(t.getCreatedAt.toString()))) + "\n")
			
		}
   //}.map( new GsonBuilder().disableHtmlEscaping().create().toJson(_))
   }
	
	

    ssc.start()
	ssc.awaitTerminationOrTimeout(timeToExecute * 60 * 1000)
	ssc.stop()
  }
}
