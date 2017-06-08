package main.scala.dataBase

import org.anormcypher._
import java.net.URL

object Consultas {
  
   var oldPath = ""
   implicit var connection = Neo4jREST()
   
   @throws(classOf[java.security.PrivilegedActionException])
   def setServer(path : String) : Boolean = {
     val neo4jURLString = util.Properties.envOrElse("DATA_BASE",path)
     val url : URL = new URL(neo4jURLString)
     val newConnection = Neo4jREST(url.getHost, url.getPort)
     try{
       newConnection.sendQuery(Cypher("""MATCH (n:Root) RETURN n.name"""))
     }
     catch{
       case e : Any => return false
     }
     connection = newConnection
     oldPath = path
     return true
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
   
   def cleanLoops() = {
     val query1 = Cypher("""
          MATCH (n:Entidad)-[r]->(m:Entidad)
          WHERE n.id = m.id
          DELETE r
          """)
     val query2 = Cypher("""
          MATCH (n:Entidad)-[r:modificador]->(m:Entidad)
          DELETE r
          """)
     val query3 = Cypher("""
          MATCH (n:Elemento)-[r:modificador]->(m:Elemento)
          DELETE r
          """)
     query1.apply()
     query2.apply()
     query3.apply()
   }
   
   def getRaiz() : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n)
          WHERE n:Root
          RETURN ID(n) as id, n.name as name, labels(n) as label
          """)
     query.apply()
   }
   
   def getAllEntidadesElementos() : Stream[CypherResultRow] = {
     print(connection.host + "\n")
     val query = Cypher("""
          MATCH (n)
          WHERE n:Entidad OR n:Elemento
          RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, reduce(sum=0,nn IN [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] |  sum + nn) as menciones
          """)
     query.apply()
   }
   
   def getAllModificadores() : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n)
          WHERE n:Modificador
          RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.menciones as menciones
          """)
     query.apply()
   }
   
   def getAllNodes() : Stream[CypherResultRow] = Stream.concat(getRaiz(),getAllEntidadesElementos(),getAllModificadores())
   
   /*
   def getAllNodes() : Stream[CypherResultRow] = {
     print(connection.host + "\n")
     val query = Cypher("""
          MATCH (n)
          RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, reduce(sum=0,nn IN [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] |  sum + nn) as menciones
          """)
     query.apply()
   }
   * */
   
   def getAllRelationships() : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n)-[r]-(m)
          RETURN ID(n) as source, ID(m) as target, r.repeticiones as repeticiones, type(r) as type
          """)
     query.apply()
   }
   
   def getBestModificadores(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (m {id:{identificador}})-[r]->(n:Modificador)
          WHERE m:Entidad OR m:Elemento
          WITH n.name as name, sum(r.repeticiones) as menciones, head(collect(ID(n))) as id, head(collect(labels(n))) as label, head(collect(n.points)) as points
          RETURN id,label,points, name, menciones
          ORDER BY CASE WHEN points < 2.0 THEN (points + 2*(2.0-points))*menciones
                        ELSE menciones*points END
          DESCENDING
          LIMIT 10
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getBestModificadoresRelationship(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (m {id:{identificador}})-[r]-(n:Modificador)
          WHERE m:Entidad OR m:Elemento
          WITH n.name as name, sum(r.repeticiones) as menciones, head(collect(ID(n))) as source, head(collect(labels(n))) as label, head(collect(n.points))  as points,  head(collect(type(r))) as type
          MATCH (m {id:{identificador}})
          RETURN name, source, ID(m) as target, menciones, type
          ORDER BY CASE WHEN points < 2.0 THEN (points + 2*(2.0-points))*menciones
                        ELSE menciones*points END
          DESCENDING
          LIMIT 10
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getReceiveingRelationships() : Stream[CypherResultRow] = {
          val query = Cypher("""
          MATCH (n)-[r]-(m)
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.points as puntos, r.repeticiones as repeticiones, type(r) as type
          """)
     query.apply()
   }
   
   def getFirstLevel() : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (b:Entidad)
          WHERE (:Root)-[]->(b:Entidad) 
          RETURN b.name as nombre, b.points_own as punt_propia, b.points_children as punt_hijos, b.menciones as menciones, b.id as identificador
          """)
     query.apply()
   }
   
   def getRelationshipsFrom(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n {id:{identificador}})-[r]->(m)
          RETURN ID(n) as source, ID(m) as target, r.repeticiones as repeticiones, type(r) as type
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getFatherRelationship(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n:Entidad)-[r]->(m {id:{identificador}})
          RETURN ID(n) as source, ID(m) as target, r.repeticiones as repeticiones, type(r) as type
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getDirectModifiersWithDatesFrom(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n {id:{identificador}})-[r]->(m:Modificador)
          RETURN r.repeticiones as repeticiones, m.points as puntos, r.date as fecha
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getIndirectModifiersWithDatesFrom(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n {id:{identificador}})-[]->(:Elemento)-[r]->(m:Modificador)
          RETURN r.repeticiones as repeticiones, m.points as puntos, r.date as fecha
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getNumberReferencesFrom(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
          MATCH (n {id:{identificador}})
          RETURN n.menciones as menciones
          """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getNodeByID(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (n {id:{identificador}})
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador, reduce(sum=0,nn IN [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] |  sum + nn) as menciones
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getSubTree(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (m {id:{identificador}})-[*]->(n)
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getSubTreeWithoutModificadores(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (m {id:{identificador}})-[*]->(n)
       WHERE n:Entidad OR n:Elemento
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador, reduce(sum=0,nn IN [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] |  sum + nn) as menciones
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getSubTreeWithoutModificadoresByRoot() : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (m:Root)-[*]->(n)
       WHERE n:Entidad OR n:Elemento
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador, reduce(sum=0,nn IN [y IN [x IN range(0,size(n.menciones)-1) WHERE x % 2 = 1] | n.menciones[y]] |  sum + nn) as menciones
       """)
     query.apply()
   }
   
   def getChildrenEntidad(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (b:Entidad) WHERE (:Entidad {id:{identificador}})-[]->(b)
       RETURN b.name as nombre, b.points_own as punt_propia, b.points_children as punt_hijos, b.menciones as menciones, b.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getChildrenElemento(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (b:Elemento) WHERE (:Entidad {id:{identificador}})-[]->(b)
       RETURN b.name as nombre, b.points_own as punt_propia, b.points_children as punt_hijos, b.menciones as menciones, b.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getChildrenEntidadWithLabels(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (n:Entidad) WHERE (:Entidad {id:{identificador}})-[]->(n)
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getChildrenElementoWithLabels(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (n:Elemento) WHERE (:Entidad {id:{identificador}})-[]->(n)
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.points_own as punt_propia, n.points_children as punt_hijos, n.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def getChildrenModificadorWithLabels(identificador : String) : Stream[CypherResultRow] = {
     val query = Cypher("""
       MATCH (n:Modificador) WHERE (:Entidad {id:{identificador}})-[]->(n)
       RETURN ID(n) as id, n.name as name, labels(n) as label, n.points as points, n.id as identificador
       """).on("identificador" -> identificador)
     query.apply()
   }
   
   def executeQuery(queryText : String) : Stream[CypherResultRow] = {
     Cypher(queryText).apply()
   }
  
}