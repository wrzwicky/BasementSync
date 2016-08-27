import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipTest {
	public static void main(String[] args) throws IOException {
		byte buffer[] = new byte[1000000];
		
		File dsrc = new File("/Documents and Settings/Bill/My Documents/Projects/eclipse/BasementSync/src");
		Set<String> srcfiles = FileUtils.listFiles(dsrc);

		FileOutputStream fos = new FileOutputStream("bundle.zip");
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
//		zos.setMethod(method)
//		zos.setLevel(level)
//		zos.setComment(comment)

		for(Iterator<String> fi = srcfiles.iterator(); fi.hasNext(); ) {
			String name = fi.next();
			File fsrc = new File(dsrc, name);
			System.out.print("Adding "+name+": ");
			
			ZipEntry entry = new ZipEntry(fsrc.getCanonicalPath());
			zos.putNextEntry(entry);

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fsrc));
			int count;
			while((count = bis.read(buffer, 0, buffer.length)) != -1) {
				zos.write(buffer, 0, count);
			}
			bis.close();
			zos.closeEntry();
			System.out.println(entry.getSize() + " => " + entry.getCompressedSize());
		}
		zos.close();
	}
}
