@echo off
mkdir test
java -cp bin;lib\truezip-6.jar Sync4 -bundle "C:\Users\bill\Documents\Projects\Java" .\test\bundle
java -cp bin;lib\truezip-6.jar Sync4 "C:\Users\bill\Documents\Projects\Java" .\test\normal

REM 2018-11-27 bundle(2030 files, 44s, 55MB) / normal(2030 files, 22s, 154MB)
pause
