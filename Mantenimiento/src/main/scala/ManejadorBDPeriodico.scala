package main.scala

import org.anormcypher._ 
import java.net.URL

class ManejadorBDPeriodico {
  
  var usuario = ""
  var newPath = ""
  var cont = ""
  var oldPath = ""
  implicit var connection = Neo4jREST()
  
  def this(path : String, user : String, password : String) = {
    this()
    usuario = user
    newPath = path
    cont = password
  }
  
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
      WHERE NOT ()-[]->(m)
      DELETE m
      """).on("rep" -> repeticiones).execute()
  }
  
  def borraMenosRepeticionesAcumuladas(repeticiones : Float) : Boolean = {
    Cypher("""
      MATCH (m)-[r]->(n:Modificador)
      WHERE m:Entidad OR m:Elemento
      WITH n.name as name, sum(r.repeticiones) as menciones
      WHERE menciones < {rep}
      MATCH (m)-[r]->(n:Modificador)
      WHERE n.name = name
      DELETE r
    """).on("rep" -> repeticiones).execute() &&
    Cypher("""
      MATCH (m:Modificador)
      WHERE NOT ()-[]->(m)
      DELETE m
      """).execute()
  }
  
  def borraMenosRepeticionesAcumuladas(identificador : String, repeticiones : Float) : Boolean = {
    Cypher("""
      MATCH (m {id:{identificador}})-[r]->(n:Modificador)
      WHERE m:Entidad OR m:Elemento
      WITH n.name as name, sum(r.repeticiones) as menciones
      WHERE menciones < {rep}
      MATCH (m {id:{identificador}})-[r]->(n:Modificador)
      WHERE n.name = name
      DELETE r
    """).on("identificador" -> identificador ,"rep" -> repeticiones).execute() &&
    Cypher("""
      MATCH (m:Modificador)
      WHERE NOT ()-[]->(m)
      DELETE m
      """).execute()
  }
  
  def borraMenosRepeticionesProporcion(proporcion : Float) : Boolean = {
    Cypher("""
      MATCH (n)-[r]->(m:Modificador)
      WITH n.id as id, count(r.repeticiones) as menciones
      MATCH (n)-[r]->(m:Modificador)
      WHERE n.id = id
      WITH id as id, menciones as menciones,r ORDER BY r.repeticiones DESCENDING
      WITH collect(r)[0..toInteger(menciones*""" +proporcion+ """)] as validas, id as id
      MATCH (n)-[r]->(m:Modificador)
      WHERE n.id = id AND NOT (r IN validas)
      DELETE r
    """).execute() &&
    Cypher("""
      MATCH (m:Modificador)
      WHERE NOT ()-[]->(m)
      DELETE m
      """).execute()
  }
  
  def borraMenosRepeticionesMedia() : Boolean = {
    var ok = true
    Cypher("""
      MATCH (m)
      WHERE m:Entidad OR m:Elemento
      WITH m.id as idEnt
      MATCH (m)-[r]->(n:Modificador)
      WHERE m.id = idEnt
      WITH m.id as idDef, sum(r.repeticiones) as menciones, count(r) as num
      RETURN idDef as idDef,(menciones*1.0)/num as media
      """).apply().foreach { row => ok = ok && borraMenosRepeticionesAcumuladas(row[String]("idDef"),row[BigDecimal]("media").floatValue() ) }
    ok = ok && Cypher("""
      MATCH (m:Modificador)
      WHERE NOT ()-[]->(m)
      DELETE m
      """).execute()
    return ok
  }
  
  
  def compactaPorDiaConFechaLimite(fechaLimite : Long) : Boolean = {
    Cypher("""
      MATCH (n)-[r]->(m:Modificador)
    	WHERE r.date < {fecha}
      WITH n.id as ident, m.name as nombreMod, sum(r.repeticiones) as menciones, (r.date/24)*24 as nuevaFecha, collect(r) as oldRels
      MATCH (n{id: ident}),(m{name: nombreMod})
      CREATE (n)-[r:modificador {repeticiones: menciones, date: nuevaFecha}]->(m)
      WITH oldRels as oldRels
      MATCH (n)-[r]->(m:Modificador)
      WHERE r IN oldRels
      DELETE r
      """).on("fecha" -> fechaLimite).execute() &&
    Cypher("""
      MATCH (n)
      WHERE n:Entidad or n:Elemento
      WITH n as n, [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 0] | n.menciones[y]] as paresMenciones, [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] as imparesMenciones
      WITH n as n, EXTRACT(i IN RANGE(0, SIZE(paresMenciones) - 1) | [ paresMenciones[i], imparesMenciones[i]]) AS listaPares
      WITH n as n, listaPares as listaPares, [i IN listaPares WHERE head(i) < {fecha} | [(head(i)/24)*24]+tail(i)] as nuevasFechas
      UNWIND nuevasFechas as res
      WITH n as n, listaPares, head(res) as cabeza, sum(head(tail(res))) as cola
      WITH n, listaPares, collect([cabeza]+[cola]) as menores
      WITH n, menores + [i IN listaPares WHERE head(i) >= {fecha}] as nuevaPares
      SET n.menciones = REDUCE(output = [], r IN nuevaPares | output + r)
       """).on("fecha" -> fechaLimite).execute()
  }
  
}