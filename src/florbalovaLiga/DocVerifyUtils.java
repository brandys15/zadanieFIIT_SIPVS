package florbalovaLiga;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathException;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.omg.CORBA.portable.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;

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
			
			sb.append("Overenie s˙boru " + f.getName() + ":\n");
			//TO DO - check operations, each one of them can return a report of String, about the situation
			//		  what will be appended to StringBuilder instance
			sb.append("1. Overenie d·tovej ob·lky:" + '\n');
			sb.append(checkDataEnvelope(doc));
			
			sb.append("2. Overenie XML Signature:" + '\n');
			sb.append(checkXMLSignature(doc));
			
			sb.append("3. Core valid·cia:" + '\n');
			sb.append(checkSignatureReference(doc));
			sb.append(checkSignatureValue(doc));
			sb.append(checkOtherXAdESElements(doc));
			
			sb.append("4. TimeStamp valid·cia:" + '\n');
			sb.append(checkTimeStamp(doc));
			
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
			sb.append("   a) CHYBA - Dokument neobsahuje pripojen˝ elektronick˝ podpis." + '\n');
			checker = true;
		}
		else sb.append("   a) Dokument obsahuje pripojen˝ elektronick˝ podpis." + '\n');
		
		//COMMENT: Datova obalka musi mat objekty v elemente ds:Object
		if(objElem == null) {
			sb.append("   b) CHYBA - Dokument neobsahuje vöetky n·leûitosti zabalenÈ v objekte ds:Object" + '\n');
			checker = true;
		}
		else sb.append("   b) Dokument obsahuje vöetky n·leûitosti zabalenÈ v objekte ds:Object" + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:xzep = "http://www.ditec.sk/ep/signature_formats/xades_zep/v1.0"
		if(xZep == null || xZep.equals("")) {
			sb.append("   c) CHYBA - KoreÚov˝ element neobsahuje atrib˙t xmlns:xzep." + '\n');
			checker = true;
		}
		else if(!xZep.matches("^http://www\\.ditec\\.sk/ep/signature_formats/xades_zep/v[1-9]\\.[0-9]+$")) { //using regex for different versions
			sb.append("   c) CHYBA - KoreÚov˝ element neobsahuje atrib˙t xmlns:xzep v spr·vnom tvare." + '\n');
			sb.append("      Atrib˙t m· hodnotu: " + xZep + "\n");
			checker = true;
		}
		else sb.append("   c) Atrib˙t xmlns:xzep koreÚovÈho elementu je v spr·vnom tvare." + '\n');
		
		//COMMENT: Korenovy element musi obsahovat atribut xmlns:ds = "http://www.w3.org/2000/09/xmldsig#"
		if(ds == null || ds.equals("0")) {
			sb.append("   d) CHYBA - KoreÚov˝ element neobsahuje atrib˙t xmlns:ds." + '\n');
			checker = true;
		}
		else if(!ds.equals("http://www.w3.org/2000/09/xmldsig#")) {
			sb.append("   d) CHYBA - KoreÚov˝ element neobsahuje atrib˙t xmlns:xzep v spr·vnom tvare." + '\n');
			sb.append("      Atrib˙t m· hodnotu: " + ds + ".\n");
			checker = true;
		}
		else sb.append("   d) Atrib˙t xmlns:ds koreÚovÈho elementu je v spr·vnom tvare." + '\n');
		
		if(checker)
			sb.append("   ZHRNUTIE - Dan˝ dokument NESP≈“A vöetky poûiadavky oËak·van˝ predpis d·tovej ob·lky." + '\n');
		else sb.append("   ZHRNUTIE - Dan˝ dokument SP≈“A vöetky poûiadavky oËak·van˝ predpis d·tovej ob·lky." + '\n');
		
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
            	sb.append("   CHYBA - " + (x + 1) + ". ds:Transform neobsahuje podporovan˝ algoritmus." + '\n');
            	sb.append("       Hodnota atrib˙tu je: " + content + '\n');
            	checker = true;
            }
            else sb.append("   " + (x + 1) + ". ds:Transform obsahuje podporovan˝ algoritmus." + '\n');
        }
		for(int x = 0, size = nodeListDigest.getLength(); x < size; x++) {
            content = nodeListDigest.item(x).getAttributes().getNamedItem("Algorithm").getNodeValue();
        	if (!content.matches("http://www.w3.org/200[01]/0[49]/xml((dsig-more#sha[23][28]4)|(dsig#sha1)|(enc#sha[25][15][26]))")) {
        		sb.append("   CHYBA - " + (x + 1) + ". ds:DigestMethod neobsahuje podporovan˝ algoritmus." + '\n');
        		sb.append("       Hodnota atrib˙tu je: " + content + "\n");
        		checker = true;
        	}
        	else sb.append("   " + (x + 1) + ". ds:DigestMethod obsahuje podporovan˝ algoritmus." + '\n');
		}
		
		String sigValue = sigMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		String canValue = canMethodElem.getAttributes().getNamedItem("Algorithm").getNodeValue();
		if (!sigValue.matches("http://www.w3.org/200[01]/0[49]/xmldsig((-more#rsa-sha[235][158][246])|(#[dr]sa-sha1))")) {
			sb.append("   CHYBA - ds:SignatureMethod neobsahuje podporovan˝ algoritmus." + '\n');
			sb.append("       Hodnota danÈho atrib˙tu je: " + sigValue + '\n');
			checker = true;
		}
		else sb.append("   ds:SignatureMethod obsahuje spr·vnu hodnotu." + '\n');
		
		if(!canValue.matches("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
			sb.append("   CHYBA - ds:CanonicalizationMethod neobsahuje podporovan˝ algoritmus." + '\n');
			sb.append("       Hodnota danÈho atrib˙tu je: " + canValue + '\n');
			checker = true;
		}
		else sb.append("   ds:CanonicalizationMethod obsahuje spr·vnu hodnotu." + '\n');
		
		if (checker) {
			sb.append("   ZHRNUTIE - Dan˝ dokument NESP≈“A vöetky poûiadavky oËak·van˝ pre overenie XML Signature." + '\n');
		} 
		else sb.append("   ZHRNUTIE - Dan˝ dokument SP≈“A vöetky poûiadavky oËak·van˝ pre overenie XML Signature." + '\n');
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
    				sb.append("   a) Hodnota odtlaËku ds:DigestValue sedÌ.\n");
    			} else sb.append("   a) CHYBA hodnota odtlaËku ds:DigestValue nesedÌ\n");
        	}		
		}
		return sb.toString();
	}
	
	private String checkSignatureValue(Document doc) throws InvalidCanonicalizerException, CanonicalizationException, TransformerConfigurationException, ParserConfigurationException, IOException, org.xml.sax.SAXException, CertificateParsingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		StringBuilder sb = new StringBuilder();
		byte[] sigInfoBytes = null; 
		Signature sign = null;
		X509CertificateObject certObj = null;
		ASN1InputStream inputStream = null;
		Element sigInfo = (Element) doc.getElementsByTagName("ds:SignedInfo").item(0);
		String sigValue = doc.getElementsByTagName("ds:SignatureValue").item(0).getTextContent();
		String canonMethod = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
		String x509Certificate = doc.getElementsByTagName("ds:X509Certificate").item(0).getTextContent();
		
		Canonicalizer canonicalizer = Canonicalizer.getInstance(canonMethod);
		sigInfoBytes = canonicalizer.canonicalize(ResourceUtils.elementToBytes(sigInfo));
		
		inputStream = new ASN1InputStream(new ByteArrayInputStream(Base64.getDecoder().decode(x509Certificate.getBytes())));
		ASN1Primitive primitive = inputStream.readObject();
		ASN1Sequence sequence = ASN1Sequence.getInstance(primitive);
		certObj = new X509CertificateObject(Certificate.getInstance(sequence));
		
		Security.addProvider(new BouncyCastleProvider());
		sign = Signature.getInstance("SHA256withRSA");
		sign.initVerify(certObj.getPublicKey());
		sign.update(sigInfoBytes);
		if (!sign.verify(Base64.getDecoder().decode(sigValue.getBytes()))) {
			sb.append("   b) CHYBA overenie hodnoty ds:SignatureValue zlyhalo.\n");
		} else {
			sb.append("   b) Overenie ds:SignatureValue bolo ˙speönÈ.\n");
		}
		inputStream.close();
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
	private String checkDsSignature(Document doc) {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:Signature
		Element sigElem = (Element) doc.getElementsByTagName("ds:Signature").item(0);
		String sigIdAttr = sigElem.getAttribute("Id");
		String sigNamespaceAttr = sigElem.getAttribute("xmlns:ds");
		String xadesIdAttr = ((Element) doc.getElementsByTagName("xades:QualifyingProperties").item(0)).getAttribute("Target");
		
		//Id atribut
		if(sigIdAttr == null || sigIdAttr.equals(""))
			sb.append("   c) CHYBA - element ds:Signature neobsahuje atrib˙t Id." + '\n');
		else if(!xadesIdAttr.contains(sigIdAttr))
			sb.append("   c) CHYBA - hodnota atrib˙tu Id patriaci elementu ds:Signature je nespr·vna." + '\n' + "      Jeho hodnota je: " + sigIdAttr + '\n');
		else sb.append("   c) Element ds:Signature obsahuje atrib˙t Id." + '\n');
		
		//xmlns:ds atribut
		if(sigNamespaceAttr == null || sigNamespaceAttr.equals(""))
			sb.append("   d) CHYBA - element ds:Signature nem· öpecifikovan˝ namespace xmlns:ds." + '\n');
		else if(!sigNamespaceAttr.equals("http://www.w3.org/2000/09/xmldsig#"))
			sb.append("   d) CHYBA - hodnota atrib˙tu xmlns:ds patriaci elementu ds:Signature je nespr·vna." + '\n' + "      Jeho hodnota je: " + sigNamespaceAttr + '\n');
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
			sb.append("   e) CHYBA - element ds:SignatureValue neobsahuje atrib˙t Id." + '\n');
		else if(!sigValIdAttr.contains(el))
			sb.append("   e) CHYBA - hodnota atrib˙tu Id z elementu ds:SignatureValue sa nezhoduje s hodnotou v xades:SignatureTimeStamp." + '\n' + "      Jeho hodnota je: " + xadesSigValIdAttr + '\n');
		else sb.append("   e) Element ds:SignatureValue obsahuje atrib˙t Id." + '\n');
		
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
				sb.append("   g) CHYBA - atrib˙t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo nie je v spr·vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   g) Atrib˙t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo je v spr·vnom tvare." + '\n');
		}
		else sb.append("   g) CHYBA - Atrib˙t Type v referencii na ds:KeyInfo v elemente ds:SignedInfo ch˝ba." + '\n');
		
		if(sigPropsCont)
			sb.append("   h) ds:SignedInfo obsahuje referenciu na ds:SignatureProperties element." + '\n');
		else sb.append("   h) CHYBA - ds:SignedInfo neobsahuje referenciu na ds:SignatureProperties element." + '\n');
		if(sigPropsRef != null && sigPropsRef.hasAttribute("Type")) {
			refType = sigPropsRef.getAttribute("Type");
			if(! refType.equals("http://www.w3.org/2000/09/xmldsig#SignatureProperties"))
				sb.append("   i) CHYBA - atrib˙t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo nie je v spr·vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   i) Atrib˙t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo je v spr·vnom tvare." + '\n');
		}
		else sb.append("   i) CHYBA - Atrib˙t Type v referencii na ds:SignatureProperties v elemente ds:SignedInfo ch˝ba." + '\n');
		
		if(signedPropsCont)
			sb.append("   j) ds:SignedInfo obsahuje referenciu na xades:SignedProperties element." + '\n');
		else sb.append("   j) CHYBA - ds:SignedInfo neobsahuje referenciu na xades:SignedProperties element." + '\n');
		if(signedPropsRef != null && signedPropsRef.hasAttribute("Type")) {
			refType = signedPropsRef.getAttribute("Type");
			if(! refType.equals("http://uri.etsi.org/01903#SignedProperties"))
				sb.append("   k) CHYBA - atrib˙t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo nie je v spr·vnom tvare." + '\n' + "      Jeho hodnota je " + refType + '\n');
			else sb.append("   k) Atrib˙t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo je v spr·vnom tvare." + '\n');
		}
		else sb.append("   k) CHYBA - Atrib˙t Type v referencii na xades:SignedProperties v elemente ds:SignedInfo ch˝ba." + '\n'); 

		if(manifestCont)
			sb.append("   l) ds:SignedInfo obsahuje referencie na ds:Manifest element v spr·vnom tvare." + '\n');
		else sb.append("   l) CHYBA - ds:SignedInfo m· nekonzistentnÈ referencie na ds:Manifest element." + '\n');
		if(manifestIdCheck)
			sb.append("   m) Atrib˙t Type v referenci·ch na ds:Manifest v elementoch ds:SignedInfo s˙ v spr·vnom tvare." + '\n');
		else sb.append("   m) CHYBA - Atrib˙t Type v referenci·ch na ds:Manifest v elementoch ds:SignedInfo nie s˙ konzistentnÈ." + '\n');
		
		return sb.toString();
	}
	private String checkDsKeyInfo(Document doc) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		//OVERENIE ds:KeyInfo
		Element keyInfo = (Element) doc.getElementsByTagName("ds:KeyInfo").item(0);
		String keyInfoId = keyInfo.getAttribute("Id");
		
		if(keyInfoId == null || keyInfoId.equals(""))
			sb.append("   n) CHYBA - ds:KeyInfo neobsahuje atrib˙t Id." + '\n');
		else sb.append("   n) ds:keyInfo obsahuje atrib˙t Id." + '\n');
		
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
			sb.append("   s) Ch˝baj˙ci element (alebo elementy) z radu X509. Ned· sa overiù ich hodnota vzhæadom na certifik·t." + '\n');
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
			sb.append("   s) - objekt ds:X509SubjectName sa zhoduje s hodnotou v certifik·te." + '\n');
		else sb.append("   s) - CHYBA - objekt ds:X509SubjectName sa zhoduje s hodnotou v certifik·te." + '\n' + "              - jeho hodnota je " + docSubjectName + '\n');
		
		if(certSerialNum.equals(docSerialNum))
			sb.append("      - objekt ds:X509SerialNumber sa zhoduje s hodnotou v certifik·te." + '\n');
		else sb.append("      - CHYBA - objekt ds:X509SerialNumber sa nezhoduje s hodnotou v certifik·te." + '\n' + "              - jeho hodnota je " + docSerialNum + '\n');
		
		if(certIssuerName.equals(docIssuerName))
			sb.append("      - objekt ds:X509IssuerName sa zhoduje s hodnotou v certifik·te." + '\n');
		else sb.append("      - CHYBA - objekt ds:X509IssuerName sa nezhoduje s hodnotou v certifik·te." + '\n' + "              - jeho hodnota je             " + docIssuerName + '\n' + "              - oËak·van· hodnota je   " + certIssuerName + '\n');
		
		return sb.toString();
	}
	private String checkDsSignatureProperties(Document doc) {
		StringBuilder sb = new StringBuilder();
		Element sigProps = (Element) doc.getElementsByTagName("ds:SignatureProperties").item(0);
		
		String sigPropsId = sigProps.getAttribute("Id");
		if(sigPropsId == null || sigPropsId.equals(""))
			sb.append("   t) CHYBA - element ds:SignatureProperties neobsahuje atrib˙t Id." + '\n');
		else sb.append("   t) Element ds:SignatureProperties obsahuje atrib˙t Id." + '\n'); 
		
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
				sb.append("      - CHYBA - element ds:SignatureProperty neobsahuje atrib˙t Id." + '\n');
			else sb.append("      - Element ds:SignatureProperty obsahuje atrib˙t Id." + '\n'); 
		}
		else sb.append("   u) CHYBA - ds:SignatureProperties neobsahuje element ds:SignatureProperty s elementom xzep:SignatureVersion." + '\n');
		
		if(xzepProductInfo) {
			sb.append("   v) - ds:SignatureProperties obsahuje element ds:SignatureProperty s elementom xzep:ProductInfos." + '\n');
			
			String xzepId = xzepProduct.getAttribute("Target");
			if(xzepId == null || xzepId.equals(""))
				sb.append("      - CHYBA - element ds:SignatureProperty neobsahuje atrib˙t Id." + '\n');
			else sb.append("      - Element ds:SignatureProperty obsahuje atrib˙t Id." + '\n'); 
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
				sb.append("      " + (i + 1) + ". manifest - CHYBA - dan˝ manifest neobsahuje atrib˙t Id." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - obsahuje atrib˙t Id." + '\n');
			
			NodeList references = elem.getElementsByTagName("ds:Reference");
			if(references.getLength() > 1) {
				int counter = 0;
				for(int j = 0; j < references.getLength(); j++) {
					if(((Element)references.item(j)).getAttribute("URI").contains("Object"))
						counter++;
				}
				
				if(counter != 1)
					sb.append("      " + (i + 1) + ". manifest - CHYBA - element obsahuje nespr·vny poËet referenciÌ (" + counter + ") na ds:Object" + '\n');
				sb.append("      " + (i + 1) + ". manifest obsahuje spr·vny poËet referenciÌ na ds:Object." + '\n');
			}
			else sb.append("      " + (i + 1) + ". manifest obsahuje spr·vny poËet referenciÌ na ds:Object." + '\n');
			
			Element tempElem = ((Element)((Element)elem.getElementsByTagName("ds:Reference").item(0)).getElementsByTagName("ds:Transforms").item(0));
			String transAlg = ((Element)tempElem.getElementsByTagName("ds:Transform").item(0)).getAttribute("Algorithm");
			
			tempElem = ((Element)((Element)elem.getElementsByTagName("ds:Reference").item(0)).getElementsByTagName("ds:DigestMethod").item(0));
			String digAlg = tempElem.getAttribute("Algorithm");
			
			if(canonList.contains(transAlg))
				sb.append("      " + (i + 1) + ". manifest obsahuje pre ds:Transforms podporovan˝ algoritmus transform·cie." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - CHYBA - neobsahuje pre ds:Transforms podporovan˝ algoritmus transform·cie." + '\n');
			
			if(cryptoList.contains(digAlg))
				sb.append("      " + (i + 1) + ". manifest obsahuje pre ds:DigestMethod podporovan˝ algoritmus öifrovania." + '\n');
			else sb.append("      " + (i + 1) + ". manifest - CHYBA - neobsahuje pre ds:DigestMethod podporovan˝ algoritmus öifrovania." + '\n');
		}
		
		return sb.toString();
	}
	private String checkDsManifestReferences(Document doc) throws Exception {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		StringBuilder sb = new StringBuilder();
		NodeList manifests = doc.getElementsByTagName("ds:Manifest");
		
		sb.append("   x) Kontrola referenciÌ ds:Manifest elementov." + '\n');
		for(int i = 0; i < manifests.getLength(); i++) {
			Element manElem = (Element) manifests.item(i);
			Element refElem = (Element) manElem.getElementsByTagName("ds:Reference").item(0);
			Element digElem = (Element)refElem.getElementsByTagName("ds:DigestValue").item(0);
			
			Element transElem = (Element) refElem.getElementsByTagName("ds:Transforms").item(0);
			Element tranElem = (Element) transElem.getElementsByTagName("ds:Transform").item(0);
			String transAlg = tranElem.getAttribute("Algorithm");
			if(!transAlg.equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
				sb.append("      " + (i + 1) + ". manifest - CHYBA - dan˝ algoritmus pre kanonikaliz·ciu nie je podporovan˝, alebo neexistuje. Preto nie je moûnÈ overiù spr·vnosù objektu ds:DigestValue." + '\n');
				continue;
			}
			
			String uri = refElem.getAttribute("URI").replaceAll("#","");
			NodeList objects = doc.getElementsByTagName("ds:Object");
			Element searchedObject = null;
			for(int j = 0; j < objects.getLength(); j++) {
				Element elem = (Element)objects.item(j);
				
				if(elem.hasAttribute("Id") && elem.getAttribute("Id").equals(uri)) {
					searchedObject = elem;
					break;
				}
			}
			
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(searchedObject), new StreamResult(buffer));

			Init.init();
			Canonicalizer canonicalizer = Canonicalizer.getInstance(transAlg);
    		MessageDigest digest = MessageDigest.getInstance("SHA-256");
    		
    		byte[] docToCanonicalize = buffer.toString().getBytes(Charset.forName("UTF-8"));    		
    		byte[] manifestBytes = canonicalizer.canonicalize(docToCanonicalize);
    		String calculatedDigestValue = new String(Base64.getEncoder().encode(digest.digest(manifestBytes)));
    		
    		if(calculatedDigestValue.equals(digElem.getTextContent()))
    			sb.append("      " + (i + 1) + ". manifest - vypoËÌtan· hodnota pre dan˝ ds:Object zodpoved· hodnote ds:DigestValue." + '\n');
    		else sb.append("      " + (i + 1) + ". manifest - CHYBA - vypoËÌtan· hodnota pre dan˝ ds:Object nezodpoved· hodnote ds:DigestValue." 
    								+ '\n' + "        VypoËÌtan· hodnota je: " + calculatedDigestValue
    								+ ", oËak·van· hodnota bola: " + digElem.getTextContent() + '\n');
		}
		
		return sb.toString();
	}
	private String checkTimeStamp (Document doc) throws XPathException, TransformerException {
		
		StringBuilder sb = new StringBuilder();
		
		URL url = null;
		try {
			url = new URL("http://test.ditec.sk/DTCCACrl/DTCCACrl.crl");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		java.io.InputStream crlS = null;
		try {
			crlS = url.openStream();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		X509CRL crl = null;
		
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		try {
			  crl = (X509CRL) cf.generateCRL(crlS);
		} catch (CRLException e) {
			sb.append("   a) CHYBA - Nepodarilo sa ziskat CRL z obdrzanych dat."+ '\n');
			//return sb.toString();
		}
		 
		try {
			crlS.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		if (crlS == null){
			sb.append("   a) CHYBA - Nepodarilo sa stiahnut CRL zo stranky."+ '\n');
			//return sb.toString();
		}

		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance("X.509", "BC");
		} catch (CertificateException | NoSuchProviderException e) {
			sb.append("   a) CHYBA - Nepodarilo sa vytvorit zadanu instanciu CertificateFactory."+ '\n');
			//return sb.toString();
		}

		
		TimeStampToken ts_token = null;

		Node timestamp = null;
		
		timestamp = doc.getElementsByTagName("xades:EncapsulatedTimeStamp").item(0);

		if (timestamp == null){
			sb.append("   a) CHYBA - Dokument neobsahuje casovu peciatku."+ '\n');
			return sb.toString();
		}

		try {
			ts_token = new TimeStampToken(new CMSSignedData(Base64.getDecoder().decode(timestamp.getTextContent())));
		} catch (TSPException | IOException | CMSException e) {
			sb.append(e);
		}

		
		
		X509CertificateHolder signer = null;
		
		Store<X509CertificateHolder> certHolders = ts_token.getCertificates();
		ArrayList<X509CertificateHolder> certList = new ArrayList<>(certHolders.getMatches(null));

		BigInteger serialNumToken = ts_token.getSID().getSerialNumber();
		X500Name issuerToken = ts_token.getSID().getIssuer();
		

		for (X509CertificateHolder certHolder : certList) {
			if (certHolder.getSerialNumber().equals(serialNumToken) && certHolder.getIssuer().equals(issuerToken)){
				signer = certHolder;
				break;
			}
		}
		
		// Overenie platnosti voci aktualnemu datumu
		if (signer == null){
			sb.append("   a) CHYBA - V dokumente sa nenachadza certifikat casovej peciatky."+ '\n');
		}else {

			if (!signer.isValidOn(new Date())){
				sb.append("   a) CHYBA - Podpisov˝ certifik·t Ëasovej peËiatky nie je platn˝ voËi aktu·lnemu Ëasu."+ '\n');
			}else{
				sb.append("   a) Podpisov˝ certifik·t Ëasovej peËiatky  je platn˝ voËi aktu·lnemu Ëasu." + '\n');
			}
			//Overenie ci je certifikat platny voci CRL
			if (crl.getRevokedCertificate(signer.getSerialNumber()) != null){
				sb.append("   b) CHYBA - Podpisov˝ certifik·t Ëasovej peËiatky nie je platn˝ voËi platnÈmu poslednÈmu CRL."+ '\n');
			}else{
				sb.append("   b) Podpisov˝ certifik·t Ëasovej peËiatky je platn˝ voËi platnÈmu poslednÈmu CRL." + '\n');
			}	
		}
		
			
			
		
		
		
		
		
		
		Node signatureValueN = null;
		
		signatureValueN = doc.getElementsByTagName("ds:SignatureValue").item(0);

		if(signatureValueN==null) {
			sb.append("   c) CHYBA - Element ds:SignatureValue nen·jden˝."+ '\n');
			return sb.toString();
			
		}
		
		//Ziskanie Signature value a HASH algorytmu
	
		byte[] signatureValue = Base64.getDecoder().decode(signatureValueN.getTextContent().getBytes());
		
		MessageDigest messageDigest = null;
		String hashAlg = ts_token.getTimeStampInfo().getHashAlgorithm().getAlgorithm().getId();

		//ziskanie hash
		try {
			messageDigest = MessageDigest.getInstance(hashAlg, "BC");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			sb.append("   c) CHYBA -Nepodporovan˝ algoritmus v message digest."+ '\n');
		}
		
		
		//vytiahnutie imprintu z TStoken
		byte[] messageImprint = ts_token.getTimeStampInfo().getMessageImprintDigest();

		//porovnanie signatureValue a Imprint
		if (!Arrays.equals(messageImprint, messageDigest.digest(signatureValue))){
			sb.append("   c) CHYBA - MessageImprint z Ëasovej peËiatky a podpis ds:SignatureValue sa nezhoduj˙." + '\n');
		}else {
			sb.append("   c) MessageImprint z Ëasovej peËiatky a podpis ds:SignatureValue sa zhoduj˙." + '\n');
		}
		//Vytiahnutie certifikatu z dokumentu
		Node x509Certificate = doc.getElementsByTagName("ds:X509Certificate").item(0);
		
		if (x509Certificate == null){
			sb.append("  c) CHYBA - Element ds:X509Certificate nen·jden˝.");
		}
		
		sb.append("5. Valid·cia podpisovÈho certifik·tu:" + '\n');
		
		ASN1InputStream asn1is = new ASN1InputStream(new ByteArrayInputStream(Base64.getDecoder().decode(x509Certificate.getTextContent())));
		
		
		ASN1Sequence sq = null;
		X509CertificateObject cert = null;
		try {
			sq = (ASN1Sequence) asn1is.readObject();
			
		} catch (IOException e) {
			sb.append("   CHYBA - Nie je moûnÈ preËÌtaù ASN1 sekvenciu."+ '\n');
		}
		try {
			cert = new X509CertificateObject(Certificate.getInstance(sq));
		} catch (CertificateParsingException e) {
			sb.append("   CHYBA - Nie je moûnÈ preËÌtaù Certifik·t z dokumentu."+ '\n');
		}
		
		
		try {
			asn1is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Zistenie ci bol certifikat platny pri podpise
		
		Boolean valid = false;
		try {
			cert.checkValidity(ts_token.getTimeStampInfo().getGenTime());
			valid = true; 
		} catch (CertificateExpiredException e) {
			
			sb.append("   a) CHYBA - Certifik·t dokumentu bol pri podpise expirovan˝."+ '\n');
		} catch (CertificateNotYetValidException e) {
			
			sb.append("   a) CHYBA - Certifik·t dokumentu eöte nebol platn˝ v Ëase podpisovania."+ '\n');
		}
		if(valid==true)sb.append("   a) Certifik·t dokumentu bol platn˝ v Ëase podpisovania."+ '\n');
		
		
		//zistenie ci bol certifikat platny v case podpisu
		
		X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
		if (entry != null && entry.getRevocationDate().before(ts_token.getTimeStampInfo().getGenTime())) {
			
			sb.append("   b) CHYBA - Certifik·t bol zruöen˝ v Ëase podpisovania."+ '\n');
		}else sb.append("   b) Certifik·t nebol zruöen˝ v Ëase podpisovania."+ '\n');
		
		
		return sb.toString();
		
		
		
		
		
	}
	
	
	
	
}
