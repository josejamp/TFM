package main.scala.dataBase

import main.scala.Jerarquia
import main.scala.Puntuacion
import main.scala.DateUtils

import scala.collection.mutable.HashMap

import org.anormcypher.CypherResultRow
import org.anormcypher.MetaDataItem
import org.anormcypher.MetaData

object TraduccionDatos {
  
  
  def generaJerarquiaSinModificadores() : Jerarquia = {
    var jer = new Jerarquia()
    Consultas.getFirstLevel().foreach { row => 
      val (nombre,punt_propia,punt_hijos,menciones,identificador) = traduceNivelEntidadElemento(row)
      jer.add(nombre, generaJerarquiaSinModificadoresAux(identificador), new Puntuacion(punt_propia,punt_hijos,sumaImpares(menciones.toList).toInt),"","",identificador)
    }
    return jer
  }
  
  def generaJerarquiaSinModificadoresAux(id_padre : String) : Jerarquia = {
    var jer = new Jerarquia()
    Consultas.getChildrenEntidad(id_padre).foreach { row => 
      val (nombre,punt_propia,punt_hijos,menciones,identificador) = traduceNivelEntidadElemento(row)
      print("Nombre: " + nombre + "\n")
      if(identificador != id_padre){
        jer.add(nombre, generaJerarquiaSinModificadoresAux(identificador), new Puntuacion(punt_propia,punt_hijos,sumaImpares(menciones.toList).toInt),"","",identificador)
      }
    }
    Consultas.getChildrenElemento(id_padre).foreach { row => 
      val (nombre,punt_propia,punt_hijos,menciones,identificador) = traduceNivelEntidadElemento(row)
      jer.add(nombre, new Jerarquia(), new Puntuacion(punt_propia,punt_hijos,sumaImpares(menciones.toList).toInt),"","",identificador)
    }
    return jer
  }
  
  def getArbolParaHTML() : (Stream[CypherResultRow],Stream[CypherResultRow]) = {
    var accNodes = Stream[CypherResultRow]()
    var accRel = Stream[CypherResultRow]()
    val hijos = Consultas.getSubTreeWithoutModificadoresByRoot()
    accNodes = Stream.concat(accNodes, hijos)
    hijos.foreach{ row => print("Row: " + row + "\n"); accRel = Stream.concat(accRel, Consultas.getFatherRelationship(getIdentificador(row)._1)) }
    hijos.foreach{ row =>
      print("Row: " + row + "\n")
      val (identificador,res) = getIdentificador(row)
      if(res){
        val modificadores = Consultas.getBestModificadores(identificador)
        val relaciones = Consultas.getBestModificadoresRelationship(identificador)
        accNodes = Stream.concat(accNodes, modificadores)
        accRel = Stream.concat(accRel, relaciones)
      }
    }
    return (accNodes, accRel)
  }
  
  def getArbolParaHTML(id_padre :String) : (Stream[CypherResultRow],Stream[CypherResultRow]) = {
    var accNodes = Stream[CypherResultRow]()
    var accRel = Stream[CypherResultRow]()
    accNodes = Stream.concat(accNodes, Consultas.getNodeByID(id_padre))
    accNodes = Stream.concat(accNodes, Consultas.getBestModificadores(id_padre))
    accRel = Stream.concat(accRel,Consultas.getBestModificadoresRelationship(id_padre))
    val hijos = Consultas.getSubTreeWithoutModificadores(id_padre)
    accNodes = Stream.concat(accNodes, hijos)
    hijos.foreach{ row => print("Row: " + row + "\n"); accRel = Stream.concat(accRel, Consultas.getFatherRelationship(getIdentificador(row)._1)) }
    hijos.foreach{ row =>
      print("Row: " + row + "\n")
      val (identificador,res) = getIdentificador(row)
      if(res){
        val modificadores = Consultas.getBestModificadores(identificador)
        val relaciones = Consultas.getBestModificadoresRelationship(identificador)
        accNodes = Stream.concat(accNodes, modificadores)
        accRel = Stream.concat(accRel, relaciones)
      }
    }
    return (accNodes, accRel)
  }
  
