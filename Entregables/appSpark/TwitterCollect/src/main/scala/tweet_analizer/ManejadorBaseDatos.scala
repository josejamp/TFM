package tweet_analizer

import org.anormcypher._ 
import java.net.URL

object ManejadorBaseDatos {
  
  var oldPath = ""
  implicit var connection = Neo4jREST()
  
  @throws(classOf[java.security.PrivilegedActionException])
  def setServer(path : String, user : String, password : String) : Boolean = {
     val neo4jURLString = util.Properties.envOrElse("DATA_BASE",path)
     val url : URL = new URL(neo4jURLString)
     print("Host: " + url.getHost + "\n")
     print("Port: " + url.getPort + "\n")
     val newConnection = Neo4jREST(url.getHost, url.getPort,"/db/data/", user, password)
     try{
       newConnection.sendQuery(Cypher("""MATCH (n:Root) RETURN n.name"""))
     }
     catch{
       case e : Any =>{
         return false
       }
     }
     connection = newConnection
     oldPath = path
     return true
   }
  
  def guardaJerarquia(jer : Jerarquia, date : Long) : Boolean = {
    var ok = true
    guardaRaiz()
     for((k,v)<-jer.getNivel){
       val (res, id) = guardaPrimerNivel(k,v,date)
       var l : List[String] = List()
       ok &= res && guardaJerarquiaAux(v._2, id, date)
     }
    return ok
  }
  
  def guardaRaiz() = {
    val query = Cypher("""
              MATCH (n:Root { name: "root" })
              RETURN n.name as name
              """)
    if(query.apply().length == 0){
      Cypher("""
          CREATE (n:Root { name: "root"})
          """).execute()
    }
  }
  
  def guardaPrimerNivel(k : String, v : (Puntuacion, Jerarquia, String, String, String), date : Long) : (Boolean, String) = {
    var ok = true
    var id = ""
    val query = Cypher("""
          MATCH (n:""" + this.getTipo(v._3) + """ { name: {nombre} })
          RETURN n.points_children as points_c, n.menciones as men, n.points_own as points_o, n.id as repID
          """).on("nombre" -> k)
      if(query.apply().length == 0){
        var men = v._1.getRepeticiones
        if(men < 0) men = 0
        var menciones = Array(date,men)
        id = java.util.UUID.randomUUID().toString()
        val result = Cypher("""
          CREATE (n:""" + this.getTipo(v._3) + """ { id: {uuid}, name: {nombre}, menciones : {menciones}, points_children: {punt}, points_own: {punt2} })
          """).on("uuid" -> id, "nombre" -> k, "menciones" -> menciones.toList ,"punt" -> v._1.getPuntuacionHijos, "punt2" -> v._1.getPuntuacionPropia).execute()
          print(k + " res primer nivel nuevo: " + result + "\n")
        ok = ok &  Cypher("""
          MATCH (n:Root { name: "root"}), (m {name: {nombre}})
          CREATE (n)-[r:""" + v._3 + """]->(m)
          """).on("nombre"->k).execute()
        ok = ok & result
      }
      else {
        query().foreach{ row =>
            val repID = row[String]("repID")
            val points_c : Float = row[BigDecimal]("points_c").floatValue()
            val points_o : Float = row[BigDecimal]("points_o").floatValue()
            var men = row[Seq[Long]]("men").toArray
            var men2 = v._1.getRepeticiones
            if(men2 < 0) men2 = 0
            val update = Cypher("""
              MATCH (n:""" + this.getTipo(v._3) + """ { name: {nombre}, id:{uuid} })
              SET n.menciones = {menciones}, n.points_children = {newPoints_c}, n.points_own = {newPoints_o}
              """).on("nombre" -> k, "uuid" -> repID, "menciones" -> ManejadorBaseDatos.actualizaMenciones(men, date, men2).toList, "newPoints_c" -> ManejadorBaseDatos.getPunt(v,points_c,true), "newPoints_o" -> ManejadorBaseDatos.getPunt(v,points_o,false)).execute()
              print(k + " res primer nivel repetido: " + update + "\n")
              ok = ok && update
              id = repID
        }
      }
    return (ok,id)
  }
  
  def guardaJerarquiaAux(jer : Jerarquia, idPadre : String, date : Long) : Boolean = {
    var ok = true
    for((k,v)<-jer.getNivel){
      if(v._3 == "modificador"){
        ok = trataModificador(k,v,idPadre, date)
      }
      else{
        val (existe, id, men, punts_c, punts_o) =  existeRelacion(k,v,idPadre)
        if(existe){
          ok = actualizaNodo(k,v, id, men, punts_c, punts_o, date) && guardaJerarquiaAux(v._2, id, date)
        }
        else{
          val (res, idCorrecto) = insertaNodo(idPadre,k,v,date)
          //ok = res && creaRelacion(k,v,idPadre,idCorrecto) && guardaJerarquiaAux(v._2, idCorrecto, date)
          ok = res && guardaJerarquiaAux(v._2, idCorrecto, date)
        }
      }
    }
    return ok
  }
  
