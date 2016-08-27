@echo off
java -cp bin;lib\truezip-6.jar Sync4 -bundle "C:\Users\bill\Documents\Projects\Java" C:\temp\bundle
java -cp bin;lib\truezip-6.jar Sync4 "C:\Users\bill\Documents\Projects\Java" C:\temp\normal
pause
