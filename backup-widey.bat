@echo off
java -cp bin Sync3 "C:\Documents and Settings\Bill\My Documents" I:\Backups\Widey -remove !!backups -remove "!!backed-up" -remove CD-DVD -remove Emu -remove "My Music" -remove "My Videos" -remove "My VMs" -remove NeroVision -remove Projects\eclipse\.metadata\.plugins -remove Projects\Thesis\.metadata\.plugins -remove Work\workspace\.metadata\.plugins
pause
