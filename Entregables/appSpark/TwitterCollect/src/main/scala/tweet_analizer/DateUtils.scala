package tweet_analizer

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.Instant
import java.util.Date
import java.util.Locale

object DateUtils {
  
  
  def parseDateString(dateString : String) : Long = {
    var formatoLocal = Locale.US
    var dateTimeFormat = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy", formatoLocal)
    var localTime = LocalDateTime.parse(dateString, dateTimeFormat)
    var zdt = localTime.atZone(ZoneId.systemDefault())
    zdt.toEpochSecond()
  }
  
  def formatDate(dateLong : Long) : String = {
    var date = LocalDateTime.ofInstant(Instant.ofEpochSecond(dateLong), ZoneId.systemDefault())
    print("Date: " + date + "\n")
    var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    date.format(dateFormatter)
  }
  
  
  def toHoursEpoch(date : Long) : Long = {
    return date/3600
  }
  
  def toSecondsFromHours(date : Long) : Long = {
    return (date*3600)
  }
  
  def toMilliSecondsFromHours(date : Long) : Long = {
    return (date*3600*1000)
  }
  
  def toHoursFromDays(date : Long) : Long =  {
    return (date*24)
  }
  
  
}