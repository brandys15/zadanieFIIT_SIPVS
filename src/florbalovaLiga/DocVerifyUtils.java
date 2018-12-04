package florbalovaLiga;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
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
			
			sb.append("Overenie súboru " + f.getName() + ":\n");
			//TO DO - check operations, each one of them can return a report of String, about the situation
			//		  what will be appended to StringBuilder instance
			sb.append("1. Overenie dátovej obálky:" + '\n');
			sb.append(checkDataEnvelope(doc));
			
			sb.append("2. Overenie XML Signature:" + '\n');
			sb.append(checkXMLSignature(doc));
			
			sb.append("3. Core validácia:" + '\n');
			sb.append(checkSignatureReference(doc));
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
			sb.append("   a) CHYBA - Dokument neobsahuje pripojený elektronický podpis." + '\n');
			checker = true;
		}
		else sb.append("   a) Dokument obsahuje pripojený elektronický podpis." + '\n');
		
		//COMMENT: Datova obalka musi mat objekty v elemente ds:Object
		if(objElem == null) {
			sb.append("   b) CHYBA - Dokument neobsahuje všetky náležitosti zabalené v objekte ds:Object" + '\n');
			checker = true;
		}
		else sb.append("   b) Dokument obsahuje všetky náležitosti zabalené v objekte ds:Object" + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:xzep = "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0"
		if(xZep == null || xZep.equals("")) {
			sb.append("   c) CHYBA - Koreòový element neobsahuje atribút xmlns:xzep." + '\n');
			checker = true;
		}
		else if(!xZep.matches("^http://www\\.ditec\\.sk/ep/signature_formats/xades_zep/v[1-9]\\.[0-9]+$")) { //using regex for different versions
			sb.append("   c) CHYBA - Koreòový element neobsahuje atribút xmlns:xzep v správnom tvare." + '\n');
			sb.append("      Atribút má hodnotu: " + xZep + "\n");
			checker = true;
		}
		else sb.append("   c) Atribút xmlns:xzep koreòového elementu je v správnom tvare." + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:ds = "http://www.w3.org/2000/09/xmldsig#"
		if(ds == null || ds.equals("0")) {
			sb.append("   d) CHYBA - Koreòový element neobsahuje atribút xmlns:ds." + '\n');
			checker = true;
		}
		else if(!ds.equals("http://www.w3.org/2000/09/xmldsig#")) {
			sb.append("   d) CHYBA - Koreòový element neobsahuje atribút xmlns:xzep v správnom tvare." + '\n');
			sb.append("      Atribút má hodnotu: " + ds + ".\n");
			checker = true;
		}
		else sb.append("   d) Atribút xmlns:ds koreòového elementu je v správnom tvare." + '\n');
		
		if(checker)
			sb.append("   ZHRNUTIE - Daný dokument NESPÅÒA všetky požiadavky oèakávaný predpis dátovej obálky." + '\n');
		else sb.append("   ZHRNUTIE - Daný dokument SPÅÒA všetky požiadavky oèakávaný predpis dátovej obálky." + '\n');
		
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
            	sb.append("   CHYBA - " + (x + 1) + ". ds:Transform neobsahuje podporovaný algoritmus." + '\n');
            	sb.append("       Hodnota atribútu je: " + content + '\n');
            	checker = true;
            }
            else sb.append("   " + (x + 1) + ". ds:Transform obsahuje podporovaný algoritmus." + '\n');
        }
		for(int x = 0, size = nodeListDigest.getLength(); x < size; x++) {
            content = nodeListDigest.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
        	if (!content.matches("http://www.w3.org/200[01]/0[49]/xml((dsig-more#sha[23][28]4)|(dsig#sha1)|(enc#sha[25][15][26]))")) {
        		sb.append("   CHYBA - " + (x + 1) + ". ds:DigestMethod neobsahuje podporovaný algoritmus." + '\n');
        		sb.append("       Hodnota atribútu je: " + content + "\n");
        		checker = true;
        	}
        	else sb.append("   " + (x + 1) + ". ds:DigestMethod obsahuje podporovaný algoritmus." + '\n');
		}
		
		String sigValue = sigMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		String canValue = canMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		if (!sigValue.matches("http://www.w3.org/200[01]/0[49]/xmldsig((-more#rsa-sha[235][158][246])|(#[dr]sa-sha1))")) {
			sb.append("   CHYBA - ds:SignatureMethod neobsahuje podporovaný algoritmus." + '\n');
			sb.append("       Hodnota daného atribútu je: " + sigValue + '\n');
			checker = true;
		}
		else sb.append("   ds:SignatureMethod obsahuje správnu hodnotu." + '\n');
		
		if(!canValue.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
			sb.append("   CHYBA - ds:CanonicalizationMethod neobsahuje podporovaný algoritmus." + '\n');
			sb.append("       Hodnota daného atribútu je: " + canValue + '\n');
			checker = true;
		}
		else sb.append("   ds:CanonicalizationMethod obsahuje správnu hodnotu." + '\n');
		
		if (checker) {
			sb.append("   ZHRNUTIE - Daný dokument NESPÅÒA všetky požiadavky oèakávaný pre overenie XML Signature." + '\n');
		} 
		else sb.append("   ZHRNUTIE - Daný dokument SPÅÒA všetky požiadavky oèakávaný pre overenie XML Signature." + '\n');
		return sb.toString();
	}
	
	private String checkSignatureReference (Document doc) throws TransformerException, InvalidCanonicalizerException, CanonicalizationException, ParserConfigurationException, IOException, NoSuchAlgorithmException, org.xml.sax.SAXException {
		StringBuilder sb = new StringBuilder();
		String canonMethod = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
		String URI, digestValue, calculatedDigestValue;
		Element referenceElem = null;
		byte[] manifestBytes = null; 
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		Node sigInfo = doc.getElementsByTagName("ds:SignedInfo").item(0);	// get element signedInfo
		NodeList nodeListReferences = ((Element) sigInfo).getElementsByTagName("ds:Reference");	// get list of reference elements
		NodeList nodeListManifest =  doc.getElementsByTagName("ds:Manifest");
		for(int x=0,size= nodeListReferences.getLength(); x<size; x++) {
			URI = nodeListReferences.item(x).getAttributes().getNamedItem("URI").getNodeValue().substring(1);	// get from each reference element the URI attribute

	        for (int i = 0; i <nodeListManifest.getLength(); i++) {
	        	Element reference = (Element) nodeListManifest.item(i); // get "ds:Manifest" elements
	        	if (reference.getAttribute("Id").equals(URI)) {
	        		referenceElem = reference;
	        	} else referenceElem = null;
	        }
	        
        	if(referenceElem != null) {
        		Init.init(); // init xml security
        		Canonicalizer canonicalizer = Canonicalizer.getInstance(canonMethod);
        		manifestBytes = canonicalizer.canonicalize(ResourceUtils.elementToBytes(referenceElem));
    			digestValue = ((Element) nodeListReferences.item(x)).getElementsByTagName("ds:DigestValue").item(0).getTextContent();  // get from concrete reference the element DigestValue
    			calculatedDigestValue = new String(Base64.getEncoder().encode(messageDigest.digest(manifestBytes)));
    			if (digestValue.equals(calculatedDigestValue)) {
    				sb.append("   a) Hodnota odtlaèku ds:DigestValue sedí.\n");
    			} else sb.append("   a) CHYBA hodnota odtlaèku ds:DigestValue nesedí\n");
        	}		
		}
		return sb.toString();
	}
	
	private String checkDsSignature(Document doc) {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:Signature
		Element sigElem = (Element) doc.getElementsByTagName("ds:Signature").item(0);
		String sigIdAttr = sigElem.getAttribute("Id");
		String sigNamespaceAttr = sigElem.getAttribute("xmlns:ds");
		String xadesIdAttr = ((Element) doc.getElementsByTagName("xades:QualifyingProperties").item(0)).getAttribute("Target");
		
		//Id atribut
		if(sigIdAttr == null || sigIdAttr.equals(""))
			sb.append("   c) CHYBA - element ds:Signature neobsahuje atribút Id." + '\n');
		else if(!xadesIdAttr.contains(sigIdAttr))
			sb.append("   c) CHYBA - hodnota atribútu Id patriaci elementu ds:Signature je nesprávna." + '\n' + "      Jeho hodnota je: " + sigIdAttr + '\n');
		else sb.append("   c) Element ds:Signature obsahuje atribút Id." + '\n');
		
		//xmlns:ds atribut
		if(sigNamespaceAttr == null || sigNamespaceAttr.equals(""))
			sb.append("   d) CHYBA - element ds:Signature nemá špecifikovaný namespace xmlns:ds." + '\n');
		else if(!sigNamespaceAttr.equals("http://www.w3.org/2000/09/xmldsig#"))
			sb.append("   d) CHYBA - hodnota atribútu xmlns:ds patriaci elementu ds:Signature je nesprávna." + '\n' + "      Jeho hodnota je: " + sigNamespaceAttr + '\n');
		else sb.append("   d) Element ds:Signature obsahuje namespace xmlns:ds." + '\n');
		
		return sb.toString();
	}
	private String checkDsSignatureValue(Document doc) {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:SignatureValue
		String sigValIdAttr = ((Element) doc.getElementsByTagName("ds:SignatureValue").item(0)).getAttribute("Id");
		String xadesSigValIdAttr = ((Element) doc.getElementsByTagName("xades:SignatureTimeStamp").item(0)).getAttribute("Id");
		String el = xadesSigValIdAttr.split("Signature")[1];
		
		if(sigValIdAttr == null || sigValIdAttr.equals(""))
			sb.append("   e) CHYBA - element ds:SignatureValue neobsahuje atribút Id." + '\n');
		else if(!sigValIdAttr.contains(el))
			sb.append("   e) CHYBA - hodnota atribútu Id z elementu ds:SignatureValue sa nezhoduje s hodnotou v xades:SignatureTimeStamp." + '\n' + "      Jeho hodnota je: " + xadesSigValIdAttr + '\n');
		else sb.append("   e) Element ds:SignatureValue obsahuje atribút Id." + '\n');
		
		return sb.toString();
	}
	private String checkDsSignedInfo(Document doc) {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:SignedInfo
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
				sb.append("   g) CHYBA - atribút Type v referencii na ds:KeyInfo v elemente ds:SignedInfo nie je v správnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   g) Atribút Type v referencii na ds:KeyInfo v elemente ds:SignedInfo je v správnom tvare." + '\n');
		}
		else sb.append("   g) CHYBA - Atribút Type v referencii na ds:KeyInfo v elemente ds:SignedInfo chýba." + '\n');
		
		if(sigPropsCont)
			sb.append("   h) ds:SignedInfo obsahuje referenciu na ds:SignatureProperties element." + '\n');
		else sb.append("   h) CHYBA - ds:SignedInfo neobsahuje referenciu na ds:SignatureProperties element." + '\n');
		if(sigPropsRef != null && sigPropsRef.hasAttribute("Type")) {
			refType = sigPropsRef.getAttribute("Type");
			if(! refType.equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties"))
				sb.append("   i) CHYBA - atribút Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo nie je v správnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   i) Atribút Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo je v správnom tvare." + '\n');
		}
		else sb.append("   i) CHYBA - Atribút Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo chýba." + '\n');
		
		if(signedPropsCont)
			sb.append("   j) ds:SignedInfo obsahuje referenciu na xades:SignedProperties element." + '\n');
		else sb.append("   j) CHYBA - ds:SignedInfo neobsahuje referenciu na xades:SignedProperties element." + '\n');
		if(signedPropsRef != null && signedPropsRef.hasAttribute("Type")) {
			refType = signedPropsRef.getAttribute("Type");
			if(! refType.equals("http://uri.etsi.org/01903#SignedProperties"))
				sb.append("   k) CHYBA - atribút Type v referencii na xades:SignedProperties v elemente ds:SignedInfo nie je v správnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   k) Atribút Type v referencii na xades:SignedProperties v elemente ds:SignedInfo je v správnom tvare." + '\n');
		}
		else sb.append("   k) CHYBA - Atribút Type v referencii na xades:SignedProperties v elemente ds:SignedInfo chýba." + '\n'); 

		if(manifestCont)
			sb.append("   l) ds:SignedInfo obsahuje referencie na ds:Manifest element v správnom tvare." + '\n');
		else sb.append("   l) CHYBA - ds:SignedInfo má nekonzistentné referencie na ds:Manifest element." + '\n');
		if(manifestIdCheck)
			sb.append("   m) Atribút Type v referenciách na ds:Manifest v elementoch ds:SignedInfo sú v správnom tvare." + '\n');
		else sb.append("   m) CHYBA - Atribút Type v referenciách na ds:Manifest v elementoch ds:SignedInfo nie sú konzistentné." + '\n');
		
		return sb.toString();
	}
	private String checkDsKeyInfo(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:KeyInfo
		Element keyInfo = (Element) doc.getElementsByTagName("ds:KeyInfo").item(0);
		String keyInfoId = keyInfo.getAttribute("Id");
		
		if(keyInfoId == null || keyInfoId.equals(""))
			sb.append("   n) CHYBA - ds:KeyInfo neobsahuje atribút Id." + '\n');
		else sb.append("   n) ds:keyInfo obsahuje atribút Id." + '\n');
		
		Element x509Data = ((Element)keyInfo.getElementsByTagName("ds:X509Data").item(0));
		
		if(x509Data == null) {
			sb.append("   o) CHYBA - ds:keyInfo neobsahuje element ds:509Data." + '\n');
			return sb.toString();
		}
		sb.append("   o) ds:keyInfo obsahuje element ds:509Data." + '\n');
		
		Element x509Certificate = (Element)x509Data.getElementsByTagName("ds:X509Certificate").item(0);
		Element x509Issuer = (Element)x509Data.getElementsByTagName("ds:X509IssuerSerial").item(0);
		Element x509SubjName = (Element)x509Data.getElementsByTagName("ds:X509SubjectName").item(0);
		boolean checker = false;
		
		if(x509Issuer == null) {
			sb.append("   p) CHYBA - ds:KeyInfo neobsahuje element ds:X509IssuerSerial." + '\n');
			checker = true;
		}
		else sb.append("   p) ds:KeyInfo obsahuje element ds:X509IssuerSerial." + '\n');
			
		if(x509SubjName == null) {
			sb.append("   q) CHYBA - ds:KeyInfo neobsahuje element ds:X509SubjectName." + '\n');
			checker = true;
		}
		else sb.append("   q) ds:KeyInfo obsahuje element ds:X509SubjectName." + '\n');
		
		if(x509Certificate == null) {
			sb.append("   r) CHYBA - ds:KeyInfo neobsahuje element ds:X509Certificate." + '\n');
			checker = true;
		}
		else sb.append("   r) ds:KeyInfo obsahuje element ds:X509Certificate." + '\n');
		
		if(checker) {
			sb.append("   s) Chýbajúci element (alebo elementy) z radu X509. Nedá sa overi ich hodnota vzh¾adom na certifikát." + '\n');
			return sb.toString();
		}
		
		byte encodedByteCert[] = Base64.getDecoder().decode(x509Certificate.getTextContent().getBytes());
		ByteArrayInputStream inStream = new ByteArrayInputStream(encodedByteCert);
		
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inStream);
		
		String certSubjectName = cert.getSubjectDN().getName();
		String certSerialNum = cert.getSerialNumber().toString();
		String certIssuerName = cert.getIssuerX500Principal().getName();
		
		String docSubjectName = x509SubjName.getTextContent();
		String docSerialNum = ((Element)x509Issuer.getElementsByTagName("ds:X509SerialNumber").item(0)).getTextContent();
		String docIssuerName = ((Element)x509Issuer.getElementsByTagName("ds:X509IssuerName").item(0)).getTextContent().replaceAll(", ", ",");
		
		if(certSubjectName.equals(docSubjectName))
			sb.append("   s) - objekt ds:X509SubjectName sa zhoduje s hodnotou v certifikáte." + '\n');
		else sb.append("   s) - CHYBA - objekt ds:X509SubjectName sa zhoduje s hodnotou v certifikáte." + '\n' + "              - jeho hodnota je " + docSubjectName + '\n');
		
		if(certSerialNum.equals(docSerialNum))
			sb.append("      - objekt ds:X509SerialNumber sa zhoduje s hodnotou v certifikáte." + '\n');
		else sb.append("      - CHYBA - objekt ds:X509SerialNumber sa nezhoduje s hodnotou v certifikáte." + '\n' + "              - jeho hodnota je " + docSerialNum + '\n');
		
		if(certIssuerName.equals(docIssuerName))
			sb.append("      - objekt ds:X509IssuerName sa zhoduje s hodnotou v certifikáte." + '\n');
		else sb.append("      - CHYBA - objekt ds:X509IssuerName sa nezhoduje s hodnotou v certifikáte." + '\n' + "              - jeho hodnota je " + docIssuerName + '\n');
		
		return sb.toString();
	}
	private String checkDsSignatureProperties(Document doc) {
		StringBuilder sb = new StringBuilder();
		Element sigProps = (Element) doc.getElementsByTagName("ds:SignatureProperties").item(0);
		
		String sigPropsId = sigProps.getAttribute("Id");
		if(sigPropsId == null || sigPropsId.equals(""))
			sb.append("   t) CHYBA - element ds:SignatureProperties neobsahuje atribút Id." + '\n');
		else sb.append("   t) Element ds:SignatureProperties obsahuje atribút Id." + '\n'); 
		
		boolean xzepSigVersion = false, xzepProductInfo = false;
		NodeList sigPropertyList = sigProps.getElementsByTagName("ds:SignatureProperty");
		Element xzepSig = null, xzepProduct = null;
		
		for(int i = 0; i < sigPropertyList.getLength(); i++) {
			Element elem = (Element)sigPropertyList.item(i);
			NodeList nextElem = elem.getElementsByTagName("xzep:SignatureVersion");
			
			if(nextElem.getLength() == 0) {
				nextElem = elem.getElementsByTagName("xzep:ProductInfos");
				if(nextElem.getLength() != 0) {
					xzepProductInfo = true;
					xzepProduct = elem;
				}
			}
			else {
				xzepSigVersion = true;
				xzepSig = elem;
			}
		}
		
		if(xzepSigVersion) {
			sb.append("   u) - ds:SignatureProperties obsahuje element ds:SignatureProperty s elementom xzep:SignatureVersion." + '\n');
			
			String xzepId = xzepSig.getAttribute("Target");
			if(xzepId == null || xzepId.equals(""))
				sb.append("      - CHYBA - element ds:SignatureProperty neobsahuje atribút Id." + '\n');
			else sb.append("      - Element ds:SignatureProperty obsahuje atribút Id." + '\n'); 
		}
		else sb.append("   u) CHYBA - ds:SignatureProperties neobsahuje element ds:SignatureProperty s elementom xzep:SignatureVersion." + '\n');
		
		if(xzepProductInfo) {
			sb.append("   v) - ds:SignatureProperties obsahuje element ds:SignatureProperty s elementom xzep:ProductInfos." + '\n');
			
			String xzepId = xzepProduct.getAttribute("Target");
			if(xzepId == null || xzepId.equals(""))
				sb.append("      - CHYBA - element ds:SignatureProperty neobsahuje atribút Id." + '\n');
			else sb.append("      - Element ds:SignatureProperty obsahuje atribút Id." + '\n'); 
		}
		else sb.append("   v) CHYBA - ds:SignatureProperties neobsahuje element ds:SignatureProperty s elementom xzep:ProductInfos." + '\n');
		
		return sb.toString();
	}
	private String checkDsManifest(Document doc) {
		StringBuilder sb = new StringBuilder();
		NodeList manifests = doc.getElementsByTagName("ds:Manifest");
		
		sb.append("   w) Kontrola ds:Manifest elementov." + '\n');
		for(int i = 0; i < manifests.getLength(); i++) {
			String canonAlgs[] = { "http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "http://www.w3.org/2000/09/xmldsig#base64"};
			String cryptoAlgs[] = 
				{ "http://www.w3.org/2000/09/xmldsig#dsa-sha1",
				  "http://www.w3.org/2000/09/xmldsig#rsa-sha1",
				  "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
				  "http://www.w3.org/2001/04/xmldsig-more#rsa-sha384",
				  "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512",
				  "http://www.w3.org/2000/09/xmldsig#sha1",
				  "http://www.w3.org/2001/04/xmldsig-more#sha224",
				  "http://www.w3.org/2001/04/xmlenc#sha256",
				  "http://www.w3.org/2001/04/xmldsig-more#sha384",
				  "http://www.w3.org/2001/04/xmlenc#sha512" };
			ArrayList<String> canonList = new ArrayList<String>(Arrays.asList(canonAlgs));
			ArrayList<String> cryptoList = new ArrayList<String>(Arrays.asList(cryptoAlgs));
			
			Element elem = (Element) manifests.item(i);
			String id = elem.getAttribute("Id");
			
			if(id == null || id.equals(""))
				sb.append("      " + (i + 1) + ". manifest - CHYBA - daný manifest neobsahuje atribút Id." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - obsahuje atribút Id." + '\n');
			
			NodeList references = elem.getElementsByTagName("ds:Reference");
			if(references.getLength() > 1) {
				int counter = 0;
				for(int j = 0; j < references.getLength(); j++) {
					if(((Element)references.item(j)).getAttribute("URI").contains("Object"))
						counter++;
				}
				
				if(counter != 1)
					sb.append("      " + (i + 1) + ". manifest - CHYBA - element obsahuje nesprávny poèet referencií (" + counter + ") na ds:Object" + '\n');
				sb.append("      " + (i + 1) + ". manifest obsahuje správny poèet referencií na ds:Object." + '\n');
			}
			else sb.append("      " + (i + 1) + ". manifest obsahuje správny poèet referencií na ds:Object." + '\n');
			
			Element tempElem = ((Element)((Element)elem.getElementsByTagName("ds:Reference").item(0)).getElementsByTagName("ds:Transforms").item(0));
			String transAlg = ((Element)tempElem.getElementsByTagName("ds:Transform").item(0)).getAttribute("Algorithm");
			
			tempElem = ((Element)((Element)elem.getElementsByTagName("ds:Reference").item(0)).getElementsByTagName("ds:DigestMethod").item(0));
			String digAlg = tempElem.getAttribute("Algorithm");
			
			if(canonList.contains(transAlg))
				sb.append("      " + (i + 1) + ". manifest obsahuje pre ds:Transforms podporovaný algoritmus transformácie." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - CHYBA - neobsahuje pre ds:Transforms podporovaný algoritmus transformácie." + '\n');
			
			if(cryptoList.contains(digAlg))
				sb.append("      " + (i + 1) + ". manifest obsahuje pre ds:DigestMethod podporovaný algoritmus šifrovania." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - CHYBA - neobsahuje pre ds:DigestMethod podporovaný algoritmus šifrovania." + '\n');
		}
		
		return sb.toString();
	}
	private String checkDsManifestReferences(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		NodeList manifests = doc.getElementsByTagName("ds:Manifest");
		
		sb.append("   x) Kontrola referencií ds:Manifest elementov." + '\n');
		for(int i = 0; i < manifests.getLength(); i++) {
			//sb.append("      " + (i + 1) + ". manifest - ." + '\n');
			Element manElem = (Element) manifests.item(i);
			Element refElem = (Element) manElem.getElementsByTagName("ds:Reference").item(0);
			Element digElem = (Element)refElem.getElementsByTagName("ds:DigestValue").item(0);
			//String digValue = digElem.getTextContent();
			
			Element transElem = (Element) refElem.getElementsByTagName("ds:Transforms").item(0);
			Element tranElem = (Element) transElem.getElementsByTagName("ds:Transform").item(0);
			String transAlg = tranElem.getAttribute("Algorithm");
			
			Init.init();
			Canonicalizer canonicalizer = Canonicalizer.getInstance(transAlg);
    		MessageDigest digest = MessageDigest.getInstance("SHA-256");
    		
    		byte[] manifestBytes = canonicalizer.canonicalize(ResourceUtils.elementToBytes(digElem));
    		String calculatedDigestValue = new String(Base64.getEncoder().encode(digest.digest(manifestBytes)));
    		
    		System.out.println(calculatedDigestValue);
		}
		
		return sb.toString();
	}
	
	private String checkOtherXAdESElements(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append(checkDsSignature(doc));
		sb.append(checkDsSignatureValue(doc));
		sb.append(checkDsSignedInfo(doc));
		sb.append(checkDsKeyInfo(doc));
		sb.append(checkDsSignatureProperties(doc));
		sb.append(checkDsManifest(doc));
		sb.append(checkDsManifestReferences(doc));
		
		return sb.toString();
	}
}
