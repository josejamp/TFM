package main.scala

import java.util.concurrent._

class RepeatRunnable(task : Boolean, delayInicial : Long, intervalo : Long, maximo : Int) {
  
  val tarea = task
  val d = delayInicial
  val i = intervalo
  val m = maximo

  val executor = new ScheduledThreadPoolExecutor(1)
  val t : ScheduledFuture[_] = null

  class MyTask(maximo : Int, fun : Boolean) extends Runnable {
        var rep = 1;

        def run() {
            if (rep > maximo) {
                t.cancel(false);
            }
            else{
              rep += 1
              if(fun) print("Proceso exitoso...")
              else print("Proceso fallido...")
            }
        }
    }
  
  def execute() = executor.scheduleAtFixedRate(new MyTask(m,tarea), d, i, TimeUnit.SECONDS);
}