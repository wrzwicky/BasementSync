import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;


// * need option to skip empty dirs
// * need to remove dirs that went away


// java Sync -nodel -nocopy -crypt -squeeze -bundle SOURCE DEST -skip dirs .. -exclude filepat ..
//  nodel: don't del anything from target dir
//  nocopy: don't copy anything to target dir
//  crpyt: prompt for password, encrypt files and mangle names
//  squeeze: compress contents and mangle names
//  bundle: all files in a dir bundled into single archive (fully rebuilt on any changes)
//  x=skip: drop these exact dirs (relative to SOURCE)
//  X=exclude: drop any objects that match these patterns (i.e. **/CVS, *.bak)
//
//  - assemble dir list, remove skips
//  - build plan:  for each dir
//     - compare file lists
//     - if same, remove dir from list
//  - log planned dirs
//  - sync:  for each dir
//     - remove del'd files
//     - copy new files

public class Sync2 {
	boolean delete = true;
	boolean copy = true;
	boolean encrypt = false;
	boolean compress = false;
	boolean bundle = false;

	File source_dir = null;
	File dest_dir = null;
	Set<File> skips = new HashSet<File>();
	Set<String> excludes = new HashSet<String>();
	
	
	
	public Sync2() {
	}
	
	public void parseArgs(String[] args) throws IOException {
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-nodel")) {
				this.delete = false;
			}
			else if(args[i].equalsIgnoreCase("-nocopy")) {
				this.copy = false;
			}
			else if(args[i].equalsIgnoreCase("-crypt")) {
				this.encrypt = true;
			}
			else if(args[i].equalsIgnoreCase("-squeeze")) {
				this.compress = true;
			}
			else if(args[i].equalsIgnoreCase("-bundle")) {
				this.bundle = true;
			}
			else if(args[i].equalsIgnoreCase("-skip")) {
				this.skips.add(new File(this.source_dir, args[++i]).getCanonicalFile());
			}
			else if(args[i].equalsIgnoreCase("-exclude")) {
				this.excludes.add(args[++i]);
			}
			else {
				if(this.source_dir == null)
					this.source_dir = new File(args[i]).getCanonicalFile();
				else if(this.dest_dir == null)
					this.dest_dir = new File(args[i]).getCanonicalFile();
				else
					throw new IllegalArgumentException("Too many arguments.");
			}
		}
		
		if(this.source_dir == null)
			throw new IllegalArgumentException("Source dir not specified.");
		if(!this.source_dir.exists())
			throw new IllegalArgumentException("Source dir does not exist.");
		if(!this.source_dir.isDirectory())
			throw new IllegalArgumentException("Source must be a directory.");
		
		if(this.dest_dir == null)
			throw new IllegalArgumentException("Destination dir not specified.");
		if(this.dest_dir.exists() && !this.dest_dir.isDirectory())
			throw new IllegalArgumentException("Destination must be a directory.");
	}
	
	public void start() {
		System.out.println("-------------------------------------------------------");
		System.out.println("Sync from " + this.source_dir);
		System.out.println("     to   " + this.dest_dir);
		System.out.println(this.skips.size() + " dirs to skip");
		System.out.println(this.excludes.size() + " patterns to filter out");
		System.out.println("-------------------------------------------------------");
		System.out.println("Measuring task ...");
		
		// ------------------------------------------------------
		// BUILD PLAN
		//

		// Build list of requested dirs that need syncing.

		FileIterator2 fit = new FileIterator2(source_dir);
		// remove dirs listed in 'skips', skip files
		fit.addFilter(new FileFilter() {
			@Override
			public boolean accept(File item) {
				return (item.isDirectory() ? !skips.contains(item) : false);
			}
		});
		// remove dirs matching 'excludes'
		//  ...

		// Find which folders need to be sync'd
		int filtered_items = 0;
		List<File> dirsToSync = new ArrayList<File>();
		if(needsSync(this.source_dir))
			dirsToSync.add(this.source_dir);
		for(; fit.hasNext(); ) {
			File dir = fit.next();
			filtered_items++;
			if(needsSync(dir))
				dirsToSync.add(dir);
		}
		
		System.out.println(filtered_items + " dirs total, " + dirsToSync.size() + " dirs need to be synchronized.");
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Starting copy:");

		for(Iterator<File> di = dirsToSync.iterator(); di.hasNext(); ) {
			File dsrc = di.next();
			File ddst = new File(this.dest_dir, FileUtils.relativize(this.source_dir, dsrc));
			System.out.println(dsrc);
			//create dirs, even if empty
			ddst.mkdirs();
			//copy all the files
			for(Iterator<String> fi = FileUtils.listFiles(dsrc).iterator(); fi.hasNext(); ) {
				String n = fi.next();
				File fsrc = new File(dsrc, n);
				File fdst = new File(ddst, n);
				if(needsSync(fsrc, fdst))
					try {
						FileUtils.copy(fsrc, fdst, false);
					}
					catch(IOException ex) {
						System.out.println("SKIPPING " + fsrc);
						ex.printStackTrace();
					}
			}
		}
	}
	
	
	
	/** @return true if given dir needs syncing. */
	private boolean needsSync(File src) {
		File dst = new File(this.dest_dir, FileUtils.relativize(this.source_dir, src));
		if(!dst.exists()) {
//			System.out.print('X');
			return true;
		}
		
		Set<String> srcfiles = FileUtils.listFiles(src);
		Set<String> dstfiles = FileUtils.listFiles(dst);

		// check for new or out-of-date files
		if(this.copy) {
			for(Iterator<String> i = srcfiles.iterator(); i.hasNext(); ) {
				String n = i.next();
				if(dstfiles.remove(n)) {
					// success: dest file exists, so compare to source
					if(needsSync(new File(src,n), new File(dst,n))){
//						System.out.print('F');
						return true;
					}
				}
				else {
					// failed: dest file absent
//					System.out.print('D');
					return true;
				}
			}
		}
		
		// check for removed files
		if(this.delete) {
			if(!dstfiles.isEmpty()) {
//				System.out.print('R');
				return true;
			}
		}

		return false;
	}

	
	
	/** @return true if given file needs syncing. */
	private boolean needsSync(File srcFile, File dstFile) {
		boolean match = (dstFile.exists()
				&& dstFile.length() == srcFile.length()
				&& FileUtils.timeEquals(dstFile.lastModified(), srcFile.lastModified()));
		return !match;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		Sync2 s = new Sync2();
		s.parseArgs(args);
		s.start();
	}
}
