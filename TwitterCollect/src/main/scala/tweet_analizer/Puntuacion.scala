package tweet_analizer


class Puntuacion(propia : Float, hijos : Float) extends Serializable{
  
  def this(prop : Float, hij : Float, repeticiones : Int) = {
    this(prop, hij)
    this.setRepeticiones(repeticiones)
  }
  
  var punt_propia : Float = propia
  var punt_hijos : Float = hijos
  
  var repeticiones : Int = -1
  
  
  def getPuntuacionPropia : Float = punt_propia
  def getPuntuacionHijos : Float = punt_hijos
  
  def getRepeticiones : Int = repeticiones
  
  def setPuntuacionPropia(p_propia : Float) = {
    this.punt_propia = p_propia
  }
  
  def setPuntuacionHijos(p_hijos : Float) = {
    this.punt_hijos = p_hijos
  }
  
  def setRepeticiones(rep : Int) = {
    this.repeticiones = rep
  }
  
  def setPuntuacion(punt : Puntuacion) = {
    this.punt_propia = punt.getPuntuacionPropia
    this.punt_hijos = punt.getPuntuacionHijos
    this.repeticiones = punt.getRepeticiones
  }
  
  def setPuntuacionSinRepeticiones(punt : Puntuacion) = {
    this.punt_propia = punt.getPuntuacionPropia
    this.punt_hijos = punt.getPuntuacionHijos
  }
  
  def getResumen : Float = {
    if(this.punt_propia <0 && this.punt_hijos < 0){
      return -1.0f
    }
    else if(this.punt_propia < 0)
      return this.punt_hijos
    else if(this.punt_hijos < 0)
      return this.punt_propia
    else return (this.punt_propia + this.punt_hijos) / 2
  }
  
  def setResumenHijos(Lhijos : List[Float]) = {
    var points = 0.0f
    var num = Lhijos.length
    if(Lhijos.length > 0){
      Lhijos.foreach{ p =>
        if(p >= 0) points += p
        else num -= 1
      }
      if(num > 0) this.punt_hijos = points/num
      else this.punt_hijos = -1.0f
    }
    else this.punt_hijos = -1.0f
  }
  
  def setResumenPropio(Lhijos : List[Float]) = {
    var points = 0.0f
    var num = Lhijos.length
    //print("Hijos: " + Lhijos + "\n")
    if(Lhijos.length > 0){
      Lhijos.foreach{ p =>
        if(p >= 0) points += p
        else num -= 1
      }
      if(num > 0) this.punt_propia = points/num
    }
  }
  
  def copy() : Puntuacion = {
    return new Puntuacion(this.punt_propia, this.punt_hijos, this.repeticiones)
  }
  
  override def toString() : String = "Propia: " + this.punt_propia + ", hijos: " + this.punt_hijos + ", repeticiones: " + this.repeticiones
  
  
}