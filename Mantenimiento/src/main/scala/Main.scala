package main.scala

object Main extends App{
  
  override def main(args: Array[String]): Unit = {
    args.foreach { x => print(x); print(" ") }
    if(args.length >= 7){
      args(3) match {
        case "limpieza" => {
          args(4) match {
            /*direccion usuario password (limpieza | compactacion) (total | media | proporcion | compactacion) delayInicial intervalo maximo repeticiones */
            case "total" => {
              if(args.length == 9){
                ProcesosPeriodicos.limpiezaPeriodicaRelaciones(args(5).toLong, args(6).toLong, args(7).toInt, args(8).toInt, args(0), args(1), args(2))
              }
              else print("Error. Argumentos: <direccion> <usuario> <password> limpieza (total | media | proporcion) <delayInicial> <intervalo> <repeticiones> <numero_menciones>")
            }
            case "media" => {
              if(args.length == 8){
                ProcesosPeriodicos.limpiezaPeriodicaRelacionesMedia(args(5).toLong, args(6).toLong, args(7).toInt, args(0), args(1), args(2))
              }
              else print("Error. Argumentos: <direccion> <usuario> <password> limpieza (total | media | proporcion) <delayInicial> <intervalo> <repeticiones>")
            }
            case "proporcion" => {
              if(args.length == 9){
                ProcesosPeriodicos.limpiezaPeriodicaRelacionesProporcion(args(5).toLong, args(6).toLong, args(7).toInt, args(8).toInt, args(0), args(1), args(2))
              }
              else print("Error. Argumentos: <direccion> <usuario> <password> limpieza (total | media | proporcion) <delayInicial> <intervalo> <repeticiones> <proporcion>")
            }
            case _ => print("Error. Argumentos: <direccion> <usuario> <password> limpieza (total | media | proporcion) ...")
          }
        }
        case "compactacion" => {
          args(4) match {
            case "dia" => {
              if(args.length == 9){
                ProcesosPeriodicos.compactacionPeriodicaRelacionesProporcion(args(5).toLong, args(6).toLong, args(7).toInt, args(8).toInt, args(0), args(1), args(2))
              }
            }
            case _ => print("Error. Argumentos: <direccion> <usuario> <password> compactacion dia <delayInicial> <intervalo> <repeticiones> <fecha_horasEpoch>")
          }
        }
        case _ => print("Error. Argumentos: <direccion> <usuario> <password> (limpieza | compactacion) (total | media | proporcion | dia) ...")
      }
    }
    else print("Error. Argumentos: direccion usuario password (limpieza | compactacion) (total | media | proporcion | dia) ...")
  }
  
  
}