  def existeRelacion(k : String, v : (Puntuacion, Jerarquia, String, String, String), idPadre : String) : (Boolean, String, Seq[Long], Float, Float) = {
     val queryRelation =  Cypher("""
      MATCH (n {id:{uuidPadre}})-[r]->(m {name:{nombre}})
      RETURN m.id as devID, m.menciones as men, m.points_children as punts_c, m.points_own as punts_o
      """).on("uuidPadre" -> idPadre, "nombre" -> k)
     if(queryRelation.apply().length == 0){
       return (false,"",Array[Long](),0.0f, 0.0f)
     }
     else{
       var ok = true
       var id = ""
       var menciones = new Array[Long](1)
       var puntuacion_c = 0.0f
       var puntuacion_o = 0.0f
       queryRelation().foreach{ row =>
         val devID = row[String]("devID")
         val men = row[Seq[Long]]("men")
         val punts_c : Float = row[BigDecimal]("punts_c").floatValue()
         val punts_o : Float = row[BigDecimal]("punts_o").floatValue()
         ok = true
         id = devID
         menciones = men.toArray
         puntuacion_c = punts_c
         puntuacion_o = punts_o         
       }
       return (ok, id, menciones, puntuacion_c, puntuacion_o)
     }
  }
  
  def actualizaNodo(k : String, v : (Puntuacion, Jerarquia, String, String, String), id : String, men : Seq[Long], punts_c : Float, punts_o : Float, date : Long) : Boolean = {
      var menciones = men.toArray
      var men2 = v._1.getRepeticiones
      if(men2 < 0) men2 = 0
     //print("Points_o : " + punts_o + "\n")
     //print("New Points_o : " + getPunt(v,punts_o,false) + "\n")
      /*
     val update = Cypher("""
              MATCH (n:""" + this.getTipo(v._3) + """ { name: {nombre}, id:{uuid} })
              SET  n.menciones = {menciones}, n.points_children = {newPoints_c}, n.points_own = {newPoints_o}
              """).on("nombre" -> k,"uuid" -> id, "menciones" -> ManejadorBaseDatos.actualizaMenciones(menciones, date, men2).toList,"newPoints_c" -> ManejadorBaseDatos.getPunt(v,punts_c,true), "newPoints_o" -> ManejadorBaseDatos.getPunt(v,punts_o,false)).execute()
              */
      val update = Cypher("""
              MATCH (n:""" + this.getTipo(v._3) + """ { name: {nombre}, id:{uuid} })
              SET  n.menciones = {menciones}, n.points_children = {newPoints_c}, n.points_own = {newPoints_o}
              """).on("nombre" -> k,"uuid" -> id, "menciones" -> ManejadorBaseDatos.actualizaMenciones(menciones, date, men2).toList,"newPoints_c" -> ManejadorBaseDatos.getPunt(v,punts_c,true), "newPoints_o" -> ManejadorBaseDatos.getPunt(v,punts_o,false)).execute()
              
              print(k + " res nodo repetido: " + update + "\n")
              
     return update
  }
  
  def insertaNodo(idPadre : String, k : String, v : (Puntuacion, Jerarquia, String, String, String), date : Long) : (Boolean, String) = {
      val id = java.util.UUID.randomUUID().toString()
      var men = v._1.getRepeticiones
      if(men < 0) men = 0
      var menciones = Array(date,men)
      val resultCreate = Cypher("""
          MATCH (m {id:{identificador}})
          CREATE UNIQUE (m)-[r:""" + v._3 + """]->(n:""" + this.getTipo(v._3) +""" { name: {nombre}, menciones : {menciones}, points_children: {punt_c}, points_own : {punt_o} })
          SET n.id = {uuid}
          """).on("identificador" -> idPadre, "uuid" -> id, "nombre" -> k, "menciones" -> menciones.toList, "punt_c" -> v._1.getPuntuacionHijos, "punt_o" -> v._1.getPuntuacionPropia).execute()
          print(k + " nodo nuevo: " + resultCreate + "\n")
      return (resultCreate, id)
  }
  
  def creaRelacion(k : String, v : (Puntuacion, Jerarquia, String, String, String), idPadre : String, idHijo : String) : Boolean = {
    val resultRelation = Cypher("""
          MATCH (n {id:{uuidPadre}}), (m {id:{uuidHijo}})
          CREATE (n)-[r:""" + v._3 + """]->(m)
          """).on("uuidPadre" -> idPadre, "uuidHijo" -> idHijo).execute()
          print(k + " res nueva relacion: " + resultRelation + "\n")
    return resultRelation
  }
  
