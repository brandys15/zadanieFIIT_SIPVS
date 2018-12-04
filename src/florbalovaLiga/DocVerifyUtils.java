package florbalovaLiga;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		docFactory.setNamespaceAware(true);
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
			sb.append("2. Overenie XML Signature:" + '\n');
			sb.append(checkXMLSignature(doc));
			checkSignatureReference(doc);
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
	
	private String checkXMLSignature(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		String content;
		boolean checker = false;
		Node sigMethodElem = doc.getElementsByTagName("ds:SignatureMethod").item(0);
		Node canMethodElem = doc.getElementsByTagName("ds:CanonicalizationMethod").item(0);
		Node sigInfo = doc.getElementsByTagName("ds:SignedInfo").item(0);	
		NodeList nodeListTransform = ((Element) sigInfo).getElementsByTagName("ds:Transform");
		NodeList nodeListDigest = ((Element) sigInfo).getElementsByTagName("ds:DigestMethod");
		for(int x = 0,size= nodeListTransform.getLength(); x<size; x++) {
            content = nodeListTransform.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
            if (!content.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
            	sb.append("Chyba, "+ (x + 1) + ". transform neobsahuje podporovan� algoritmus ale" + content + "\n");
            	checker = true;
            }
        }
		for(int x=0,size= nodeListDigest.getLength(); x<size; x++) {
            content = nodeListDigest.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
        	if (!content.matches("http://www.w3.org/200[01]/0[49]/xml((dsig-more#sha[23][28]4)|(dsig#sha1)|(enc#sha[25][15][26]))")) {
        	sb.append("Chyba, "+ (x + 1) + ". digestMethod neobsahuje podporovan� algoritmus ale" + content + "\n");
        	checker = true;
        	}
		}
		String sigValue = sigMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		String canValue = canMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		if (!sigValue.matches("http://www.w3.org/200[01]/0[49]/xmldsig((-more#rsa-sha[235][158][246])|(#[dr]sa-sha1))")) {
			sb.append("Chyba, SignatureMethod neobsahuje podporovan� algoritmus ale " + sigValue);
			checker = true;
		}
		else if(!canValue.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
			sb.append("Chyba, CanonicalizationMethod neobsahuje podporovan� algoritmus ale " + sigValue);
			checker = true;
		}
		else if (checker) {
			sb.append("   ZHRNUTIE - Dan� dokument NESP��A v�etky po�iadavky o�ak�van� pre overenie XML Signature." + '\n');
		} 
		else sb.append("   ZHRNUTIE - Dan� dokument SP��A v�etky po�iadavky o�ak�van� pre overenie XML Signature." + '\n');
		return sb.toString();
	}
	
	private String checkSignatureReference (Document doc) {
		StringBuilder sb = new StringBuilder();
		String content1, content2;
		Node sigInfo = doc.getElementsByTagName("ds:SignedInfo").item(0);	// get element signedInfo
		NodeList nodeListReferences = ((Element) sigInfo).getElementsByTagName("ds:Reference");	// get list of reference elements
		for(int x=0,size= nodeListReferences.getLength(); x<size; x++) {
			content1 = nodeListReferences.item(x).getAttributes().getNamedItem("URI").getNodeValue().substring(1);	// get from each reference element the URI attribute
			content2 = ((Element) nodeListReferences.item(x)).getElementsByTagName("ds:DigestValue").item(0).getTextContent();  // get from concrete reference the element DigestValue
			System.out.println(content1 + "\n" + content2 + "\n");
		}
		return sb.toString();
	}
}
