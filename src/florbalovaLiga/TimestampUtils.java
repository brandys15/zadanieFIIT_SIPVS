package florbalovaLiga;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TimestampUtils {
	public static byte[] getRequest(byte[] data) throws Exception {
		TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
		TimeStampRequest req = reqGen.generate(TSPAlgorithms.SHA256, data);
		
		return req.getEncoded();
	}
	
	public static TimeStampResponse getResponse(byte[] request, String tsGeneratorUrl) throws Exception {
		URL url = new URL(tsGeneratorUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-type", "application/timestamp-query");
			con.setRequestProperty("Content-length", String.valueOf(request.length));
		
		OutputStream outStream = con.getOutputStream();
			outStream.write(request);
			outStream.flush();
		
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("HTTP Error: " + con.getResponseCode() + " - " + con.getResponseMessage());
		}

		InputStream inStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		
		StringBuilder result = new StringBuilder();
		String line = reader.readLine();
		
        while (line != null) {
        	result.append(line);
        	line = reader.readLine();
        }
        
        return new TimeStampResponse(Base64.getDecoder().decode(result.toString()));
	}
	
	public static void addTsElements(Document doc, String tsToken, Node qualifProps) {
		Element unsignedProps = doc.createElement("xades:UnsignedProperties");
		Element unsignedSigProps = doc.createElement("xades:UnsignedSignatureProperties");
		Element sigTs = doc.createElement("xades:SignatureTimeStamp");
		Element encapsulatedTs = doc.createElement("xades:EncapsulatedTimeStamp");
		
		unsignedProps.appendChild(unsignedSigProps);
		unsignedSigProps.appendChild(sigTs);
		sigTs.appendChild(encapsulatedTs);
		
		Text timestampNode = doc.createTextNode(tsToken);
		encapsulatedTs.appendChild(timestampNode);
		((Element) qualifProps).appendChild(unsignedProps);
	}
}
