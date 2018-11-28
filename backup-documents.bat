@echo off

:NameA
set NA=%COMPUTERNAME%
if NOT [%NA%]==[] GOTO NameB

set NA=%USERDOMAIN%
if NOT [%NA%]==[] GOTO NameB

set NA=(computer)

:NameB
set NB=%USERNAME%
if NOT [%NB%]==[] GOTO GO

set NB="(user)"

:GO
set ouf=%NA%__%NB%
echo = [%ouf%]

mkdir test
java -cp bin Sync3 "C:\Users\bill\Documents" .\test\%ouf% ^
  -remove !!backups -remove "!!backed-up" -remove CD-DVD -remove Emu -remove "My Music" ^
  -remove "My Videos" -remove "My VMs" -remove NeroVision ^
  -remove Projects\eclipse\.metadata\.plugins ^
  -remove Projects\Thesis\.metadata\.plugins ^
  -remove Work\workspace\.metadata\.plugins
pause
