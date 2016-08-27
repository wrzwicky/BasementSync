import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.zip.Deflater;

public class DeflaterTest {

	public static byte[] compresserZlib(byte[] donnees) {
		ByteArrayOutputStream resultat = new ByteArrayOutputStream();
		byte[] buffer = new byte[1000000];
		int nbEcrits;

		Deflater deflater = new Deflater();
		deflater.setInput(donnees);
		deflater.setLevel(9);
		deflater.finish();

		while (!deflater.finished()) {
			nbEcrits = deflater.deflate(buffer);
			resultat.write(buffer, 0, nbEcrits);
		}

		return resultat.toByteArray();
	}

	public static void main(String[] args) {
		Random r = new Random();
		byte[] buffer = new byte[5000000];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) (r.nextInt() % 127);
		}

		for (int i = 0; i < 100; i++) {
			long start = System.currentTimeMillis();
			byte[] result = compresserZlib(buffer);
			long end = System.currentTimeMillis();

			System.out.println("Run took: " + (end - start) + " "
					+ result[Math.abs(buffer[0])]);
		}

	}
}
