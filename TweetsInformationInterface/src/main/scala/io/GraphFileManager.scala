package main.scala.io

import java.io.IOException
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

class GraphFileManager(pathHTML : String, pathJSON : String, pathTemplateHTML : String) {
  
  var pathHtml = pathHTML
  var pathJson = pathJSON
  var pathTemplateHtml = pathTemplateHTML
  
  var CONF = """
     nodeTypes: {"label": ["Entidad",
                           "Elemento",
                           "Modificador"]},
     nodeStyle: {
        "Entidad": {
            borderWidth : 0,
            color: function(d) {
              var punt_propia = d.getProperties().punt_propia;
              var punt_hijos = d.getProperties().punt_hijos;
              var resumen = -1.0;
              if(punt_propia <0 && punt_hijos < 0){
                resumen = -1.0;
              }
              else if(punt_propia < 0)
                resumen = punt_hijos;
              else if(punt_hijos < 0)
                resumen = punt_propia;
              else resumen = (punt_propia + punt_hijos) / 2;
              if(resumen >= 0 && resumen < 1) return "#ff1a1a";
              else if(resumen >= 1 && resumen < 1.5) return "#ff4d4d";
              else if(resumen >= 1.5 && resumen < 2.5) return "#f2f2f2";
              else if(resumen >= 2.5 && resumen < 3) return "#b3ffb3";
              else if(resumen >= 3 && resumen <= 4) return "#66ff66";
              else return "#f2f2f2"
            },
            radius : function(d) {
               var menciones = d.getProperties().menciones;
               if(menciones==0) menciones=1;
               if(menciones > 2000) menciones = 2000
               return 10+((10/2000)*menciones)
            }
        },
        "Elemento":{
            color:  function(d) {
              var resumen = d.getProperties().punt_propia
              if(resumen >= 0 && resumen < 1) return "#ff1a1a";
              else if(resumen >= 1 && resumen < 1.5) return "#ff4d4d";
              else if(resumen >= 1.5 && resumen < 2.5) return "#f2f2f2";
              else if(resumen >= 2.5 && resumen < 3) return "#b3ffb3";
              else if(resumen >= 3 && resumen <= 4) return "#66ff66";
              else return "#f2f2f2"
            },
            borderWidth : 0,
            radius : function(d) {
               var menciones = d.getProperties().menciones;
               if(menciones > 2000) menciones = 2000
               return 8+((8/2000)*menciones)
            }
        },
        "Modificador": {
            color: function(d) {
                        if(d.getProperties().points >= 0 && d.getProperties().points < 1) return "#ff1a1a";
                        else if(d.getProperties().points >= 1 && d.getProperties().points < 1.5) return "#ff4d4d";
                        else if(d.getProperties().points >= 1.5 && d.getProperties().points < 2.5) return "#f2f2f2";
                        else if(d.getProperties().points >= 2.5 && d.getProperties().points < 3) return "#b3ffb3";
                        else if(d.getProperties().points >= 3 && d.getProperties().points <= 4) return "#66ff66";
                        else return "#f2f2f2"
                    },
            borderWidth : 0,
            radius : 5
        }
      }
    """
  val ALCHEMY_BEGIN =   """alchemy.begin({
  	  dataSource: $dataSource, 
  	  nodeCaption: function(d) {
                        if(d.label=="Modificador")
                          return d.name;
                        else if(d.label=="Entidad") return d.name.toUpperCase();
                        else return d.name.charAt(0).toUpperCase() + d.name.slice(1) 
                    },
  	  nodeMouseOver: function(d) {
                        if(d.label=="Modificador")
                          return d.name;
                        else if(d.label=="Entidad") return d.name.toUpperCase();
                        else return d.name.charAt(0).toUpperCase() + d.name.slice(1) 
                    },
  	  edgeCaption: function(d) {
                        if(d.type=="modificador")
                        return d.repeticiones; else return d.type 
                    }, 
  	  edgeMouseOver: function(d) {
                        if(d.type=="modificador")
                        return d.repeticiones; else return d.type 
                    },
      nodeCaptionsOnByDefault: true,
  	  """ + CONF +
  	  """})"""
  
  def writeHTML() = {
    val toWrite = scala.io.Source.fromFile(pathJson).mkString
    var alchemyText = ALCHEMY_BEGIN.replace("$dataSource", toWrite)
    
    var htmlText = scala.io.Source.fromFile(pathHTML).mkString
    htmlText = htmlText.replace("$script", alchemyText)
    
    val file = new File(pathHtml)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(htmlText)
    bw.close()
  }
  
  def restoreHTML() = {
    val toWrite = scala.io.Source.fromFile(pathTemplateHtml).mkString
    
    val file = new File(pathHtml)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(toWrite)
    bw.close()
  }
  
  
}