package main.scala.web

import javax.script.ScriptEngineManager

class MainRunner {
  
  
  def run = {
    val engine = new ScriptEngineManager().getEngineByMimeType("text/javascript")
    val result = engine.eval("""alchemy.begin({
        	dataSource: "actors.json", 
        	nodeCaption: 'name', 
        	nodeMouseOver: 'name',
            cluster: true,
            clusterColours: ["#1B9E77","#D95F02","#7570B3","#E7298A","#66A61E","#E6AB02"]})""")
    println(result)
  }
}