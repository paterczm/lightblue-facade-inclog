

package com.redhat.lightblue.facade.inclog

import org.slf4j.LoggerFactory

/**
 * Parses jsonassert's diff.
 *
 */
object IncLogDiffExtractor {

    val logger = LoggerFactory.getLogger(IncLogDiffExtractor.getClass);

    def unapply(diffStr: String): Option[IncLogDiff] = {

        if (diffStr.startsWith("""[]: Expected""") || diffStr == "One object is null and the other isn't"
            || diffStr.contains("Could not find match for element")) {
            // there is no diff path - this inconsistency applies to object count returned
            return Some(IncLogCountDiff(null))
        }

        logger.debug("Diff before: " + diffStr)

        val fields = diffStr.split(';').toList map (parsePathDiff)

        if (logger.isDebugEnabled()) {
            fields.foreach { field => logger.debug("Diff after: " + field.path) }
        }

        Some(IncLogPathDiff(fields, null))
    }

    def parsePathDiff(_fieldStr: String): PathDiff = {
        val fieldStr = _fieldStr.trim()

        if (fieldStr.isEmpty()) {
            // there is a case when diff is empty... what is going on?
            return PathDiff("<empty>")
        }

        if (!fieldStr.contains("Expected") && !fieldStr.contains("Unexpected")) {
            throw new Exception(s"""Can't make up diff from: $fieldStr""")
        }

        var fieldName = fieldStr.replaceAll("""[,:]?\s*(Une|E)xpected.*""", "")
        fieldName = fieldName.replaceAll("""\.?\[.*?\]\.?""", ".")
        fieldName = if (fieldName.startsWith(".")) { fieldName.substring(1) } else { fieldName }
        fieldName = if (fieldName.endsWith(".")) { fieldName.substring(0, fieldName.length() - 1) } else { fieldName }

        // those are things like: [internalLastUpdatedDate=1462889346000],Expected: a JSON object,     but none found,
        // should it be a count diff?
        fieldName = if (fieldName.isEmpty()) { "<this>" } else { fieldName }

        PathDiff(fieldName)
    }
}