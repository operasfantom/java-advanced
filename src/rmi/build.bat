@echo off
call javac -cp .. Server.java Client.java -d ../../out
rem call rmic -d %classpath% examples.rmi.RemoteAccount examples.rmi.RemoteBank
