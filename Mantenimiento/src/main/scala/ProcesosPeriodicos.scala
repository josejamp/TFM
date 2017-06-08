package main.scala



object ProcesosPeriodicos {
  
  def limpiezaPeriodicaRelaciones(delayInicial : Long, intervalo : Long, maximo : Int, repeticiones : Int, path : String, user : String, password : String) = {
    var mBD = new ManejadorBDPeriodico()
    if(mBD.setServer(path, user, password)){
      var rn = new RepeatRunnable(mBD.borraMenosRepeticiones(repeticiones) , delayInicial, intervalo, maximo)
      rn.execute()
    }
    else print("Error de conexion")
  }
  
  def limpiezaPeriodicaRelacionesMedia(delayInicial : Long, intervalo : Long, maximo : Int, path : String, user : String, password : String) = {
    var mBD = new ManejadorBDPeriodico()
    if(mBD.setServer(path, user, password)){
      var rn = new RepeatRunnable(mBD.borraMenosRepeticionesMedia() , delayInicial, intervalo, maximo)
      rn.execute()
    }
    else print("Error de conexion")
  }
  
  def limpiezaPeriodicaRelacionesProporcion(delayInicial : Long, intervalo : Long, maximo : Int, proporcion : Float, path : String, user : String, password : String) = {
    var mBD = new ManejadorBDPeriodico()
    if(mBD.setServer(path, user, password)){
       var rn = new RepeatRunnable(mBD.borraMenosRepeticionesProporcion(proporcion) , delayInicial, intervalo, maximo)
       rn.execute()
    }
    else print("Error de conexion")
  }
  
  def compactacionPeriodicaRelacionesProporcion(delayInicial : Long, intervalo : Long, maximo : Int, fechaLimite : Long, path : String, user : String, password : String) = {
    var mBD = new ManejadorBDPeriodico()
    if(mBD.setServer(path, user, password)){
      var rn = new RepeatRunnable(mBD.compactaPorDiaConFechaLimite(fechaLimite) , delayInicial, intervalo, maximo)
      rn.execute()
    }
    else print("Error de conexion")
  }
  
}