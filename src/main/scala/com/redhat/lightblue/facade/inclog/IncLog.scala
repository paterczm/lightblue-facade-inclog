

package com.redhat.lightblue.facade.inclog

import scala.io.Source
import scala.util.matching.Regex

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory

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

    val includeOption = Option.builder("i")
        .required(false)
        .desc("include regex")
        .longOpt("include")
        .hasArg()
        .build();

    val excludeOption = Option.builder("e")
        .required(false)
        .desc("exclude regex")
        .longOpt("exclude")
        .hasArg()
        .build();

    options.addOption(inconsistencyLogFilesOption);
    options.addOption(includeOption)
    options.addOption(excludeOption)

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

        println(s"""Processed $incsProcessed/$linesProcessed (recogonized inconsistencies / all log lines)""")

        val config = ReporterConfig(
            cmd.hasOption('i') match {
                case true => Some(new Regex(cmd.getOptionValue('i')))
                case false => None
            },
            cmd.hasOption('e') match {
                case true => Some(new Regex(cmd.getOptionValue('e')))
                case false => None
            })

        Reporter.generateReport(config)

    } catch {
        case e: MissingOptionException => {
            logger.error(e.getMessage());
            var formatter = new HelpFormatter();
            formatter.printHelp(120, IncLog.getClass.getSimpleName, "", options, null);
            System.exit(1);
        }
    }

}
