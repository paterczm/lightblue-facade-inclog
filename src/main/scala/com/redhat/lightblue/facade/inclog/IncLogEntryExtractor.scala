package com.redhat.lightblue.facade.inclog

import org.slf4j.LoggerFactory
import scala.util.matching.Regex

case class IncLogEntry(date: String, bean: String, method: String, paramStr: String, var diff: IncLogDiff, line: String) {
    def call = s"""$bean.$method"""
}
case class IncLogPathDiff(pathDiffs: List[PathDiff], override var id: String) extends IncLogDiff
case class IncLogCountDiff(override var id: String) extends IncLogDiff
case class PathDiff(path: String)

abstract class IncLogDiff() {
    var id: String
}

case class Config(val includeRegex: Option[Regex], val excludeRegex: Option[Regex])

object IncLogEntryExtractor {

    val logger = LoggerFactory.getLogger(IncLogEntryExtractor.getClass);

    val regexp = """(\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d,\d\d\d) \[.*?\] WARN  \[com.redhat.lightblue.migrator.facade.ConsistencyChecker\] \[.*?\] Inconsistency found in (\w+)\.(\w+)\((.*?)\) - diff: (.*)""".r

    def unapply(line: String)(implicit config: Config): Option[IncLogEntry] = {

        // some logs contain full response, some don't, because it's too big - normalize
        val inconsistencyWithoutResponses = if (line.contains("- legacyJson:")) {
            line.split(" - legacyJson:")(0);
        } else {
            line
        }

        regexp.findFirstIn(inconsistencyWithoutResponses) match {
            case Some(regexp(date, bean, method, paramStr, diffStr)) => {
                try {

                    val incLogEntry = IncLogEntry(date, bean, method, paramStr, null, inconsistencyWithoutResponses)

                    include(incLogEntry.call, config) match {
                        case false => None
                        case true => {
                            val diff = diffStr match {
                                case IncLogDiffExtractor(diff) => diff
                            }

                            incLogEntry.diff = diff
                            Some(incLogEntry)
                        }
                    }

                } catch {
                    case e: Exception => { throw new Exception(s"""Processing line $line failed""", e) }
                }
            }
            case None => {
                // not an inconsistency, can be facade's InconsistencyChecker error
                // TODO: handle "payload and diff is greater than X bytes" inconsistencies
                logger.warn(s"""Not recognized as inconsistency: $line""")
                None
            }
        }
    }

    private def include(call: String, config: Config): Boolean = {
        val include = config.includeRegex match {
            case Some(x) => x.pattern.matcher(call).matches()
            case None => true
        }

        val exclude = config.excludeRegex match {
            case Some(x) => x.pattern.matcher(call).matches()
            case None => false
        }

        include && !exclude
    }
}