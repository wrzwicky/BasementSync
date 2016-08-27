import java.io.File;
import java.util.*;

// Need to explicitly search one item ahead, as iterators have no peek function.
public class FileIterator implements Iterator<File> {
	File root;
	Set<File> excludes;
	private Stack<Iterator<File>> iterators;
	private File nextFile;
	
	
	public FileIterator(File root, Set<File> excludes) {
		this.root = root;
		this.excludes = excludes;

		this.iterators = new Stack<Iterator<File>>();
		pushDir(root);
		try {
			nextFile = findNext();
		}
		catch(NoSuchElementException e) {
			nextFile = null;
		}
	}
	
	@Override
	public boolean hasNext() {
		return nextFile != null;
	}

	@Override
	public File next() {
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



	/** Returns next object that is not in exclusion list. */
	public File findNext() {
		File next = null;
		while(true) {
			if(iterators.empty())
				return null;
			else if(iterators.peek().hasNext()) {
				next = iterators.peek().next();
				if(!FileUtils.contains(this.excludes, next))
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
