

# java Sync -nodel -nocopy -crypt -squeeze -bundle SOURCE DEST -x dirs.. -X filepat
#  nodel: don't del anything from target dir
#  nodelskip: don't delete from DEST files that are suppressed by -x and -X
#  nocopy: don't copy anything to target dir
#  crpyt: prompt for password, encrypt files and mangle names
#  squeeze: compress contents and mangle names
#  bundle: all files in a dir bundled into single archive (fully rebuilt on any changes)
#  x=skip: drop these exact dirs (relative to SOURCE)
#  X=exclude: drop any objects that match these patterns (i.e. **/CVS, *.bak)
#
#  - assemble dir list, remove skips
#  - build plan:  for each dir
#     - compare file lists
#     - if same, remove dir from list
#  - log planned dirs
#  - sync:  for each dir
#     - remove del'd files
#     - copy new files



class Sync:
    pass



if __name__ == "__main__":
    pass
