package com.redhat.lightblue.facade.inclog

import org.slf4j.LoggerFactory

case class IncLogEntry(date: String, bean: String, method: String, paramStr: String, diff: IncLogDiff, line: String)
case class IncLogPathDiff(pathDiffs: List[PathDiff]) extends IncLogDiff
case class IncLogCountDiff() extends IncLogDiff
case class PathDiff(path: String)

trait IncLogDiff {}

object IncLogEntryExtractor {

    val logger = LoggerFactory.getLogger(IncLogEntryExtractor.getClass);

    val regexp = """(\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d,\d\d\d) \[.*?\] WARN  \[com.redhat.lightblue.migrator.facade.ConsistencyChecker\] \[.*?\] Inconsistency found in (\w+)\.(\w+)\((.*?)\) - diff: (.*)""".r

    def unapply(line: String): Option[IncLogEntry] = {

        // some logs contain full response, some don't, because it's too big - normalize
        val inconsistencyWithoutResponses = if (line.contains("- legacyJson:")) {
            line.split(" - legacyJson:")(0);
        } else {
            line
        }

        regexp.findFirstIn(inconsistencyWithoutResponses) match {
            case Some(regexp(date, bean, method, paramStr, diffStr)) => {
                try {
                    val diff = diffStr match {
                        case IncLogDiffExtractor(diff) => diff
                    }
                    Some(IncLogEntry(date, bean, method, paramStr, diff, line))
                } catch {
                    case e: Exception => { throw new Exception(s"""Processing line $line failed""", e) }
                }
            }
            case None => None
        }
    }
}