import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


// * need option to skip empty dirs
// * need to remove dirs that went away


// plan for Sync4:
//   get srcFiles
//   remove patterns
//   copy new files
//   delete excess files
//
//   get srcDirs
//   remove skips
//   remove patterns
//   recurse dirs
//   delete excess dirs



// java Sync -nodel -nocopy -crypt -squeeze -bundle SOURCE DEST -remove dirs .. -exclude filepat ..
//  nodel: don't del anything from target dir
//  nocopy: don't copy anything to target dir
//  crpyt: prompt for password, encrypt files and mangle names
//  squeeze: compress contents and mangle names
//  bundle: all files in a dir bundled into single archive (fully rebuilt on any changes)
//  remove <dir relative to source>: skip dir on source; remove from dest
//  exclude <pattern relative to source>: remove any objects that matches this pattern (i.e. **/CVS, *.bak)

public class Sync3 {
	boolean delete = true;
	boolean copy = true;
	boolean encrypt = false;
	boolean compress = false;
	boolean bundle = false;

	File source_dir = null;
	File dest_dir = null;
	Set<File> skips = new HashSet<File>();
	Set<String> excludes = new HashSet<String>();
	
	int total_dirs = 0;
	int syncd_dirs = 0;
	int deled_dirs = 0;
	int blokd_dirs = 0;
	
	int total_fils = 0;
	int copyd_fils = 0;
	int deled_fils = 0;
	int blokd_fils = 0;
	
	
	
	public Sync3() {
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
			else if(args[i].equalsIgnoreCase("-remove")) {
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
		System.out.println(this.skips.size() + " dirs to remove");
		System.out.println(this.excludes.size() + " patterns to filter out");
	
		System.out.println("-------------------------------------------------------");
		System.out.println("Starting sync:");

		long start = System.currentTimeMillis();
		doSync(this.source_dir);
		long end = System.currentTimeMillis();

		System.out.println("-------------------------------------------------------");
		System.out.println(
				this.total_dirs + " dirs scanned: "
				+ this.blokd_dirs + " dirs blocked, "
				+ this.deled_dirs + " dirs deleted, "
				+ this.syncd_dirs + " dirs synchronized.");
		System.out.println(
				this.total_fils + " files scanned: "
				+ this.blokd_fils + " files blocked, "
				+ this.copyd_fils + " files copied, "
				+ this.deled_fils + " files deleted.");
		System.out.println("Time required: " + (end-start)/1000.0 + " seconds");
		System.out.println("Done!");
	}

	
	
	/** Sync given dir (if not blocked) and all sub-dirs. */
	private void doSync(File source_dir) {
		this.total_dirs++;
		
		// Remove dir if blocked by 'skips' list.
		if(skips.contains(source_dir)) {
			this.blokd_dirs++;
			File dest = new File(this.dest_dir, FileUtils.relativize(this.source_dir, source_dir));
			if(dest.exists()) {
				System.out.println("DELETING " + dest);
				try {
					FileUtils.delete(dest);
				}					
				catch(IOException ex) {
					System.out.println("  FAILED due to: " + ex.getMessage());
				}
			}
		}
		
		// Else sync contents of dir.
		else {
			syncFiles(source_dir);
			syncDirs(source_dir);
		}
	}
	
	
	
	/** Simply iterate through subdirs. */
	private void syncDirs(File source_dir) {
		File dsrc = source_dir;

		// Process sub-dirs
		Set<String> srcdirs = FileUtils.listDirs(dsrc);
		for(Iterator<String> fi = srcdirs.iterator(); fi.hasNext(); ) {
			String n = fi.next();
			File fsrc = new File(dsrc, n);
			doSync(fsrc);
		}
		
		// Remove excess dirs that disappeared from source.
		if(this.delete) {
			File ddst = new File(this.dest_dir, FileUtils.relativize(this.source_dir, dsrc));
			Set<String> dstdirs = FileUtils.listDirs(ddst);
			dstdirs.removeAll(srcdirs);
			if(!dstdirs.isEmpty()) {
				for(Iterator<String> fi = dstdirs.iterator(); fi.hasNext(); ) {
					String n = fi.next();
					File fdst = new File(ddst, n);
					System.out.println("DELETING " + fdst);
					this.deled_dirs++;
					try {
						FileUtils.delete(fdst);
					}
					catch(IOException ex) {
						System.out.println("  FAILED due to: " + ex.getMessage());
					}
				}
			}
		}
	}
	
	
	
	/** Copy and remove files as needed; ignore dirs. */
	private void syncFiles(File source_dir) {
		File dsrc = source_dir;
		File ddst = new File(this.dest_dir, FileUtils.relativize(this.source_dir, dsrc));
		boolean touched = false;

		Set<String> srcfiles = FileUtils.listFiles(dsrc);

		// Copy files
		if(this.copy) {
			// Create dirs, even if empty
			ddst.mkdirs();
			
			// Copy all files that differ
			for(Iterator<String> fi = srcfiles.iterator(); fi.hasNext(); ) {
				String n = fi.next();
				File fsrc = new File(dsrc, n);
				//WRZ if matches excludes, blokd_fils++ and delete
				File fdst = new File(ddst, n);
				if(needsSync(fsrc, fdst)) {
					if(!touched) {
						System.out.println(source_dir);
						this.syncd_dirs++;
						touched = true;
					}
					this.copyd_fils++;
					try {
						FileUtils.copy(fsrc, fdst, false);
					}
					catch(IOException ex) {
						System.out.println("  FAILED " + fsrc.getName() + "\n    due to: " + ex.getMessage());
					}
				}
			}
		}
		
		// Remove excess files
		if(this.delete) {
			Set<String> dstfiles = FileUtils.listFiles(ddst);
			dstfiles.removeAll(srcfiles);
			
			if(!dstfiles.isEmpty()) {
				if(!touched) {
					System.out.println(source_dir);
					this.syncd_dirs++;
					touched = true;
				}
				for(Iterator<String> fi = dstfiles.iterator(); fi.hasNext(); ) {
					String n = fi.next();
					File fdst = new File(ddst, n);
					if(fdst.delete())
						this.deled_fils++;
					else
						System.out.println("FAILED to delete " + fdst);
				}
			}
		}
	}

	
	
	/** @return true if given file needs syncing. */
	private boolean needsSync(File srcFile, File dstFile) {
		this.total_fils++;
		boolean match = (dstFile.exists()
				&& dstFile.length() == srcFile.length()
				&& FileUtils.timeEquals(dstFile.lastModified(), srcFile.lastModified()));
		return !match;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		Sync3 s = new Sync3();
		s.parseArgs(args);
		s.start();
	}
}
