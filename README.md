# lightblue-facade-inclog
A command line tool to generate inconsistency reports.

Usage:
```
java -jar inclog-0.1-SNAPSHOT-jar-with-dependencies.jar
usage: IncLog$
 -f,--files <lb-inconsistency.log>   lb-inconsistencies.log files
 -e,--exclude <arg>                  exclude regex
 -i,--include <arg>                  include regex
```

Example:
```
java -jar inclog-0.1-SNAPSHOT-jar-with-dependencies.jar -f log/lb-inconsistencies.log log/lb-inconsistencies.log.1 log/lb-inconsistencies.log.2 -i.*Subscription.*
```
