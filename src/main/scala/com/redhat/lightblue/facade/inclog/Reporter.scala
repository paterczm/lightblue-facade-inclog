package com.redhat.lightblue.facade.inclog

import scala.collection.immutable.List
import scala.collection.mutable.HashMap
import scala.util.matching.Regex
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.charset.Charset
import java.io.FileWriter

object Reporter {

    private val inconsistencies = HashMap.empty[String, Int]

    def add(inc: IncLogEntry) = {
        inc.diff match {
            case p: IncLogPathDiff => {
                p.pathDiffs.foreach {
                    path =>
                        {
                            p.id = s"""${inc.bean} ${path.path}"""
                            addInconsistency(p.id, inc)
                        }
                }

            }
            case c: IncLogCountDiff => { addInconsistency(s"""${inc.bean} <array elem not found>""", inc) }
        }
    }

    private def addInconsistency(id: String, inc: IncLogEntry) {
        inconsistencies.get(id) match {
            case Some(count) => inconsistencies.put(id, count + 1)
            case None => inconsistencies.put(id, 1)
        }

        saveInconsistencyToFile(id, inc)
    }

    private def saveInconsistencyToFile(fileName: String, inc: IncLogEntry) {
        val fw = new FileWriter(s"""inc/$fileName.txt""", true);
        fw.write(inc.line + "\n\n\n");
        fw.close()
    }

    def generateReport() = {
        val incList = inconsistencies.toList.sortBy(_._2).reverse

        val clientPage = html.index.render(incList, new java.util.Date());

        Files.write(Paths.get("index.html"), clientPage.body.getBytes(StandardCharsets.UTF_8))

    }
}