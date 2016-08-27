import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import de.schlichtherle.io.File;


public class ZipTest2 {
	public static void main(String[] args) throws IOException {
		File dsrc = new File("/Documents and Settings/Bill/My Documents/Projects/eclipse/BasementSync/src");
		Set<String> srcfiles = FileUtils.listFiles(dsrc);

		File zip = new File("bundle2.zip");
		for(Iterator<String> fi = srcfiles.iterator(); fi.hasNext(); ) {
			String name = fi.next();
			File fsrc = new File(dsrc, name);
			System.out.println("Adding "+name);
			
			File fdst = new File(zip, name);
			if( !fdst.archiveCopyFrom(fsrc) )
				throw new IOException("File.archiveCopyFrom failed");
		}
	}
}