  def trataModificador(k : String, v : (Puntuacion, Jerarquia, String, String, String), idPadre : String, date : Long) : Boolean = {
    var ok = true
    val qAlreadyEntityOrElement = Cypher("""
      MATCH (n{ name: {nombre}})
      WHERE n:Entidad OR n:Elemento
      RETURN n.id as identificador
      """).on("nombre" -> k)
    if(qAlreadyEntityOrElement.apply().length == 0){
      val query = Cypher("""
        MATCH (n:""" + "Modificador" + """{ name: {nombre}})
        RETURN n.name
        """).on("nombre" -> k)
      if(query.apply().length == 0){
        val resultCreate = Cypher("""
          MERGE (n:""" + this.getTipo(v._3) +""" {name: {nombre}, points: {punt} })
          """).on("nombre" -> k, "punt" -> v._1.getResumen).execute()
          //print(k + " res nuevo modificador: " + resultCreate + "\n")
       ok = ok && resultCreate
      }
      val queryRelation =  Cypher("""
        MATCH (n {id:{uuidPadre}})-[r:""" + v._3 + """ {date:{fecha}}]->(m {name:{nombre}})
        RETURN r.repeticiones as repet
        """).on("uuidPadre" -> idPadre, "nombre" -> k, "fecha" -> date)
      if(queryRelation.apply().length == 0){
        val resultRelation = Cypher("""
        MATCH (n {id:{uuidPadre}}), (m {name:{nombre}})
        CREATE (n)-[r:""" + v._3 + """{repeticiones: {rep}, date: {fecha}}]->(m)
        """).on("uuidPadre" -> idPadre, "nombre" -> k, "rep" -> v._1.getRepeticiones, "fecha" -> date).execute()
        //print(k + " res nueva relacion modificador: " + resultRelation + "\n")
        print(k + " res nueva relacion modificador: " + v._1.getRepeticiones + "\n")
        return ok && resultRelation
      }
      else{
        queryRelation().foreach{ row =>
            val repet = row[Int]("repet")
            val res = Cypher("""
             MATCH (n {id:{uuidPadre}})-[r:""" + v._3 + """]->(m {name:{nombre}})
             SET r.repeticiones = {rep}
             """).on("uuidPadre" -> idPadre, "nombre" -> k, "rep" -> (repet + v._1.getRepeticiones)).execute()
             //print(k + " res old relacion modificador: " + res + "\n")
             print(k + " res old relacion modificador: " + (repet + v._1.getRepeticiones) + "\n")
             ok = ok && res
        }
        return ok
      }
    }
    else{
      print("En else" + qAlreadyEntityOrElement.apply() + "\n")
      return true
    }
  }
  
  def borraTodo() : Boolean = {
    Cypher("""
      MATCH (n) DETACH
      DELETE n
      """).execute()
  }
  
  def borraMenosRepeticiones(repeticiones : Int) : Boolean = {
    Cypher("""
      MATCH (n)-[r]->(m:Modificador)
      WHERE r.repeticiones < {rep}
      DELETE r
      """).on("rep" -> repeticiones).execute() &&
      Cypher("""
      MATCH (m:Modificador)
      WHERE NOT (n)-[r]->(m)
      DELETE m
      """).on("rep" -> repeticiones).execute()
  }
  
  def getTipo(tipo : String) : String = tipo match{
    case "elemento" => "Elemento"
    case "modificador" => "Modificador"
    case "none" => "Entidad"
    case _ => "Entidad"
  }
  
  def guardaPrueba(date : Long) : Boolean = {
	Cypher("""
	CREATE (n:Prueba {fecha : {fecha}})
	""").on("fecha" -> date).execute()
  }
  
	    def getPunt(v : (Puntuacion, Jerarquia, String, String, String), punts : Float, child : Boolean) : Float = {
    if(child){
      if(v._1.getPuntuacionHijos < 0) return punts
      else if(punts < 0) return v._1.getPuntuacionHijos
      else return (punts + v._1.getPuntuacionHijos)/2
    }
    else{
      if(v._1.getPuntuacionPropia < 0) return punts
      else if(punts < 0) return v._1.getPuntuacionPropia
      else return (punts + v._1.getPuntuacionPropia)/2
    }
  }
  
  def resumen(punt_propia : Float, punt_hijos : Float) : Float = {
    if(punt_propia <0 && punt_hijos < 0){
      return -1.0f
    }
    else if(punt_propia < 0)
      return punt_hijos
    else if(punt_hijos < 0)
      return punt_propia
    else return (punt_propia + punt_hijos) / 2
  }
  
  def actualizaMenciones(menciones : Array[Long], date : Long, men : Int) : Array[Long] = {
    if(men > 0){
      var pos = contieneFecha(menciones, date)
      if(pos == -1){
        var nuevasMenciones = new Array[Long](menciones.length+2)
        var i = 0
        menciones.foreach { x => 
          nuevasMenciones.update(i, x)
          i = i+1
        }
        nuevasMenciones.update(i, date)
        nuevasMenciones.update(i+1, men)
        return nuevasMenciones
      }
      else{
        var nuevasMenciones = new Array[Long](menciones.length)
        var i = 0
        menciones.foreach { x => 
          nuevasMenciones.update(i, x)
          i = i+1
        }
        nuevasMenciones.update(pos, nuevasMenciones.apply(pos)+men)
        return nuevasMenciones
      }
    }
    else return menciones
  }
  
  def contieneFecha(menciones : Array[Long], date : Long) : Int = {
    var i = 0
    menciones.foreach { x => 
      if(x==date) return i+1
      else i = i+1
    }
    return -1
  }
  
}