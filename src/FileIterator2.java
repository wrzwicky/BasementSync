import java.io.File;
import java.io.FileFilter;
import java.util.*;


public class FileIterator2 implements Iterator<File> {
	File root;
	private List<FileFilter> filters = new ArrayList<FileFilter>();
	private Stack<Iterator<File>> iterators = new Stack<Iterator<File>>();
	private File nextFile = null;
	
	
	public FileIterator2(File root) {
		this.root = root;
		pushDir(root);
	}
	
	@Override
	public boolean hasNext() {
		if(this.nextFile == null)
			this.nextFile = findNext();
		return this.nextFile != null;
	}

	@Override
	public File next() {
		// extra check cuz nobody can call findNext til filters are added.
		if(this.nextFile == null)
			this.nextFile = findNext();
		if(this.nextFile == null)
			throw new java.util.NoSuchElementException("Iterator has finished.");

		File result = this.nextFile;
		try {
			this.nextFile = findNext();
		}
		catch(NoSuchElementException e) {
			this.nextFile = null;
		}
		return result;
	}
	
	@Override
	public void remove() {
		throw new NoSuchMethodError("Not implemented");
	}



	public void addFilter(FileFilter f) {
		this.filters.add(f);
	}
	
	/** Returns next object that is not in exclusion list. */
	public File findNext() {
		File next = null;
		while(true) {
			if(iterators.empty())
				return null;
			else if(iterators.peek().hasNext()) {
				next = iterators.peek().next();
				boolean approved = true;
				for(Iterator<FileFilter> iff=this.filters.iterator(); approved && iff.hasNext(); ) {
					approved = iff.next().accept(next);
				}
				if(approved)
					break;
			}
			else
				// remove all finished iterators
				iterators.pop();
		}
		if(next.isDirectory())
			pushDir(next);
		
		return next;
	}
	
	/** Push children of given dir onto iterator stack. */
	private void pushDir(File dir) {
		List<File> files = new ArrayList<File>();
		List<File> dirs = new ArrayList<File>();
		File[] all = dir.listFiles();
		
		for(int i=0; i<all.length; i++) {
			if(all[i].isDirectory()) {
				dirs.add(all[i]);
			}
			else {
				files.add(all[i]);
			}
		}
		Collections.sort(files);
		Collections.sort(dirs);
		
		iterators.push(dirs.iterator());
		iterators.push(files.iterator());
	}
}



//interface FileFilter {
//	/** @return false to block recursion into this folder. */
//	boolean descend(File dir);
//	/** @return false to block return of this item by iterator. */
//	boolean report(File item);
//}
