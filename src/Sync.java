import java.io.*;
import java.util.*;


// java Sync -nodel -nocopy -crypt -squeeze -bundle -target dir -source dir -skip dirs.. -exclude filepat
//  nodel: don't del anything from target dir
//  nocopy: don't copy anything to target dir
//  crpyt: prompt for password, encrypt files and mangle names
//  squeeze: compress contents and mangle names
//  bundle: all files in a dir bundled into single archive (fully rebuilt on any changes)
//
//  - assemble dir list, remove skips
//  - build plan:  for each dir
//     - compare file lists
//     - if same, remove dir from list
//  - log planned dirs
//  - sync:  for each dir
//     - remove del'd files
//     - copy new files


public class Sync {
	public static void main(String[] args) throws IOException {
		File target = new File(args[0]);
		File source = new File(args[1]);
		Set<File> excludes = new HashSet<File>();
		for(int i=2; i<args.length; i++)
			excludes.add(new File(source, args[i]));

		Iterator<File> i = new FileIterator( source, excludes);
		for(; i.hasNext(); ) {
			File src = i.next();
			File dst = new File(target, FileUtils.relativize(source, src));
			if(src.isDirectory()) {
				// Log dirs
				System.out.println(src);
				// Delete dest files that are missing from source
				if(dst.exists()) {
					Set<String> srcdir  = FileUtils.listFiles(src);
					Set<String> destdir = FileUtils.listFiles(dst);
					destdir.removeAll(srcdir);
					for(Iterator<String> j=destdir.iterator(); j.hasNext(); ) {
						File gone = new File(dst, j.next());
						System.out.println("DELETING " + gone);
						gone.delete();
					}
				}
			}
			else {
				// and copy files
				if(dst.exists() && dst.length() == src.length() && FileUtils.timeEquals(dst.lastModified(), src.lastModified()))
					continue;
//				System.out.println(src);
//				if(dst.exists())
//					System.out.println("  d/s: " + dst.length() + "/" + src.length() + " and " + dst.lastModified() + "/" + src.lastModified());
//				System.out.print(">");
				try {
					FileUtils.copy(src, dst, false);
				}
				catch(IOException ex) {
					System.out.println("SKIPPING " + src);
					ex.printStackTrace();
				}
			}
		}
	}
}
