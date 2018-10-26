@echo off
rem ** JAC startup script for WINDOWS 2000 ** 

rem ** the location of the JAC distribution **
set JAC_ROOT="c:\jac"
SET CLASSPATH=".;C:\JAC\lib\bcel.jar;C:\JAC\lib\gnu-regexp.jar;C:\JAC\lib\postgresql.jar;c:\JAC\jac.jar;c:\jdk1.3\jre\lib;C:\JAC\lib\cup.jar;C:\JAC\lib\jhotdraw.jar;C:\JAC\src"

if not z%CLASSPATH% == z goto start
    echo "Error: Java environment is not initialized..."
    echo "-- add the jac\jac.jar and jac\lib\*.jar to the CLASSPATH."
 goto fin
:start
    java -Djava.security.policy=%JAC_ROOT%\jac.policy jac.core.Jac -c -D -R %JAC_ROOT% %1 %2 %3 %4 %5 %6 %7 %8
:fin
