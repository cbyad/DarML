package com.upmc.dar

import java.io.{File, InputStream, OutputStream, PrintWriter}
import scala._

object Utils {
  var cols : Array[String] = new Array[String](500000)
  val home = "/Users/cb_mac/Desktop/UPMC/M2/AAGA/TME/BrooklynHousePricing/files/brooklyn_sales_map.csv"
  val out = "/Users/cb_mac/Desktop/UPMC/M2/AAGA/TME/BrooklynHousePricing/files/toto.csv"

  def myLittleParser(in : String , out: String): Unit = {

    val bufferedSource = io.Source.fromFile(in)
    var writer = new PrintWriter(new File(out))

    for (line <- bufferedSource.getLines) {
      cols = line.split(",").map(_.trim)
      // do whatever you want with the columns here

    writer.write(s"${cols(1)},${cols(2)},${cols(3)},${cols(4)},${cols(5)},${cols(6)},${cols(8)},"+
      s"${cols(9)},${cols(11)},${cols(12)},${cols(13)},${cols(14)},${cols(15)},${cols(16)},${cols(17)},${cols(18)}," +
      s"${cols(19)},${cols(20)},${cols(21)},${cols(22)},${cols(49)},${cols(55)},${cols(65)}," +
      s"${cols(66)},${cols(82)},${cols(83)},${cols(84)},${cols(85)},${cols(95)},${cols(97)},${cols(100)}," +
      s"${cols(107)},${cols(109)},${cols(110)}")
      writer.write("\n")
    }
    println(cols.length)

    bufferedSource.close
    writer.close()
  }

  def main(args: Array[String]): Unit = {
    myLittleParser(home,out)
  }

}
