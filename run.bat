@echo off
IF NOT EXIST .\webapps\timesheet.properties (
mkdir webapps 
xcopy /Y timesheet.properties .\webapps\
)
mvn jetty:run