  def subArbolPorID(id_padre : String) : (Stream[CypherResultRow],Stream[CypherResultRow]) = {
    var accNodes = Stream[CypherResultRow]()
    var accRel = Stream[CypherResultRow]()
    accNodes = Stream.concat(accNodes, Consultas.getNodeByID(id_padre))
    accRel = Stream.concat(accRel, Consultas.getRelationshipsFrom(id_padre))
    val nodosHijos = Consultas.getSubTree(id_padre)
    nodosHijos.foreach { row => 
      val (identificador,res) = getIdentificador(row)
      if(res){
        val sRel = Consultas.getRelationshipsFrom(identificador)
        accRel = Stream.concat(accRel, sRel)
      }
    }
    return (Stream.concat(accNodes,nodosHijos), accRel)
  }
  
  def subArbolPorIDAux(id_padre : String) : (Stream[CypherResultRow],Stream[CypherResultRow]) = {
    var accNodes = Stream[CypherResultRow]()
    var accRel = Stream[CypherResultRow]()
    val nodoActualEnts = Consultas.getChildrenEntidadWithLabels(id_padre)
    nodoActualEnts.foreach { row => 
      val (identificador,res) = getIdentificador(row)
      if(identificador != id_padre){
        var (sNodes, sRel) = subArbolPorIDAux(identificador)
        accNodes = Stream.concat(accNodes, sNodes)
        accRel = Stream.concat(accRel, sRel)
      }
    }
    val nodoActualElems = Consultas.getChildrenElementoWithLabels(id_padre)
    nodoActualElems.foreach { row => 
      val (identificador,res) = getIdentificador(row)
      var (sNodes, sRel) = subArbolPorIDAux(identificador)
      accNodes = Stream.concat(accNodes, sNodes)
      accRel = Stream.concat(accRel, sRel)
    }
    val nodoActualMods = Consultas.getChildrenModificadorWithLabels(id_padre)
    accNodes = Stream.concat(accNodes, nodoActualEnts)
    accNodes = Stream.concat(accNodes, nodoActualElems)
    accNodes = Stream.concat(accNodes, nodoActualMods)
    accRel = Stream.concat(accRel, Consultas.getRelationshipsFrom(id_padre))
    return (accNodes, accRel)
  }
  
  def llenaRelacionesYAtributos() : RelacionesYAtributos = {
    var relAtt = new RelacionesYAtributos()
     Consultas.getReceiveingRelationships().foreach { row => 
       val tipo = row[String]("type")
       relAtt.addRelation(tipo)
       if(tipo != "modificador"){
         relAtt.addAttribute(tipo, "menciones")
         relAtt.addAttribute(tipo, "puntuacion hijos")
         relAtt.addAttribute(tipo, "puntuacion propia")
         relAtt.addAttribute(tipo, "nombre")
       }
       else{
         relAtt.addAttribute(tipo, "repeticiones")
         relAtt.addAttribute(tipo, "puntuacion")
         relAtt.addAttribute(tipo, "nombre")
       }
    }
    print("Rel&Att: " + relAtt.getTable + "\n")
    return relAtt
  }
  
  def mencionesGrafica(id : String) : List[(String,Float)] = {
    var ret = List[(String,Float)]()
    Consultas.getNumberReferencesFrom(id).foreach { row => 
      val dates = row[Seq[Long]]("menciones").toList.zipWithIndex.filter(_._2 % 2 == 0).map(_._1).map( x => DateUtils.toSecondsFromHours(x))
      val refs = row[Seq[Long]]("menciones").toList.zipWithIndex.filter(_._2 % 2 > 0).map(_._1).map(_.toFloat)
      ret = dates.map(x => DateUtils.formatDate(x)).zip(refs)
    }
    return ret
  }
  
  def puntuacionPropiaGrafica(id : String) : List[(String,Float)] = {

    var ret = List[(String,Float)]()
    for((k,v)<-getPuntuacionPropiaFechas(id)){
      ret = (DateUtils.formatDate(DateUtils.toSecondsFromHours(k)),(v.sum/v.length).toFloat) :: ret
    }
    return ret
  }
  
  def puntuacionHijosGrafica(id : String) : List[(String,Float)] = {

    var ret = List[(String,Float)]()
    for((k,v)<-getPuntuacionHijosFechas(id)){
      ret = (DateUtils.formatDate(DateUtils.toSecondsFromHours(k)),(v.sum/v.length).toFloat) :: ret
    }
    return ret
  }
  
