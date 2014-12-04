@ECHO OFF
set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;configs;lib/log4j-api-2.0.2.jar;lib/log4j-core-2.0.2.jar;lib/RDTService.jar

java -Xms128m -Xmx384m -XX:+UnlockCommercialFeatures -XX:+FlightRecorder com.l3v.RDTService
