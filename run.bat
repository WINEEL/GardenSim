@echo off
REM Usage:
REM   run            -> launch JavaFX GUI
REM   run api ...    -> run headless API (extra args passed to Maven)
REM                     e.g. run api -Dexec.jvmArgs="-Ddays=20 -Dpest=0.25 -DtickMs=1000"

IF /I "%1"=="api" (
  SHIFT
  mvn -q -Papi exec:java %*
) ELSE (
  mvn -q -DskipTests javafx:run %*
)
