package florbalovaLigaView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

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

import org.bouncycastle.tsp.TimeStampResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import florbalovaLiga.Callback;
import florbalovaLiga.ResourceUtils;
import florbalovaLiga.TimestampUtils;
import florbalovaLiga.Utils;
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
import sk.ditec.zep.dsigner.xades.XadesSig;
import sk.ditec.zep.dsigner.xades.plugin.DataObject;
import sk.ditec.zep.dsigner.xades.plugins.xmlplugin.XmlPlugin;

public class MainItemsController {
	ObservableList<String> pohlavieList = FXCollections.observableArrayList("Mu�", "�ena");
	ObservableList<String> ligistaList = FXCollections.observableArrayList("�no", "Nie");

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
	private static final int NUM_OF_XML_FILES = 1;

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

	private boolean checkString(String text) {
		return text.matches("[a-zA-Z ]+");
	}

	private boolean checkEmail(String text) {
		return text.matches("[a-zA-Z0-9.,/*\\_%+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,4}");
	}

	private boolean checkNum(String text) {
		return text.matches("(\\+421|0)[0-9]{9}");
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

				if (listOfGenderBoxes.get(i).getValue().equals("Mu�"))
					player.setAttribute("gender", "m");
				else if (listOfGenderBoxes.get(i).getValue().equals("�ena"))
					player.setAttribute("gender", "f");

				if (listOfLeaguestBoxes.get(i).getValue().equals("�no"))
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

	private ArrayList<File> getFilesForSigning(int numOfXmlFiles) {
		ArrayList<File> list = new ArrayList<File>();
		File xslfile = null;
		File xsdfile = null;
		File xmlfile = null;

		try {
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
			list.add(xslfile);
			list.add(xsdfile);

			for (int i = 0; i < numOfXmlFiles; i++) {
				fileChooser.setTitle("Please choose an XML file.");
				fileChooser.getExtensionFilters().clear();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

				xmlfile = fileChooser.showOpenDialog(null);
				if (xmlfile == null) {
					setError("Error!", "No XML file was chosen.", null);
					return null;
				}

				fileChooser.setInitialDirectory(xmlfile.getParentFile());
				list.add(xmlfile);
				xmlfile = null;
			}

			return list;
		} catch (Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
			return null;
		}
	}

	private int checkObjectAddingResult(XadesSig dSigner) {
		int checker = dSigner.getReturnCode();

		if (checker != 0) {
			if (checker == -1) {
				setErrorPlain("Error!", "TS answear is empty!", dSigner.getErrorMessage());
				return -1;
			} else if (checker == -2) {
				setErrorPlain("Error!", "Signature was not successfully created!", dSigner.getErrorMessage());
				return -1;
			} else if (checker == -3) {
				setErrorPlain("Error!", "JavaScript error!", dSigner.getErrorMessage());
				return -1;
			} else {
				setErrorPlain("Error!", "Something went wrong.", dSigner.getErrorMessage());
				return -1;
			}
		}

		return 0;
	}

	private int checkSigningResult(XadesSig dSigner) {
		int checker = dSigner.getReturnCode();

		if (checker != 0) {
			if (checker == 1) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"The signing can't be done if it's cancelled!");
				return -1;
			} else if (checker == -1) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"Unkonwn algorythm of digital sign or unknown signing policy");
				return -1;
			} else if (checker == -2) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.", "No object to sign!");
				return -1;
			} else if (checker == -3) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.", "Parameter SignatureId is empty!");
				return -1;
			} else if (checker == -4) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"SignatureId is not relevant for regular expression of ID!");
				return -1;
			} else if (checker == -5) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.", "Ambiguity of input XML ID");
				return -1;
			} else if (checker == -6) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"DataEnvelopeId is not relevant for regular expression of ID!");
			} else if (checker == -7) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"DataEnvelopeUri is not relevant for valid Uri!");
				return -1;
			} else if (checker == -8) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"Ambiguity of DataEnvelopeId and SignatureId!");
				return -1;
			} else if (checker == -11) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.", "Function sign is allready in progres!");
				return -1;
			} else if (checker == -12) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"Can't find plugin of incoming data type!");
				return -1;
			} else if (checker == -13) {
				Utils.setErrorPlain("Error!", "The signing wasn't completed.",
						"Before recalling of function sign() is necessary to call function reset()!");
				return -1;
			} else {
				Utils.setError("Error!", "Something went wrong.", dSigner.getErrorMessage());
				return -1;
			}
		}
		return 0;
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

	private void setErrorPlain(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}

	private String getStackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		ex.printStackTrace(pw);

		return sw.toString();
	}

	/*
	 * Core functionality of application
	 */
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
			root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
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
						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
						StringWriter sw = new StringWriter();

						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer(xsl);

						transformer.transform(xml, new StreamResult(sw));

						bw.write(sw.toString());
						bw.close();

						setInformation("Success!", "HTML file successfully written.", null);
					} else
						setError("Error!", "No file was selected for creation!", null);
				} else
					setError("Error!", "No file was selected! Please choose an XSL file.", null);
			} else
				setError("Error!", "No file was selected! Please choose an XML file.", null);
		} catch (Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
		}
	}

	@FXML
	protected void Sign(ActionEvent event) {
		try {
			ArrayList<File> listOfFiles = getFilesForSigning(NUM_OF_XML_FILES);
			if (listOfFiles == null)
				return;

			File xslfile = listOfFiles.get(0);
			File xsdfile = listOfFiles.get(1);
			ArrayList<File> listOfXmlFiles = new ArrayList<File>();
			for (int i = 2; i < listOfFiles.size(); i++)
				listOfXmlFiles.add(listOfFiles.get(i));

			XadesSig dSigner = new XadesSig();
			dSigner.installLookAndFeel();
			dSigner.installSwingLocalization();
			dSigner.reset();

			for (int i = 0; i < listOfXmlFiles.size(); i++) {
				XmlPlugin xmlPlugin = new XmlPlugin();
				DataObject xmlObject = xmlPlugin.createObject2("xml_sig_" + i, // object ID
						"UFL team", // object description
						ResourceUtils.readResource(listOfXmlFiles.get(i).getAbsolutePath()), // XML source
						ResourceUtils.readResource(xsdfile.getAbsolutePath()), // XSD source
						"", // Namespace URI https://github.com/nothing_is_hore
						"http://www.w3.org/2001/XMLSchema", // XSD reference //http://www.w3.org/2001/XMLSchema-instance
						ResourceUtils.readResource(xslfile.getAbsolutePath()), // XSL source
						"http://www.w3.org/1999/XSL/Transform", // XSL reference
						XmlPlugin.VISUAL_TRANSFORM_HTML);

				if (xmlObject == null) {
					setError("Error!", "Something went wrong.", xmlPlugin.getErrorMessage());
					return;
				}

				dSigner.addObject(xmlObject);
				if (checkObjectAddingResult(dSigner) == -1)
					return;
			}

			dSigner.sign20("ufl_sig", // signature ID
					"http://www.w3.org/2001/04/xmlenc#sha256", // identifik�tor algoritmu pre v�po�et digit�lnych
																// odtla�kov v r�mci vytv�ran�ho elektronick�ho podpisu;
																// nepovinn� parameter; ak je null alebo pr�zdny,
																// pou�ije sa algoritmus �pecifikovan� v r�mci
																// konfigur�cie aplik�cie
					"urn:oid:1.3.158.36061701.1.2.2", // jednozna�n� identifik�tor podpisovej politiky pou�itej pri
														// vytv�ran� elektronick�ho podpisu
					"dataEnvelopeId", // jednozna�n� XML Id elementu xzep:DataEnvelope
					"dataEnvelopeURI", // URI atrib�t elementu xzep:DataEnvelope
					"dataEnvelopeDescr", // Description atrib�t elementu xzep:DataEnvelope
					Callback.getInstance());

			if (checkSigningResult(dSigner) == -1)
				return;

			fileChooser.setTitle("Save file as");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

			File file = fileChooser.showSaveDialog(null);
			if (file != null) {
				BufferedOutputStream bufOut = new BufferedOutputStream(new FileOutputStream(file));
				byte[] xmlBytes = dSigner.getSignedXmlWithEnvelope().getBytes("UTF-8");

				bufOut.write(xmlBytes);
				bufOut.close();

				fileChooser.setInitialDirectory(file.getParentFile());
			}

			setInformation("Success!", "Document successfully created.", null);
		} catch (Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
		}
	}

	@FXML
	protected void AddTimestamp(ActionEvent event) {
		File signedXml = null;

		fileChooser.setTitle("Please choose an XML file.");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));

		signedXml = fileChooser.showOpenDialog(null);
		if (signedXml == null) {
			setError("Error!", "No XML file was chosen.", null);
			return;
		}
		try {
			fileChooser.setInitialDirectory(signedXml.getParentFile());
			docBuilder.reset();

			Document doc = docBuilder.parse(signedXml);
			Node sigElem = doc.getElementsByTagName("ds:SignatureValue").item(0);
			if (sigElem == null) {
				setErrorPlain("Error!", "Your document doesn't contain a important part for creating timestamp!", null);
				return;
			}

			String signValue = sigElem.getTextContent();
			String tsGenUrl = "http://test.ditec.sk/timestampws/TS.aspx";
			byte request[] = TimestampUtils.getRequest(Base64.getEncoder().encodeToString(signValue.getBytes()).getBytes());
			TimeStampResponse response = TimestampUtils.getResponse(request, tsGenUrl);

			Node qualifProps = doc.getElementsByTagName("xades:QualifyingProperties").item(0);
			if (qualifProps == null) {
				setErrorPlain("Error!", "Error! Missing 'xades:QualifyingProperties' element from signed XML document", null);
				return;
			}
			
			String tsToken = new String(Base64.getEncoder().encode(response.getTimeStampToken().getEncoded()));
			TimestampUtils.addTsElements(doc, tsToken, qualifProps);
			
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
			}
			else setError("Error!", "No file was selected for creation!", null);
		} catch (Exception ex) {
			setError("Error!", ex.getMessage(), getStackTrace(ex));
		}
	}
}
