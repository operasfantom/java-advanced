@echo off
set CLASSPATH=../../out/
echo %classpath%

start rmiregistry
start java rmi.Server
