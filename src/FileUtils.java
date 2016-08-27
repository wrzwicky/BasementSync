import java.io.*;
import java.util.*;

public class FileUtils {

	/** @returns true if file is equal to or anywhere under dir. */
	public static boolean contains(File dir, File file) {
		return file.getAbsolutePath().startsWith(dir.getAbsolutePath());
	}

	/** @returns true if file is equal to or anywhere under any dir in given list. */
	public static boolean contains(Set<File> dirs, File file) {
		for(Iterator<File> i=dirs.iterator(); i.hasNext(); ) {
			if(contains(i.next(), file))
				return true;
		}
		return false;
	}
	
	public static String relativize(File dir, File child) {
		if(!contains(dir,child))
			throw new IllegalArgumentException("Child is not under given dir.");
		String s_dir = dir.getAbsolutePath();
		String s_child = child.getAbsolutePath();
		int p = s_dir.length();
		while(p<s_child.length() && s_child.charAt(p) == File.separatorChar)
			p++;
		return s_child.substring(p);
	}
	
	/** @return a set containing everything in given dir.*/
	public static Set<String> listAll(File dir) {
		String[] all = dir.list();
		Set<String> contents = new HashSet<String>();
		Collections.addAll(contents, all);
		return contents;
	}

	/** @return set of file names in given dir.  Sub-dirs names and contents are ignored. */
	public static Set<String> listFiles(File dir) {
		File[] all = dir.listFiles();
		Set<String> contents = new HashSet<String>();
		for(int i=0; i<all.length; i++) {
			if(all[i].isFile())
				contents.add(all[i].getName());
		}
		return contents;
	}
	
	/** @return set of dir names in given dir.*/
	public static Set<String> listDirs(File dir) {
		File[] ary = dir.listFiles();
		Set<String> set = new HashSet<String>();
		for(int i=0; i<ary.length; i++) {
			if(ary[i].isDirectory())
				set.add(ary[i].getName());
		}
		return set;
	}
	
	/** Copy one file from to given dest.  Dest can be file or dir.*/
	public static void copy(File source, File dest, boolean copyPerms) throws IOException {
		if(source.isDirectory())
			throw new IllegalArgumentException("Source cannot be a directory.");
		if(dest.isDirectory())
			dest = new File(dest, source.getName());
		
		dest.getParentFile().mkdirs();
		
		byte[] buf = new byte[1000000];
		FileInputStream in = new FileInputStream(source);
		try {
			FileOutputStream out = new FileOutputStream(dest);
			try {
				while(true) {
					int len = in.read(buf);
					if(len > 0)
						out.write(buf, 0, len);
					if(len < buf.length)
						break;
				}
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
		dest.setLastModified(source.lastModified());
		dest.setExecutable(source.canExecute());
		if(copyPerms) {
			dest.setReadable(source.canRead());
			dest.setWritable(source.canWrite());
		}
	}
	
	public static void delete(File source) throws IOException {
		if(source.isDirectory()) {
			File[] contents = source.listFiles();
			for(int i=0; i<contents.length; i++) {
				delete(contents[i]);
			}
		}
		if(!source.delete())
			throw new IOException("Unable to delete " + source);
	}

	/**
	 *  Compare file time stamps.
	 *  FAT32 is highly inaccurate.
	 */
	public static boolean timeEquals(long date1, long date2) {
		return Math.abs(date1-date2) < 2000;
	}
	
	private static long round(long val, long increment) {
		long m = val % increment;
		if(2*m >= increment)
			val += (increment-m);
		else
			val -= m;
		return val;
	}
	
	public static boolean matches(String filepath, String pattern) {
		// 'string' 'x*y' 'x?y' - precisely match name
		// 'x/**/y' matches dir 'x' item 'y', 0 or more dirs between
		// 'x/*' matches items in x
		// 'x/**' matches items in and under x
		// '**/x' matches all x anywhere under given root
		// '*/x' matches all x in a dir directly inside root
		// 'x**y' error -- **
		filepath = filepath.replace(File.separatorChar, '/');
		String[] path = filepath.split("/");
		String[] pats = pattern.split("/");
		for(int i=0,p=0; i<path.length; i++) {
//			if namematches(path,pattern)
		}
		return false;
	}
	
	public static boolean nameMatches(String name, String pattern) {
		return false;
	}
}
