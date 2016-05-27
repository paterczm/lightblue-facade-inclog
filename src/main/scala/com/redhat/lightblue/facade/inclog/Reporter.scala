package com.redhat.lightblue.facade.inclog

import scala.collection.immutable.List
import scala.collection.mutable.HashMap
import scala.util.matching.Regex
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets

case class ReporterConfig(include: Option[Regex], exclude: Option[Regex])

object Reporter {

    private val inconsistencies = HashMap.empty[String, Int]

    def add(inc: IncLogEntry) = {
        inc.diff match {
            case p: IncLogPathDiff => {
                p.pathDiffs.foreach { path => addInconsistency(s"""${inc.bean} ${path.path}""") }

            }
            case c: IncLogCountDiff => { addInconsistency(s"""${inc.bean} <array elem not found>""") }
        }
    }

    private def addInconsistency(id: String) {
        inconsistencies.get(id) match {
            case Some(count) => inconsistencies.put(id, count + 1)
            case None => inconsistencies.put(id, 1)
        }
    }

    def generateReport(config: ReporterConfig) = {
        val incList = inconsistencies.toList.filter {
            case (id, _) => {

                val include = config.include match {
                    case Some(x) => x.pattern.matcher(id).matches()
                    case None => true
                }

                val exclude = config.exclude match {
                    case Some(x) => x.pattern.matcher(id).matches()
                    case None => false
                }

                include && !exclude

            }
        }.sortBy(_._2).reverse

        val clientPage = html.index.render(incList, new java.util.Date());

        Files.write(Paths.get("index.html"), clientPage.body.getBytes(StandardCharsets.UTF_8))

    }
}