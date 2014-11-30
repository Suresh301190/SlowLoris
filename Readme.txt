Network Security Project 4

Implementation of SlowLoris Attack on JAVA

Usage: the package includes a Runnable jar SlowLoris.jar

To run the Attack use the following command

(for Help) Values in (Defaults)
java -jar SlowLoris.jar --h

-h hostname (localhost)
-p port (80)
-c No_of_connections (50) 
-i Interval_Between_Headers_in_ms (10000) 
-t test_Duration (120s)
-r connections_per_s (50)
-d Output_directory (if you want the Graph representation) 
-o probe Timeout (5000)

e.g. Command
java -jar SlowLoris.jar -c 1000 -r 200 -d "/home/ocean/Desktop/" -t 60 -o 3000

The Application is tested with apache v2.0.63 (Vulnerable) and apache 2.4.x which is bundled with XAMPP LAMP Stack