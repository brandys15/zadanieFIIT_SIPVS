package florbalovaLigaView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

public class MainItemsController {

	ObservableList<String> pohlavieList = FXCollections.observableArrayList("Muž", "Žena");
	ObservableList<String> ligistaList = FXCollections.observableArrayList("Áno", "Nie");

	@FXML
	private TextField txtField_teamName;
	@FXML
	private TextField txtField_email;
	@FXML
	private TextField txtField_phoneNum;
	
	@FXML
	private ComboBox<String> comboBox_gender_01;
	@FXML
	private ComboBox<String> comboBox_leaguest_01;
	@FXML
	private TextField txtField_name_01;
	@FXML
	private TextField txtField_surname_01;
	@FXML
	private ComboBox<String> comboBox_gender_02;
	@FXML
	private ComboBox<String> comboBox_leaguest_02;
	@FXML
	private TextField txtField_name_02;
	@FXML
	private TextField txtField_surname_02;
	@FXML
	private ComboBox<String> comboBox_gender_03;
	@FXML
	private ComboBox<String> comboBox_leaguest_03;
	@FXML
	private TextField txtField_name_03;
	@FXML
	private TextField txtField_surname_03;
	@FXML
	private ComboBox<String> comboBox_gender_04;
	@FXML
	private ComboBox<String> comboBox_leaguest_04;
	@FXML
	private TextField txtField_name_04;
	@FXML
	private TextField txtField_surname_04;
	@FXML
	private ComboBox<String> comboBox_gender_05;
	@FXML
	private ComboBox<String> comboBox_leaguest_05;
	@FXML
	private TextField txtField_name_05;
	@FXML
	private TextField txtField_surname_05;

	private DocumentBuilderFactory docFactory = null;
	private DocumentBuilder docBuilder = null;
	private FileChooser fileChooser = null;

	private ArrayList<TextField> listOfNameFields = null;
	private ArrayList<TextField> listOfSurnameFields = null;
	private ArrayList<ComboBox<String>> listOfGenderBoxes = null;
	private ArrayList<ComboBox<String>> listOfLeaguestBoxes = null;

	private static final int MINIMUM_PLAYER_COUNT = 3;

	@FXML
	private void initialize() {
		comboBox_gender_01.setItems(pohlavieList);
		comboBox_leaguest_01.setItems(ligistaList);

		comboBox_gender_02.setItems(pohlavieList);
		comboBox_leaguest_02.setItems(ligistaList);

		comboBox_gender_03.setItems(pohlavieList);
		comboBox_leaguest_03.setItems(ligistaList);

		comboBox_gender_04.setItems(pohlavieList);
		comboBox_leaguest_04.setItems(ligistaList);

		comboBox_gender_05.setItems(pohlavieList);
		comboBox_leaguest_05.setItems(ligistaList);

		initializeVariables();
	}

