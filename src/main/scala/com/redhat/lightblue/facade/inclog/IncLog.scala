

package com.redhat.lightblue.facade.inclog

import scala.io.Source
import scala.util.matching.Regex

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException

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

    val outputOption = Option.builder("o")
        .required(true)
        .desc("Report output directory")
        .longOpt("output")
        .hasArg()
        .build();

    val dontStopOnErrorOption = Option.builder()
        .required(false)
        .desc("Continue processing even when inconsistency parsing fails")
        .longOpt("dont-stop-on-error")
        .build();

    options.addOption(inconsistencyLogFilesOption);
    options.addOption(includeOption)
    options.addOption(excludeOption)
    options.addOption(outputOption)
    options.addOption(dontStopOnErrorOption);

    val parser = new DefaultParser();

    try {
        val cmd = parser.parse(options, args);

        var filePaths = cmd.getOptionValues("files");

        var incsProcessed = 0
        var linesProcessed = 0

        implicit val config = Config(
            createReportDirIfNotExists(cmd.getOptionValue("o")),
            cmd.hasOption('i') match {
                case true => Some(new Regex(cmd.getOptionValue('i')))
                case false => None
            },
            cmd.hasOption('e') match {
                case true => Some(new Regex(cmd.getOptionValue('e')))
                case false => None
            })

        filePaths foreach { filePath =>
            for (line <- Source.fromFile(filePath).getLines()) {

                linesProcessed += 1

                try {
                    line match {
                        case IncLogEntryExtractor(inc) => {
                            Reporter.add(inc)
                            incsProcessed += 1
                        }
                        case _ => {
                            // ignore
                        }
                    }
                } catch {
                    case e: Exception => {
                        if (cmd.hasOption("dont-stop-on-error")) {
                            logger.error("", e)
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }

        println(s"""Processed $incsProcessed/$linesProcessed (recogonized inconsistencies / all log lines)""")

        Reporter.generateReport()

    } catch {
        case e: MissingOptionException => {
            logger.error(e.getMessage());
            var formatter = new HelpFormatter();
            formatter.printHelp(120, IncLog.getClass.getSimpleName, "", options, null);
            System.exit(1);
        }
    }

    def createReportDirIfNotExists(path: String): File = {
        val outDir = new File(path)
        if (outDir.exists() && outDir.isDirectory()) {
            // do nothing
        } else if (!outDir.exists()) {
            // create
            outDir.mkdirs()
        } else if (outDir.exists() && outDir.isFile()) {
            // break
            throw new IOException(s"""${outDir} is a file, not a dir""")
        }

        val incDir = new File(s"""$path/inc""")
        if (!incDir.exists()) {
            incDir.mkdir()
        }

        outDir
    }

}
