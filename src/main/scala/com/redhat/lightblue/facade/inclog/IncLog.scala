

package com.redhat.lightblue.facade.inclog

import scala.io.Source
import org.apache.commons.cli.Options
import org.apache.commons.cli.Option
import org.slf4j.LoggerFactory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.HelpFormatter

object IncLog extends App {

    val logger = LoggerFactory.getLogger(IncLog.getClass);

    val options = new Options();

    val inconsistencyLogFilesOption = Option.builder("f")
        .required(true)
        .desc("lb-inconsistencies.log files")
        .longOpt("files")
        .hasArgs()
        .argName("lb-inconsistency.log")
        .build();

    options.addOption(inconsistencyLogFilesOption);

    val parser = new DefaultParser();

    try {
        val cmd = parser.parse(options, args);

        var filePaths = cmd.getOptionValues("files");

        var incsProcessed = 0
        var linesProcessed = 0

        filePaths foreach { filePath =>
            for (line <- Source.fromFile(filePath).getLines()) {

                linesProcessed += 1

                line match {
                    case IncLogEntryExtractor(inc) => {
                        Reporter.add(inc)
                        incsProcessed += 1
                    }
                    case _ => {
                        // not an inconsistency, can be facade's InconsistencyChecker error
                        // TODO: handle "payload and diff is greater than X bytes" inconsistencies
                        logger.warn(s"""Not recognized as inconsistency: $line""")
                    }
                }
            }
        }

        println(s"""Processed $incsProcessed/$linesProcessed (recgonized inconsistencies / all log lines)""")

        Reporter.generateReport()

    } catch {
        case e: MissingOptionException => {
            logger.error(e.getMessage());
            var formatter = new HelpFormatter();
            formatter.printHelp(120, IncLog.getClass.getSimpleName, "", options, null);
            System.exit(1);
        }
    }

}
