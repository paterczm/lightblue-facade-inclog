# lightblue-facade-inclog
A command line tool to generate inconsistency reports.

Usage:
```
java -jar inclog-0.1-SNAPSHOT-jar-with-dependencies.jar
usage: IncLog$
 -e,--exclude <arg>                  exclude regex
 -f,--files <lb-inconsistency.log>   lb-inconsistencies.log files
 -i,--include <arg>                  include regex
 -o,--output <arg>                   Report output directory
```

Example:
```
java -jar inclog-0.1-SNAPSHOT-jar-with-dependencies.jar -f log/lb-inconsistencies.log log/lb-inconsistencies.log.1 log/lb-inconsistencies.log.2 -i.*Subscription.* -o .
```
