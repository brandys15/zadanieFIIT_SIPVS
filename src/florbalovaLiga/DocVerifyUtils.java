package florbalovaLiga;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocVerifyUtils {
	private static DocVerifyUtils instance = null;
	private DocumentBuilderFactory docFactory = null;
	private DocumentBuilder docBuilder = null;
	
	public static DocVerifyUtils getInstance() throws Exception {
		if(instance == null)
			instance = new DocVerifyUtils();
		
		return instance;
	}
	private DocVerifyUtils() throws Exception {
		docFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docFactory.newDocumentBuilder();
	}
	
	public String checkDocuments(List<File> docs) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		for(File f: docs) {
			Document doc = docBuilder.parse(f);
			
			sb.append("Overenie s�boru " + f.getName() + ":\n");
			//TO DO - check operations, each one of them can return a report of String, about the situation
			//		  what will be appended to StringBuilder instance
			sb.append("1. Overenie d�tovej ob�lky:" + '\n');
			sb.append(checkDataEnvelope(doc));
			
			sb.append("----------------------------------------------------------------------------------------------\n");
		}
		
		return sb.toString();
	}
	
	private String checkDataEnvelope(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean checker = false; 
		
		Node sigElem = doc.getElementsByTagName("ds:Signature").item(0);
		Node objElem = doc.getElementsByTagName("ds:Object").item(0);
		
		Element root = doc.getDocumentElement();
		String xZep = root.getAttribute("xmlns:xzep");
		String ds = root.getAttribute("xmlns:ds");
		
		//SOURCE: GOV_ZEP.2.5.080911.Profil XAdES_ZEP - format ZEP na baze XAdES
		//COMMENT: Datova obalka musi obsahovat ds:Signature element
		if(sigElem == null) {
			sb.append("   a) CHYBA - Dokument neobsahuje pripojen� elektronick� podpis." + '\n');
			checker = true;
		}
		else sb.append("   a) Dokument obsahuje pripojen� elektronick� podpis." + '\n');
		
		//COMMENT: Datova obalka musi mat objekty v elemente ds:Object
		if(objElem == null) {
			sb.append("   b) CHYBA - Dokument neobsahuje v�etky n�le�itosti zabalen� v objekte ds:Object" + '\n');
			checker = true;
		}
		else sb.append("   b) Dokument obsahuje v�etky n�le�itosti zabalen� v objekte ds:Object" + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:xzep = "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0"
		if(xZep == null || xZep.equals("")) {
			sb.append("   c) CHYBA - Kore�ov� element neobsahuje atrib�t xmlns:xzep." + '\n');
			checker = true;
		}
		else if(!xZep.matches("^http://www\\.ditec\\.sk/ep/signature_formats/xades_zep/v[1-9]\\.[0-9]+$")) { //using regex for different versions
			sb.append("   c) CHYBA - Kore�ov� element neobsahuje atrib�t xmlns:xzep v spr�vnom tvare." + '\n');
			sb.append("      Atrib�t m� hodnotu: " + xZep + "\n");
			checker = true;
		}
		else sb.append("   c) Atrib�t xmlns:xzep kore�ov�ho elementu je v spr�vnom tvare." + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:ds = "http://www.w3.org/2000/09/xmldsig#"
		if(ds == null || ds.equals("0")) {
			sb.append("   d) CHYBA - Kore�ov� element neobsahuje atrib�t xmlns:ds." + '\n');
			checker = true;
		}
		else if(!ds.equals("http://www.w3.org/2000/09/xmldsig#")) {
			sb.append("   d) CHYBA - Kore�ov� element neobsahuje atrib�t xmlns:xzep v spr�vnom tvare." + '\n');
			sb.append("      Atrib�t m� hodnotu: " + ds + ".\n");
			checker = true;
		}
		else sb.append("   d) Atrib�t xmlns:ds kore�ov�ho elementu je v spr�vnom tvare." + '\n');
		
		if(checker)
			sb.append("   ZHRNUTIE - Dan� dokument NESP��A v�etky po�iadavky o�ak�van� predpis d�tovej ob�lky." + '\n');
		else sb.append("   ZHRNUTIE - Dan� dokument SP��A v�etky po�iadavky o�ak�van� predpis d�tovej ob�lky." + '\n');
		
		return sb.toString();
	}
	
	
}
