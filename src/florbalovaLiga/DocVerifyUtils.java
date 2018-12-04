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
			
			sb.append("3. Core valid�cia:" + '\n');
			checkSignatureReference(doc);
			sb.append(checkOtherXAdESElements(doc));
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
		String content = null;
		boolean checker = false;
		
		Node sigMethodElem = doc.getElementsByTagName("ds:SignatureMethod").item(0);
		Node canMethodElem = doc.getElementsByTagName("ds:CanonicalizationMethod").item(0);
		Node sigInfo = doc.getElementsByTagName("ds:SignedInfo").item(0);	
		
		NodeList nodeListTransform = ((Element) sigInfo).getElementsByTagName("ds:Transform");
		NodeList nodeListDigest = ((Element) sigInfo).getElementsByTagName("ds:DigestMethod");
		
		for(int x = 0, size = nodeListTransform.getLength(); x < size; x++) {
            content = nodeListTransform.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
            if (!content.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
            	sb.append("   CHYBA - " + (x + 1) + ". ds:Transform neobsahuje podporovan� algoritmus." + '\n');
            	sb.append("       Hodnota atrib�tu je: " + content + '\n');
            	checker = true;
            }
            else sb.append("   " + (x + 1) + ". ds:Transform obsahuje podporovan� algoritmus." + '\n');
        }
		for(int x = 0, size = nodeListDigest.getLength(); x < size; x++) {
            content = nodeListDigest.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
        	if (!content.matches("http://www.w3.org/200[01]/0[49]/xml((dsig-more#sha[23][28]4)|(dsig#sha1)|(enc#sha[25][15][26]))")) {
        		sb.append("   CHYBA - " + (x + 1) + ". ds:DigestMethod neobsahuje podporovan� algoritmus." + '\n');
        		sb.append("       Hodnota atrib�tu je: " + content + "\n");
        		checker = true;
        	}
        	else sb.append("   " + (x + 1) + ". ds:DigestMethod obsahuje podporovan� algoritmus." + '\n');
		}
		
		String sigValue = sigMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		String canValue = canMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		if (!sigValue.matches("http://www.w3.org/200[01]/0[49]/xmldsig((-more#rsa-sha[235][158][246])|(#[dr]sa-sha1))")) {
			sb.append("   CHYBA - ds:SignatureMethod neobsahuje podporovan� algoritmus." + '\n');
			sb.append("       Hodnota dan�ho atrib�tu je: " + sigValue + '\n');
			checker = true;
		}
		else sb.append("   ds:SignatureMethod obsahuje spr�vnu hodnotu." + '\n');
		
		if(!canValue.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
			sb.append("   CHYBA - ds:CanonicalizationMethod neobsahuje podporovan� algoritmus." + '\n');
			sb.append("       Hodnota dan�ho atrib�tu je: " + canValue + '\n');
			checker = true;
		}
		else sb.append("   ds:CanonicalizationMethod obsahuje spr�vnu hodnotu." + '\n');
		
		if (checker) {
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
	
	private String checkOtherXAdESElements(Document doc) {
		StringBuilder sb = new StringBuilder();
		
		
		//OVERENIE ds:Signature
		Element sigElem = (Element) doc.getElementsByTagName("ds:Signature").item(0);
		String sigIdAttr = sigElem.getAttribute("Id");
		String sigNamespaceAttr = sigElem.getAttribute("xmlns:ds");
		String xadesIdAttr = ((Element) doc.getElementsByTagName("xades:QualifyingProperties").item(0)).getAttribute("Target");
		//Id atribut
		if(sigIdAttr == null || sigIdAttr.equals(""))
			sb.append("   c) CHYBA - element ds:Signature neobsahuje atrib�t Id." + '\n');
		else if(!sigIdAttr.equals(xadesIdAttr))
			sb.append("   c) CHYBA - hodnota atrib�tu Id patriaci elementu ds:Signature je nespr�vna." + '\n' + "      Jeho hodnota je: " + sigIdAttr + '\n');
		else sb.append("   c) Element ds:Signature obsahuje atrib�t Id." + '\n');
		//xmlns:ds atribut
		if(sigNamespaceAttr == null || sigNamespaceAttr.equals(""))
			sb.append("   d) CHYBA - element ds:Signature nem� �pecifikovan� namespace xmlns:ds." + '\n');
		else if(!sigNamespaceAttr.equals("http://www.w3.org/2000/09/xmldsig#"))
			sb.append("   d) CHYBA - hodnota atrib�tu xmlns:ds patriaci elementu ds:Signature je nespr�vna." + '\n' + "      Jeho hodnota je: " + sigNamespaceAttr + '\n');
		else sb.append("   d) Element ds:Signature obsahuje namespace xmlns:ds." + '\n');
		
		
		//OVERENIE ds:SignatureValue
		String sigValIdAttr = ((Element) doc.getElementsByTagName("ds:SignatureValue").item(0)).getAttribute("Id");
		//String xadesSigValIdAttr = ((Element) doc.getElementsByTagName("xades:SignatureTimeStamp").item(0)).getAttribute("Id"); - nepouzite
		if(sigValIdAttr == null || sigValIdAttr.equals(""))
			sb.append("   e) CHYBA - element ds:SignatureValue neobsahuje atrib�t Id." + '\n');
		else sb.append("   e) Element ds:SignatureValue obsahuje atrib�t Id." + '\n');
		
		
		//OVERENIE ds:SignedInfo
		//NodeList refNodes = doc.getElementsByTagName("ds:Reference");
		NodeList nodes = ((Element) doc.getElementsByTagName("ds:SignedInfo").item(0)).getElementsByTagName("ds:Reference");
		String keyInfoId = ((Element) doc.getElementsByTagName("ds:KeyInfo").item(0)).getAttribute("Id");
		String sigPropsId = ((Element) doc.getElementsByTagName("ds:SignatureProperties").item(0)).getAttribute("Id");
		String xadesSigPropsId = ((Element) doc.getElementsByTagName("xades:SignedProperties").item(0)).getAttribute("Id");
		//String manifestId = ((Element) doc.getElementsByTagName("ds:Manifest").item(0)).getAttribute("Id");
		String refType = null;
		boolean keyInfoCont = false, sigPropsCont = false, signedPropsCont = false, manifestCont = true, manifestIdCheck = true;
		Element keyInfoRef = null, sigPropsRef = null, signedPropsRef = null; //, manifestRef = null;
		
		for(int i = 0; i < nodes.getLength(); i++) {
			Element elem = (Element) nodes.item(i);
			String refId = elem.getAttribute("Id");
			
			if(refId.contains(keyInfoId)) {
				keyInfoCont = true;
				keyInfoRef = elem;
			}
			else if(refId.contains(sigPropsId)) {
				sigPropsCont = true;
				sigPropsRef = elem;
			}
			else if(refId.contains(xadesSigPropsId)) {
				signedPropsCont = true;
				signedPropsRef = elem;
			}
			else if(!refId.contains("ManifestObject")) {
				manifestCont = false;
				if(!elem.getAttribute("Type").equals("http://www.w3.org/2000/09/xmldsig#Manifest"))
					manifestIdCheck = false;
			}
		}
		
		if(keyInfoCont)
			sb.append("   f) ds:SignedInfo obsahuje referenciu na ds:KeyInfo element." + '\n');
		else sb.append("   f) CHYBA - ds:SignedInfo neobsahuje referenciu na ds:KeyInfo element." + '\n');
		if(keyInfoRef != null && keyInfoRef.hasAttribute("Type")) {
			refType = keyInfoRef.getAttribute("Type");
			if(! refType.equals("http://www.w3.org/2000/09/xmldsig#Object"))
				sb.append("   g) CHYBA - atrib�t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo nie je v spr�vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   g) Atrib�t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo je v spr�vnom tvare." + '\n');
		}
		else sb.append("   g) CHYBA - Atrib�t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo ch�ba." + '\n');
		
		if(sigPropsCont)
			sb.append("   h) ds:SignedInfo obsahuje referenciu na ds:SignatureProperties element." + '\n');
		else sb.append("   h) CHYBA - ds:SignedInfo neobsahuje referenciu na ds:SignatureProperties element." + '\n');
		if(sigPropsRef != null && sigPropsRef.hasAttribute("Type")) {
			refType = sigPropsRef.getAttribute("Type");
			if(! refType.equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties"))
				sb.append("   i) CHYBA - atrib�t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo nie je v spr�vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   i) Atrib�t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo je v spr�vnom tvare." + '\n');
		}
		else sb.append("   i) CHYBA - Atrib�t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo ch�ba." + '\n');
		
		if(signedPropsCont)
			sb.append("   j) ds:SignedInfo obsahuje referenciu na xades:SignedProperties element." + '\n');
		else sb.append("   j) CHYBA - ds:SignedInfo neobsahuje referenciu na xades:SignedProperties element." + '\n');
		if(signedPropsRef != null && signedPropsRef.hasAttribute("Type")) {
			refType = signedPropsRef.getAttribute("Type");
			if(! refType.equals("http://uri.etsi.org/01903#SignedProperties"))
				sb.append("   k) CHYBA - atrib�t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo nie je v spr�vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   k) Atrib�t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo je v spr�vnom tvare." + '\n');
		}
		else sb.append("   k) CHYBA - Atrib�t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo ch�ba." + '\n'); 

		if(manifestCont)
			sb.append("   l) ds:SignedInfo obsahuje referencie na ds:Manifest element v spr�vnom tvare." + '\n');
		else sb.append("   l) CHYBA - ds:SignedInfo m� nekonzistentn� referencie na ds:Manifest element." + '\n');
		if(manifestIdCheck)
			sb.append("   m) Atrib�t Type v referenci�ch na ds:Manifest v elementoch ds:SignedInfo s� v spr�vnom tvare." + '\n');
		else sb.append("   m) CHYBA - Atrib�t Type v referenci�ch na ds:Manifest v elementoch ds:SignedInfo nie s� konzistentn�." + '\n');
		//
		
		return sb.toString();
	}
}
