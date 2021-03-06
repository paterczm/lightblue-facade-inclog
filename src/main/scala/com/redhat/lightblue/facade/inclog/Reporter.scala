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
import java.io.File

object Reporter {

    private val inconsistencies = HashMap.empty[String, Int]

    def add(inc: IncLogEntry)(implicit config:Config) = {
        inc.diff match {
            case p: IncLogPathDiff => {
                p.pathDiffs.foreach {
                    path =>
                        {
                            addInconsistency(s"""${inc.bean} ${path.path}""", inc, config.outputDir)
                        }
                }

            }
            case c: IncLogCountDiff => { addInconsistency(s"""${inc.bean} <array size mismatch>""", inc, config.outputDir) }
        }
    }

    private def addInconsistency(id: String, inc: IncLogEntry, dir: File) {
        inconsistencies.get(id) match {
            case Some(count) => inconsistencies.put(id, count + 1)
            case None => inconsistencies.put(id, 1)
        }

        saveInconsistencyToFile(new File(s"""${dir.getAbsolutePath}/inc/${id}.txt"""), inc)
    }

    private def saveInconsistencyToFile(file: File, inc: IncLogEntry) {
        val fw = new FileWriter(file, true);
        fw.write(inc.line + "\n\n\n");
        fw.close()
    }

    def generateReport()(implicit config: Config) = {
        val incList = inconsistencies.toList.sortBy(_._2).reverse

        val clientPage = html.index.render(incList, new java.util.Date());

        Files.write(Paths.get(s"""${config.outputDir.getAbsolutePath}/index.html"""), clientPage.body.getBytes(StandardCharsets.UTF_8))

    }
}
