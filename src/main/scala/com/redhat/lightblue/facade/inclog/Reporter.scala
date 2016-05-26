package com.redhat.lightblue.facade.inclog

import scala.collection.mutable.HashMap
import scala.collection.mutable.MutableList

object Reporter {

    var countInconsistenciesCount = 0
    val inconsistentPaths = HashMap.empty[String, Int]

    def add(inc: IncLogEntry) = {

        inc.diff match {
            case p: IncLogPathDiff => {
                p.fields.foreach { field =>

                    inconsistentPaths.get(field.path) match {
                        case Some(count) => inconsistentPaths.put(field.path, count + 1)
                        case None => inconsistentPaths.put(field.path, 1)
                    }

                }

            }
            case c: IncLogCountDiff => {
                countInconsistenciesCount+=1
            }
        }

    }

    def generateReport() = {
        val list = MutableList.empty[(String, Int)]

        inconsistentPaths.toList.sortBy(_._2).reverse foreach { case (path, count) => println(s"""$path: $count""")}
        println(s"""Count inconsistencies: $countInconsistenciesCount""")
    }

}