	private void initializeVariables() {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

			listOfNameFields = new ArrayList<TextField>();
			listOfSurnameFields = new ArrayList<TextField>();
			listOfGenderBoxes = new ArrayList<ComboBox<String>>();
			listOfLeaguestBoxes = new ArrayList<ComboBox<String>>();

			listOfNameFields.add(txtField_name_01);
			listOfNameFields.add(txtField_name_02);
			listOfNameFields.add(txtField_name_03);
			listOfNameFields.add(txtField_name_04);
			listOfNameFields.add(txtField_name_05);

			listOfSurnameFields.add(txtField_surname_01);
			listOfSurnameFields.add(txtField_surname_02);
			listOfSurnameFields.add(txtField_surname_03);
			listOfSurnameFields.add(txtField_surname_04);
			listOfSurnameFields.add(txtField_surname_05);

			listOfGenderBoxes.add(comboBox_gender_01);
			listOfGenderBoxes.add(comboBox_gender_02);
			listOfGenderBoxes.add(comboBox_gender_03);
			listOfGenderBoxes.add(comboBox_gender_04);
			listOfGenderBoxes.add(comboBox_gender_05);

			listOfLeaguestBoxes.add(comboBox_leaguest_01);
			listOfLeaguestBoxes.add(comboBox_leaguest_02);
			listOfLeaguestBoxes.add(comboBox_leaguest_03);
			listOfLeaguestBoxes.add(comboBox_leaguest_04);
			listOfLeaguestBoxes.add(comboBox_leaguest_05);

		} catch (Exception e) {
			setError("Error!", e.getMessage(), getStackTrace(e));
		}
	}

	private void clearVariables() {
		this.txtField_teamName.clear();
		this.txtField_phoneNum.clear();
		this.txtField_email.clear();

		for (int i = 0; i < listOfNameFields.size(); i++) {
			this.listOfNameFields.get(i).clear();
			this.listOfGenderBoxes.get(i).valueProperty().set(null);
			this.listOfSurnameFields.get(i).clear();
			this.listOfLeaguestBoxes.get(i).valueProperty().set(null);
		}
	}

	private int checkNumOfCompletedFields(TextField name, TextField surname, ComboBox<String> gender,
			ComboBox<String> leaguest) {
		int count = 0;

		if (!name.getText().equals(""))
			count++;
		if (!surname.getText().equals(""))
			count++;
		if (gender.getValue() != null)
			count++;
		if (leaguest.getValue() != null)
			count++;

		return count;
	}

	private boolean checkString(String text) {
		return text.matches("[a-zA-Z ]+");
	}

	private boolean checkEmail(String text) {
		return text.matches("[a-zA-Z0-9.,/*\\_%+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,4}");
	}

	private boolean checkNum(String text) {
		return text.matches("(\\+421|0)[0-9]{9}");
	}

	private Element getPlayersElem(Document doc) {
		Element playersElem = doc.createElement("players");
		ArrayList<Element> listOfPlayers = new ArrayList<Element>();

		for (int i = 0; i < listOfNameFields.size(); i++) {
			int numOfCompletedFields = checkNumOfCompletedFields(listOfNameFields.get(i), listOfSurnameFields.get(i),
					listOfGenderBoxes.get(i), listOfLeaguestBoxes.get(i));

			if (numOfCompletedFields == 4) {
				Element player = doc.createElement("player");
				Element firstname = doc.createElement("firstname");
				Element lastname = doc.createElement("lastname");
				Element leaguest = doc.createElement("leaguest");

				firstname.setTextContent(listOfNameFields.get(i).getText());
				lastname.setTextContent(listOfSurnameFields.get(i).getText());

				if (!checkString(listOfNameFields.get(i).getText())) {
					setError("Error!", "Invalid string for name.", null);
					return null;
				}
				if (!checkString(listOfSurnameFields.get(i).getText())) {
					setError("Error!", "Invalid string for surname.", null);
					return null;
				}

				if (listOfGenderBoxes.get(i).getValue().equals("Muž"))
					player.setAttribute("gender", "m");
				else if (listOfGenderBoxes.get(i).getValue().equals("Žena"))
					player.setAttribute("gender", "f");

				if (listOfLeaguestBoxes.get(i).getValue().equals("Áno"))
					leaguest.setTextContent("true");
				else if (listOfLeaguestBoxes.get(i).getValue().equals("Nie"))
					leaguest.setTextContent("false");

				player.appendChild(firstname);
				player.appendChild(lastname);
				player.appendChild(leaguest);

				listOfPlayers.add(player);
			} else if (numOfCompletedFields > 0 && numOfCompletedFields < 4) {
				setError("Error!", "Not all fields for the current player are filled in.", null);
				if (listOfPlayers.size() < MINIMUM_PLAYER_COUNT) {
					setError("Error!", "Not enough players!",
							"Minimum count for registrating a team is " + MINIMUM_PLAYER_COUNT + '.');
				}
				return null;
			} else if (numOfCompletedFields == 0)
				if ((i + 1) < listOfNameFields.size())
					if (checkNumOfCompletedFields(listOfNameFields.get(i + 1), listOfSurnameFields.get(i + 1),
							listOfGenderBoxes.get(i + 1), listOfLeaguestBoxes.get(i + 1)) == 4)
						continue;
		}

		if (listOfPlayers.size() < MINIMUM_PLAYER_COUNT) {
			setError("Error!", "Not enough players!",
					"Minimum count for registrating a team is " + MINIMUM_PLAYER_COUNT + '.');
			return null;
		}

		for (Element e : listOfPlayers)
			playersElem.appendChild(e);

		return playersElem;
	}

	private ArrayList<File> getAllFiles() {
		ArrayList<File> list = new ArrayList<File>();
		File xslfile = null;
		File xsdfile = null;
		File xmlfile = null;
		
		try {
			fileChooser.setTitle("Please choose an XML file.");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
			
			xmlfile = fileChooser.showOpenDialog(null);
			if (xmlfile == null) {
				setError("Error!", "No XML file was chosen.", null);
				return null;
			}
			
			fileChooser.setInitialDirectory(xmlfile.getParentFile());
			fileChooser.setTitle("Please choose an XSD file.");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSD", "*.xsd"));
			
			xsdfile = fileChooser.showOpenDialog(null);
			if (xsdfile == null) {
				setError("Error!", "No XSD file was chosen.", null);
				return null;
			}
			
			fileChooser.setInitialDirectory(xsdfile.getParentFile());
			fileChooser.setTitle("Please choose an XSL file.");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSL", "*.xslt"));
			
			xslfile = fileChooser.showOpenDialog(null);
			if (xslfile == null) {
				setError("Error!", "No XSL file was chosen.", null);
				return null;
			}
			
			fileChooser.setInitialDirectory(xslfile.getParentFile());
			
			list.add(xmlfile);
			list.add(xslfile);
			list.add(xsdfile);
			
			return list;
		}
		catch(Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
			return null;
		}
	}
	
	private void setInformation(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	@SuppressWarnings("unused")
	private void setWarning(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	private void setError(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.ERROR);
		
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		
		Label label = new Label("The exception stacktrace is:");
		TextArea textArea = new TextArea(contentText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}
	private String getStackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		ex.printStackTrace(pw);
		
		return sw.toString();
	}
	
	@FXML
	protected void ValidateXML(ActionEvent event) {
		File xsdfile = null;
		File xmlfile = null;

		fileChooser.setTitle("Please choose an XML file.");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

		xmlfile = fileChooser.showOpenDialog(null);
		if (xmlfile != null) {
			fileChooser.setTitle("Please choose an XSD file.");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSD", "*.xsd"));
			fileChooser.setInitialDirectory(xmlfile.getParentFile());

			xsdfile = fileChooser.showOpenDialog(null);
			if (xsdfile != null) {
				fileChooser.setInitialDirectory(xsdfile.getParentFile());
				
				Source xmlFile = new StreamSource(xmlfile);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				try {
					Schema schema = schemaFactory.newSchema(xsdfile);
					Validator validator = schema.newValidator();
					validator.validate(xmlFile);
					setInformation("Success!", xmlfile.getName() + " is valid.", null);
				} catch (SAXException e) {
					setError("Error!", xmlfile.getName() + " is invalid.",
							"Reason: Line " + ((SAXParseException) e).getLineNumber() + ". " + e.getMessage());
				} catch (IOException e) {
					this.setError("Error!", e.getMessage(), getStackTrace(e));
				}
			} else
				setError("Error!", "No file was selected! Please choose an XSD file.", null);
		} else
			setError("Error!", "No file was selected! Please choose an XML file.", null);
	}

	@FXML
	protected void GenerateXML(ActionEvent event) {
		try {
			Document doc = docBuilder.newDocument();			
			Element root = doc.createElement("ufl_team");
			root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:noNamespaceSchemaLocation", "UFL.xsd");
			doc.appendChild(root);
						
			if (this.txtField_teamName.getText().equals("")) {
				setError("Error!", "Missing team name!", null);
				return;
			}
			if (!checkEmail(this.txtField_email.getText())) {
				setError("Error!", "Wrong e-mail format!", null);
				return;
			}
			if (!checkNum(this.txtField_phoneNum.getText())) {
				setError("Error!", "Wrong format of phone number!", "Allowed are 0900000000 or +421900000000!");
				return;
			}
			
			Element teamName = doc.createElement("team_name");
			teamName.setTextContent(this.txtField_teamName.getText());
			
			Element email = doc.createElement("email");
			email.setTextContent(this.txtField_email.getText());
			
			Element phoneNum = doc.createElement("phone_number");
			phoneNum.setTextContent(this.txtField_phoneNum.getText());
			
			Element players = getPlayersElem(doc);
			if (players == null)
				return;

			root.appendChild(teamName);
			root.appendChild(email);
			root.appendChild(phoneNum);
			root.appendChild(players);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			DOMSource xmlSource = new DOMSource(doc);

			fileChooser.setTitle("Save file as");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

			File file = fileChooser.showSaveDialog(null);
			if (file != null) {
				fileChooser.setInitialDirectory(file.getParentFile());
				
				StreamResult stream = new StreamResult(file);
				transformer.transform(xmlSource, stream);
				setInformation("Success!", "XML file was successfully written.", null);
				clearVariables();
			} else
				setError("Error!", "No file was selected for creation!", null);

		} catch (Exception e) {
			setError("Error!", e.getMessage(), getStackTrace(e));
		}
	}

	@FXML
	protected void VisualizeXML(ActionEvent event) {
		File xslfile = null;
		File xmlfile = null;

		fileChooser.setTitle("Please choose an XML file.");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
		
		try {
			xmlfile = fileChooser.showOpenDialog(null);
			if (xmlfile != null) {
				fileChooser.setTitle("Please choose an XSL file.");
				fileChooser.setInitialDirectory(xmlfile.getParentFile());
				fileChooser.getExtensionFilters().clear();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XSLT", "*.xslt"));

				xslfile = fileChooser.showOpenDialog(null);
				if (xslfile != null) {
					Source xml = new StreamSource(xmlfile);
					Source xsl = new StreamSource(xslfile);

					fileChooser.setTitle("Save file as");
					fileChooser.setInitialDirectory(xslfile.getParentFile());
					fileChooser.getExtensionFilters().clear();
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));

					File file = fileChooser.showSaveDialog(null);
					if (file != null) {
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
						StringWriter sw = new StringWriter();

						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer(xsl);
						
						transformer.transform(xml, new StreamResult(sw));
						
						bw.write(sw.toString());
						bw.close();
						
						setInformation("Success!", "HTML file successfully written.", null);
					} 
					else setError("Error!", "No file was selected for creation!", null);
				} 
				else setError("Error!", "No file was selected! Please choose an XSL file.", null);
			}
			else setError("Error!", "No file was selected! Please choose an XML file.", null);
		} catch (Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
		}
	}
	
	@FXML
	protected void Sign(ActionEvent event) {
		/*try {
			ArrayList<File> listOfFiles = getAllFiles();
			if(listOfFiles == null)
				return;
			
			File xslfile = listOfFiles.get(0);
			File xsdfile = listOfFiles.get(1);
			File xmlfile = listOfFiles.get(2);
			
			XadesSig dSigner = new XadesSig();
			dSigner.installLookAndFeel();
			dSigner.installSwingLocalization();
			dSigner.reset();
			
			XmlPlugin xmlPlugin = new XmlPlugin();
			DataObject xmlObject = xmlPlugin.createObject2(
					"xml_sig",							//object ID
					"UFL team",							//object description
					xmlfile.getAbsolutePath(),			//XML source
					xsdfile.getAbsolutePath(),			//XSD source
					namespaceUri,						//Namespace URI
					xsdReference,						//XSD reference
					xslfile.getAbsolutePath(),			//XSL source
					xslReference);						//XSL reference
			
			if(xmlObject == null) {
				setError("Error!", "Something went wrong.", xmlPlugin.getErrorMessage());
				return;
			}
			
			int checker = dSigner.addObject(xmlObject);
			if(checker != 0) {
				setError("Error!", "Something went wrong.", dSigner.getErrorMessage());
				return;
			}
			
			checker = dSigner.sign20(
					"ufl_sig",					//signature ID
					"http://www.w3.org/2001/04/xmlenc#sha256",			//identifikátor algoritmu pre výpoèet digitálnych odtlaèkov v rámci vytváraného elektronického podpisu; nepovinný parameter; ak je null alebo prázdny, použije sa algoritmus špecifikovaný v rámci konfigurácie aplikácie
					signaturePolicyIdentifier,	//jednoznaèný identifikátor podpisovej politiky použitej pri vytváraní elektronického podpisu
					dataEnvelopeId,				//jednoznaèné XML Id elementu xzep:DataEnvelope
					dataEnvelopeURI,			//URI atribút elementu xzep:DataEnvelope
					dataEnvelopeDescr);			//Description atribút elementu xzep:DataEnvelope
			
			if(checker != 0) {
				setError("Error!", "Something went wrong.", dSigner.getErrorMessage());
				return;
			}
			
			//OCAKAVANE TO DO - preverenie, ake rozne verzie dokumentov vytvara (cez getSignedXml - rozne verzie)
			//(pravdepodobne) ulozenie do subora cez BufferedOutputStream alebo daco take
			
			setInformation("Success!", "Document successfully created.", dSigner.getSignedXmlWithEnvelope());
		}
		catch(Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
		}*/
	}
}

/*
 * UNUSED - SETTING XSL STYLESHEET TO XML (MAYBE NOT NECESSARY)
 * 
			//doc.appendChild(root);
			//ProcessingInstructionImpl pi = (ProcessingInstructionImpl) doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"ToHtml.xslt\"");
			//root = doc.getDocumentElement();
			//doc.insertBefore((Node) pi, root);
 */