  def resumenGrafica(id : String) : List[(String,Float)] = {

    var ret = List[(String,Float)]()
    for((k,v)<-getResumenFechas(id)){
      ret = (DateUtils.formatDate(DateUtils.toSecondsFromHours(k)),(v.sum/v.length).toFloat) :: ret
    }
    return ret
  }
  
  
  def getPuntuacionPropiaFechas(id : String) : HashMap[Long,List[Float]] = {
    var hashMap = new HashMap[Long,List[Float]]()
    List.concat(Consultas.getDirectModifiersWithDatesFrom(id),Consultas.getIndirectModifiersWithDatesFrom(id)).foreach { row => 
      val date = row[Long]("fecha")
      val rep = row[Int]("repeticiones")
      val punt = row[BigDecimal]("puntos").floatValue()
      if(hashMap.contains(date)){
        var lista = cleanOptionOfListOfLong(hashMap.get(date))
        for( i <- 1 to rep){
          lista = punt :: lista
        }
        hashMap.put(date,lista)
      }
      else{
        var lista = List[Float]()
        for( i <- 1 to rep){
          lista = punt :: lista
        }
        hashMap.put(date,lista)
      }
    }
    return hashMap
  }
  
  def getPuntuacionHijosFechas(id : String) : HashMap[Long,List[Float]] = {
    var hashMap = new HashMap[Long,List[Float]]()
    Consultas.getChildrenEntidad(id).foreach{ row =>
      if(row[String]("identificador") != id){
        val puntHijos = getResumenFechas(row[String]("identificador"))
        for((k,v)<-puntHijos){
          if(hashMap.contains(k)){
            var lista = cleanOptionOfListOfLong(hashMap.get(k))
            lista = (v.sum/v.length) :: lista
            hashMap.put(k,lista)
          }
          else{
            var lista = List[Float]()
            lista = (v.sum/v.length) :: lista
            hashMap.put(k,lista)
          }
        }
      }
    }
    return hashMap
  }
  
  def getResumenFechas(id : String) :  HashMap[Long,List[Float]] = {
    var hashMap = getPuntuacionHijosFechas(id)
    val hashPropio = getPuntuacionPropiaFechas(id)
    for((k,v)<-hashPropio){
      if(hashMap.contains(k)){
        val listaHijos = cleanOptionOfListOfLong(hashMap.get(k))
        val mediaHijos = (listaHijos.sum/listaHijos.length)
        val mediaPropia = (v.sum/v.length)
        var nuevaLista = List[Float]()
        nuevaLista = mediaHijos :: (mediaPropia :: nuevaLista)
        hashMap.put(k,nuevaLista)
      }
      else{
        val mediaPropia = (v.sum/v.length)
        var nuevaLista = List[Float]()
        nuevaLista = (mediaPropia :: nuevaLista)
        hashMap.put(k,nuevaLista)
      }
    }
    return hashMap
  }
  
  def cleanOptionOfListOfLong(lista : Option[List[Float]]) : List[Float] = lista match{
    case Some(x:List[Float]) => x
    case _ => List[Float]()
  }
  
  def optionOfTupleToInt(x : Option[(String,String,Int)]) : (String,String,Int) = x match{
    case Some(num:(String,String,Int)) => num
    case _ => ("","",0)
  }
  
  def hashOfDatesToList(hashMap : HashMap[Long,List[Float]]) : List[(Long,Float)] = {
    var l = List[(Long,Float)]()
    for((k,v)<-hashMap){
      l = List.concat(l,List((k,(v.sum/v.length))))
    }
    l.sortBy(_._1)
  }
  
