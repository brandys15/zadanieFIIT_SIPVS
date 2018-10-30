package florbalovaLiga;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;

public class ResourceUtils {
	static public InputStream getResourceAsStream(String name) throws FileNotFoundException {
		File file = new File(name);
		InputStream stream = new FileInputStream(file);
		
		return stream;
	}

	static public String readResource(String name) throws IOException {
		InputStream is = getResourceAsStream(name);
		byte[] data = new byte[is.available()];
		
		is.read(data);
		is.close();
		
		return new String(data, "UTF-8");
	}

	static public String readResourceAsBase64(String name) throws IOException {
		InputStream is = getResourceAsStream(name);
		byte[] data = new byte[is.available()];
		
		is.read(data);
		is.close();
		
		String msg = Base64.encode(data);
		return msg;
	}

	static public void writeFileFromBase64(String filename, String base64) throws IOException {
		String path = new File(System.getProperty("user.home"), filename).getAbsolutePath();
		FileOutputStream is = new FileOutputStream(path);
		
		try {
			is.write(Base64.decode(base64));
		} catch (Base64DecodingException e) {
			throw new RuntimeException(e);
		} finally {
			is.close();
		}
	}
}
