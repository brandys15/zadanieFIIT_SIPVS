package florbalovaLiga;

import java.io.File;
import java.util.List;

public class DocVerifyUtils {
	private static DocVerifyUtils instance = null;
	
	public static DocVerifyUtils getInstance() {
		if(instance == null)
			instance = new DocVerifyUtils();
		
		return instance;
	}
	private DocVerifyUtils() {}
	
	public String checkDocuments(List<File> docs) {
		StringBuilder sb = new StringBuilder();
		
		for(File f: docs) {
			sb.append("Overenie súboru " + f.getName() + '\n');
			//TO DO - check operations, each one of them can return a report of String, about the situation
			//		  what will be appended to StringBuilder instance
			
			sb.append("-------------------------------------------------------------\n");
		}
		
		return sb.toString();
	}
}
