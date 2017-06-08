package tweet_analizer

import org.apache.commons.cli.{Options, ParseException, PosixParser}
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.feature.HashingTF
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

/* *
 Objeto basado en el objeto Utils de com.databricks.apps.twitter_classifier
*/
object Utils {

  val numFeatures = 1000
  val tf = new HashingTF(numFeatures)

  val CONSUMER_KEY = "consumerKey"
  val CONSUMER_SECRET = "consumerSecret"
  val ACCESS_TOKEN = "accessToken"
  val ACCESS_TOKEN_SECRET = "accessTokenSecret"

  val THE_OPTIONS = {
    val options = new Options()
    options.addOption(CONSUMER_KEY, true, "Twitter OAuth Consumer Key")
    options.addOption(CONSUMER_SECRET, true, "Twitter OAuth Consumer Secret")
    options.addOption(ACCESS_TOKEN, true, "Twitter OAuth Access Token")
    options.addOption(ACCESS_TOKEN_SECRET, true, "Twitter OAuth Access Token Secret")
    options
  }

  def parseCommandLineWithTwitterCredentials(args: Array[String]) = {
    val parser = new PosixParser
    try {
      val cl = parser.parse(THE_OPTIONS, args)
      System.setProperty("twitter4j.oauth.consumerKey", cl.getOptionValue(CONSUMER_KEY))
      System.setProperty("twitter4j.oauth.consumerSecret", cl.getOptionValue(CONSUMER_SECRET))
      System.setProperty("twitter4j.oauth.accessToken", cl.getOptionValue(ACCESS_TOKEN))
      System.setProperty("twitter4j.oauth.accessTokenSecret", cl.getOptionValue(ACCESS_TOKEN_SECRET))
      cl.getArgList.toArray
    } catch {
      case e: ParseException =>
        System.err.println("Parsing failed.  Reason: " + e.getMessage)
        System.exit(1)
    }
  }
  
  def parseWordsToLookFor(arg: String) : Array[String] = {
	if (arg.equals("[]")) return Array()
	else return arg.stripPrefix("[").stripSuffix("]").trim.replaceAll("""(?m)\s+$""", "").split(",")
  }
  
  def cleanTweet(arg: Array[String]) : String = {
	val lista = arg.map( x => replaceURLs(x));
	var frase = ""
	for(i <- 0 to lista.length-1){
		if(i != lista.length-1){
			frase = frase + lista(i) + " "
		}
		else frase = frase + lista(i)
	}
	return frase
  }
  
  def replaceURLs(arg: String) : String = {
	return arg.replaceAll("""(http|ftp|https)://([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])?""", "<link>")
  }

  def getAuth = {
    Some(new OAuthAuthorization(new ConfigurationBuilder().build()))
  }


  def featurize(s: String): Vector = {
    tf.transform(s.sliding(2).toSeq)
  }

  object IntParam {
    def unapply(str: String): Option[Int] = {
      try {
        Some(str.toInt)
      } catch {
        case e: NumberFormatException => None
      }
    }
  }
}