  def generaConsultaGenerica(relName : String) : String = {
    if(relName=="modificador"){
        """
          MATCH (n)-[r:""" + relName + """]->(m:Modificador)
          RETURN m.name as name, m.points as puntos, r.date as dates, r.repeticiones as repeticiones
        """
      }
      else if(relName=="elemento"){
        """
          MATCH (n)-[r:""" + relName + """]->(m:Elemento)
          RETURN n.name as padre, m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
      else{
        """
          MATCH (n)-[r:""" + relName + """]->(m:Entidad)
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
  }
  
  def generaConsultaGenericaFecha(relName : String, date1 : Long , date2 : Long) : String = {
    if(relName=="modificador"){
        """
          MATCH (n)-[r:""" + relName + """]->(m:Modificador)
          WHERE r.date >= """ + date1 + """ AND r.date <= """ + date2 + """
          RETURN n.name as padre, m.name as name, m.points as puntos, r.date as dates, r.repeticiones as repeticiones
        """
      }
      else if(relName=="elemento"){
        """
          MATCH (n)-[r:""" + relName + """]->(m)
          RETURN n.name as padre, m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
      else{
        """
          MATCH (n)-[r:""" + relName + """]->(m)
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
  }
  
  def traduceConsultaFechaMencionesModificador(relName : String, attName : String, op : String, value : String, date1 : Long, date2 : Long) : List[(String,String,String)] = {
    var hashMap = new HashMap[String,(String,String,Int)]()
    val query = Consultas.executeQuery(generaConsultaGenericaFecha(relName,date1,date2))
    query.foreach{ row =>
      val rep = row[Int]("repeticiones")
      val nombre = row[String]("name")
      val padre = row[String]("padre")
      val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
      if(hashMap.contains(fullName)){
        val (n,p,r) = optionOfTupleToInt(hashMap.get(fullName))
        hashMap.put(fullName, (n,p,r+rep))
      }
      else hashMap.put(fullName,(nombre,padre,rep))
    }
    var l = List[(String,String,String)]()
    query.foreach{ row =>
      val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
      if(hashMap.contains(fullName)){
        val rep = optionOfTupleToInt(hashMap.get(fullName))._3
        if(compruebaOperacion(rep, op, value.toFloat))
          l = (fullName,row[BigDecimal]("puntos").toString(),rep.toString()) :: l
      }
    }
    return l
  }
  
  def traduceConsultaFechaMenciones(relName : String, attName : String, op : String, value : String, date1 : Long, date2 : Long) : List[(String,String,String,String,String)] = {
    var l = List[(String,String,String,String, String)]()
    val datos = Consultas.executeQuery(generaConsultaGenerica(relName))
    datos.foreach { row => 
      val menciones = row[Seq[Long]]("menciones").toList
      val valida = parteListaPorFechas(menciones,date1,date2)
      val numTotales = sumaImpares(valida)
      if(valida.size > 0 && compruebaOperacion(numTotales, op, value.toFloat)){
        if(relName == "elemento"){
          val (n,pp,ph,m,i) = traduceResultadoConsultaElementoSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
        else{
          val (n,pp,ph,m,i) = traduceResultadoConsultaEntidadSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
      }
    }
    return l
  }
  
  def traduceConsultaFechaPuntHijos(relName : String, attName : String, op : String, value : String, date1 : Long, date2 : Long) : List[(String,String,String,String,String)] = {
    var l = List[(String,String,String,String, String)]()
    val datos = Consultas.executeQuery(generaConsultaGenerica(relName))
    datos.foreach { row =>
      print("Nombre: " + row[String]("name") + "\n")
      val listaFechas = hashOfDatesToList(getPuntuacionHijosFechas(row[String]("identificador")))  
      val listaValida = parteListaPorFechasTuples(listaFechas,date1,date2)
      print("Lista valida: " + listaValida + "\n")
      if(listaValida.filter{x => compruebaOperacion(x._2,op,value.toFloat)}.length > 0){
        if(relName == "elemento"){
          val (n,pp,ph,m,i) = traduceResultadoConsultaElementoSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
        else{
          val (n,pp,ph,m,i) = traduceResultadoConsultaEntidadSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
      }
    }
    return l
  }
  
  def traduceConsultaFechaPuntPropia(relName : String, attName : String, op : String, value : String, date1 : Long, date2 : Long) : List[(String,String,String,String,String)] = {
    var l = List[(String,String,String,String, String)]()
    val datos = Consultas.executeQuery(generaConsultaGenerica(relName))
    datos.foreach { row =>
      val listaFechas = hashOfDatesToList(getPuntuacionPropiaFechas(row[String]("identificador")))  
      val listaValida = parteListaPorFechasTuples(listaFechas,date1,date2)
      if(listaValida.filter{x => compruebaOperacion(x._2,op,value.toFloat)}.length > 0){
        if(relName == "elemento"){
          val (n,pp,ph,m,i) = traduceResultadoConsultaElementoSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
        else{
          val (n,pp,ph,m,i) = traduceResultadoConsultaEntidadSinTratar(row)
          l = (n,pp,ph,sumaImpares(m.toList).toString(),i) :: l
        }
      }
    }
    return l
  }
  
  def parteListaPorFechas(menciones : List[Long], date1 : Long, date2 : Long) : List[Long] = {
    var l = List[Long]()
    var enc = false
    var pos = 0
    menciones.foreach { x => 
      if(pos % 2 == 0){
        if(x > date1) enc = true
        if(x > date2) enc = false
      }
      if(enc) l = List.concat(l,List(x))
      pos += 1
    }
    return l
  }
  
  def parteListaPorFechasTuples(lista : List[(Long,Float)], date1 : Long, date2 : Long) : List[(Long,Float)] = {
    lista.filter{ p => p._1 >= date1 && p._1 <= date2}
  }
  
  
  def generaConsultaAritmetica(relName : String, attName : String, op : String, value : String) : String = {
      val attTranslated = traduceNombreAtributo(attName,relName=="modificador")
      val fullAtt = if(attTranslated != "repeticiones") ("m." + attTranslated) else if(relName == "modificador")("r." + attTranslated) else ("m." + attTranslated)
      var valueModified = if(attTranslated=="name") """""""+value+""""""" else value
      var opModified = op
      if(opModified=="contiene"){
        opModified = "=~"
        valueModified = """'.*""" + value + """.*'"""
      }
      val fullOp = fullAtt + " " + opModified + " " + valueModified
      if(relName=="modificador"){
        """
          MATCH (n)-[r:""" + relName + """]->(m:Modificador)
          WHERE """ + fullOp + """
          RETURN n.name as padre, m.name as name, m.points as puntos, r.repeticiones as repeticiones
        """
      }
      else if(relName=="elemento"){
        """
          MATCH (n)-[r:""" + relName + """]->(m)
          WHERE """ + fullOp + """
          RETURN n.name as padre, m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
      else{
        """
          MATCH (n)-[r:""" + relName + """]->(m)
          WHERE """ + fullOp + """
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
  }
  
  def generaConsultaPertenenciaCaminoIndirecto(relName : String, id : String) : String = {
    val realID = """""""+id+"""""""
    if(relName=="modificador"){
        """
          MATCH (n {id:""" + realID + """})-[*]->(x)-[r:""" + relName + """]->(m:Modificador)
          RETURN n.name as padre, m.name as name, m.points as puntos, r.repeticiones as repeticiones
        """
      }
      else if(relName=="elemento"){
        """
          MATCH (n {id:""" + realID + """})-[*]->(x)-[r:""" + relName + """]->(m)
          RETURN n.name as padre, m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
      else{
        """
          MATCH (n {id:""" + realID + """})-[*]->(x)-[r:""" + relName + """]->(m)
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
  }
  
    def generaConsultaPertenenciaCaminoDirecto(relName : String, id : String) : String = {
    val realID = """""""+id+"""""""
    if(relName=="modificador"){
        """
          MATCH (n {id:""" + realID + """})-[r:""" + relName + """]->(m:Modificador)
          RETURN n.name as padre, m.name as name, m.points as puntos, r.repeticiones as repeticiones
        """
      }
      else if(relName=="elemento"){
        """
          MATCH (n {id:""" + realID + """})-[r:""" + relName + """]->(m)
          RETURN n.name as padre, m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
      else{
        """
          MATCH (n {id:""" + realID + """})-[r:""" + relName + """]->(m)
          RETURN m.name as name, m.points_own as punt_propia, m.points_children as punt_hijos, m.menciones as menciones, m.id as identificador
        """
      }
  }
  
  def traduceConsultaAritmetica(relName : String, attName : String, op : String, value : String) : List[(String,String,String,String,String)] = {
    var l = List[(String,String,String,String, String)]()
    if(attName == "menciones"){
      val queryText = generaConsultaGenerica(relName)
      Consultas.executeQuery(queryText).foreach { row => 
        if(relName != "elemento"){
          val (n,pp,ph,m,i) = traduceResultadoConsultaEntidadSinTratar(row)
          val suma = sumaImpares(m.toList)
          if(compruebaOperacion(suma.toInt,op,value.toInt))
            l = (n,pp,ph,suma.toString(),i) :: l  
        }
        else{
          val (n,pp,ph,m,i) = traduceResultadoConsultaElementoSinTratar(row)
          val suma = sumaImpares(m.toList)
          if(compruebaOperacion(suma.toInt,op,value.toInt))
            l = (n,pp,ph,suma.toString(),i) :: l  
        }
      }
    }
    else{
      val queryText = generaConsultaAritmetica(relName,attName,op,value)
      print(queryText + "\n")
      Consultas.executeQuery(queryText).foreach { row => 
        if(relName != "elemento") l = traduceResultadoConsultaEntidad(row) :: l  
        else l = traduceResultadoConsultaElemento(row) :: l  
      }
    }
    return l
  }
  
  def traduceConsultaAritmeticaModificador(relName : String, attName : String, op : String, value : String) : List[(String,String,String)] = {
    if(attName == "repeticiones"){
      var hashMap = new HashMap[String,(String,String,Int)]()
      val query = Consultas.executeQuery(generaConsultaGenericaFecha(relName,0, System.currentTimeMillis / 1000))
      query.foreach{ row =>
        val rep = row[Int]("repeticiones")
        val nombre = row[String]("name")
        val padre = row[String]("padre")
        val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
        if(hashMap.contains(fullName)){
          val (n,p,r) = optionOfTupleToInt(hashMap.get(fullName))
          hashMap.put(fullName, (n,p,r+rep))
        }
        else hashMap.put(fullName,(nombre,padre,rep))
      }
      var l = List[(String,String,String)]()
      query.foreach{ row =>
        val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
        if(hashMap.contains(fullName)){
          val rep = optionOfTupleToInt(hashMap.get(fullName))._3
          if(compruebaOperacion(rep, op, value.toFloat))
            l = (fullName,row[BigDecimal]("puntos").toString(),rep.toString()) :: l
        }
      }
      return l
    }
    else{
      var l = List[(String,String,String)]()
      var hashMap = new HashMap[String,(String,String,Int)]()
      val queryText = generaConsultaAritmetica(relName,attName,op,value)
      print(queryText + "\n")
      val query = Consultas.executeQuery(queryText)
      query.foreach{ row =>
        val rep = row[Int]("repeticiones")
        val nombre = row[String]("name")
        val padre = row[String]("padre")
        val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
        if(hashMap.contains(fullName)){
          val (n,p,r) = optionOfTupleToInt(hashMap.get(fullName))
          hashMap.put(fullName, (n,p,r+rep))
        }
        else hashMap.put(fullName,(nombre,padre,rep))
      }
      query.foreach { row => 
        val fullName = row[String]("name") + " ( " + row[String]("padre") + " )"
        if(hashMap.contains(fullName)){
         l = (fullName, traduceResultadoConsultaModificador(row)._2, optionOfTupleToInt(hashMap.get(fullName))._3.toString()) :: l  
        }
      }
      return l
    }
  }
  
  def traduceConsultaPertenencia(relName : String, id : String) : List[(String,String,String,String,String)] = {
    var l = List[(String,String,String,String, String)]()
    val queryTextInd = generaConsultaPertenenciaCaminoIndirecto(relName,id)
    val queryTextDir = generaConsultaPertenenciaCaminoDirecto(relName,id)
    print(queryTextInd + "\n")
    print(queryTextDir + "\n")
    val elements =  List.concat(Consultas.executeQuery(queryTextInd),Consultas.executeQuery(queryTextDir))
    elements.foreach { row => 
      if(relName != "elemento") l = traduceResultadoConsultaEntidad(row) :: l  
      else l = traduceResultadoConsultaElemento(row) :: l  
    }
    return l
  }
  
  def traduceConsultaPertenenciaModificador(relName : String, id : String) : List[(String,String,String)] = {
    var l = List[(String,String,String)]()
    val queryTextInd = generaConsultaPertenenciaCaminoIndirecto(relName,id)
    val queryTextDir = generaConsultaPertenenciaCaminoDirecto(relName,id)
    print(queryTextInd + "\n")
    print(queryTextDir + "\n")
    val elements =  List.concat(Consultas.executeQuery(queryTextInd),Consultas.executeQuery(queryTextDir))
    elements.foreach { row => 
     l = traduceResultadoConsultaModificador(row) :: l  
    }
    return l
  }
  
  def traduceNivelEntidadElemento(row : CypherResultRow) : (String,Float,Float,Seq[Long],String) = {
    (row[String]("nombre"), row[BigDecimal]("punt_propia").floatValue(), row[BigDecimal]("punt_hijos").floatValue(), row[Seq[Long]]("menciones"), row[String]("identificador"))
  }
  
  def traduceResultadoConsultaEntidadSinTratar(row : CypherResultRow) : (String, String, String, Seq[Long], String) = {
    (row[String]("name"), row[BigDecimal]("punt_propia").toString(), row[BigDecimal]("punt_hijos").toString(), row[Seq[Long]]("menciones") , row[String]("identificador"))
  }
  
  def traduceResultadoConsultaElementoSinTratar(row : CypherResultRow) : (String, String, String, Seq[Long], String) = {
    (row[String]("name") + " ( " + row[String]("padre") + " )", row[BigDecimal]("punt_propia").toString(), row[BigDecimal]("punt_hijos").toString(), row[Seq[Long]]("menciones"), row[String]("identificador"))
  }
  
  def traduceResultadoConsultaEntidad(row : CypherResultRow) : (String, String, String, String, String) = {
    (row[String]("name"), row[BigDecimal]("punt_propia").toString(), row[BigDecimal]("punt_hijos").toString(), sumaImpares(row[Seq[Long]]("menciones").toList).toString() , row[String]("identificador"))
  }
  
  def traduceResultadoConsultaElemento(row : CypherResultRow) : (String, String, String, String, String) = {
    (row[String]("name") + " ( " + row[String]("padre") + " )", row[BigDecimal]("punt_propia").toString(), row[BigDecimal]("punt_hijos").toString(), sumaImpares(row[Seq[Long]]("menciones").toList).toString(), row[String]("identificador"))
  }
  
  def traduceResultadoConsultaModificador(row : CypherResultRow) : (String, String, String) = {
    (row[String]("name") + " ( " + row[String]("padre") + " )", row[BigDecimal]("puntos").toString(), row[Int]("repeticiones").toString())
  }
  
  def getIdentificador(row : CypherResultRow) : (String, Boolean) = {
    if(row[Option[String]]("identificador")==None) return ("",false)
    else (row[String]("identificador"),true)
  }
  
  def traduceNombreAtributo(nombre : String, modificador : Boolean) : String = nombre match{
    case "menciones" => if(modificador) "repeticiones" else "menciones"
    case "repeticiones" => if(modificador) "repeticiones" else "menciones"
    case "puntuacion hijos" => "points_children"
    case "puntuacion propia" => "points_own"
    case "puntuacion" => "points"
    case "nombre" => "name"
    case _ => ""
  }
  
  def traduceNombreAtributoFechas(nombre : String, modificador : Boolean) : String = nombre match{
    case "menciones" => if(modificador) "repeticiones" else "menciones"
    case "repeticiones" => if(modificador) "repeticiones" else "menciones"
    case "puntuacion hijos" => "punt_hijos"
    case "puntuacion propia" => "punt_propia"
    case "puntuacion" => "puntos"
    case "nombre" => "name"
    case _ => ""
  }
  
  def compruebaOperacion(suma : Long, op : String, value : Long) : Boolean = op match{
    case "=" => suma==value
    case ">" => suma > value
    case ">=" => suma >= value
    case "<" => suma < value
    case "<=" => suma <= value
  }
  
    def compruebaOperacion(suma : Float, op : String, value : Float) : Boolean = op match{
    case "=" => suma==value
    case ">" => suma > value
    case ">=" => suma >= value
    case "<" => suma < value
    case "<=" => suma <= value
  }
  
  
  def sumaImpares(lista : List[Long]) : Long = lista.zipWithIndex.filter(_._2 % 2 > 0).map(_._1).sum
  
  
  def impares[A](lista : List[A]) : List[A] = lista.zipWithIndex.filter(_._2 % 2 > 0).map(_._1)
  
  def pares[A](lista : List[A]) : List[A] = lista.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
  
}