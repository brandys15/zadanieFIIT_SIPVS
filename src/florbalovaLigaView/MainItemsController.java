package florbalovaLigaView;

import java.io.File;
import java.io.IOException;
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
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class MainItemsController {

	ObservableList<String> pohlavieList = FXCollections.observableArrayList("Muž", "Žena");
	ObservableList<String> ligistaList = FXCollections.observableArrayList("Áno", "Nie");
	// informacie v tabulke
	// private boolean nazovCheck;
	// private boolean checkNum;
	// private boolean emailCheck;
	@FXML
	private TextField nazovTimu;
	@FXML
	private TextField email;
	@FXML
	private TextField telCislo;
	@FXML
	private ComboBox<String> pohlavieBox1;
	@FXML
	private ComboBox<String> ligistaBox1;
	@FXML
	private TextField Meno1;
	@FXML
	private TextField Priezvisko1;
	@FXML
	private ComboBox<String> pohlavieBox2;
	@FXML
	private ComboBox<String> ligistaBox2;
	@FXML
	private TextField Meno2;
	@FXML
	private TextField Priezvisko2;
	@FXML
	private ComboBox<String> pohlavieBox3;
	@FXML
	private ComboBox<String> ligistaBox3;
	@FXML
	private TextField Meno3;
	@FXML
	private TextField Priezvisko3;
	@FXML
	private ComboBox<String> pohlavieBox4;
	@FXML
	private ComboBox<String> ligistaBox4;
	@FXML
	private TextField Meno4;
	@FXML
	private TextField Priezvisko4;
	@FXML
	private ComboBox<String> pohlavieBox5;
	@FXML
	private ComboBox<String> ligistaBox5;
	@FXML
	private TextField Meno5;
	@FXML
	private TextField Priezvisko5;
	
	private DocumentBuilderFactory docFactory = null;
	private DocumentBuilder docBuilder = null;
	
	@FXML
	private void initialize() {
		pohlavieBox1.setItems(pohlavieList);
		ligistaBox1.setItems(ligistaList);

		pohlavieBox2.setItems(pohlavieList);
		ligistaBox2.setItems(ligistaList);

		pohlavieBox3.setItems(pohlavieList);
		ligistaBox3.setItems(ligistaList);

		pohlavieBox4.setItems(pohlavieList);
		ligistaBox4.setItems(ligistaList);

		pohlavieBox5.setItems(pohlavieList);
		ligistaBox5.setItems(ligistaList);

		initializeVariables();
	}
	private void initializeVariables() {
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
		} catch (Exception e) {
			setError("Error!", e.getMessage(), e.getStackTrace().toString());
		}
	}

	private boolean checkString(String text) {
		return text.matches("[a-zA-Z ]+");
	}
	private boolean checkEmail(String text) {
		return text.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}");
	}
	private boolean checkNum(String text) {
		return text.matches("(\\+)?[0-9]+");
	}
	
	private Element getPlayersElem(Document doc) {
		Element playersElem = doc.createElement("players");
		ArrayList<Element> listOfPlayers = new ArrayList<Element>();
		
		if(!Meno1.getText().equals("") && !Priezvisko1.getText().equals("") && pohlavieBox1.getValue() != null && ligistaBox1.getValue() != null) {
			Element player = doc.createElement("player");
			Element firstname = doc.createElement("firstname");
			Element lastname = doc.createElement("lastname");
			Element gender = doc.createElement("gender");
			Element leaguest = doc.createElement("leaguest");
			
			firstname.setTextContent(Meno1.getText());
			lastname.setTextContent(Priezvisko1.getText());
			
			if(!checkString(Meno1.getText())) {
				setError("Error!", "Invalid string for name.", null);
				return null;
			}
			if(!checkString(Priezvisko1.getText())) {
				setError("Error!", "Invalid string for surname.", null);
				return null;
			}
			
			if (pohlavieBox1.getValue().equals("Muž"))
				gender.setTextContent("m");
			else if (pohlavieBox1.getValue().equals("Žena"))
				gender.setTextContent("f");
			
			if (ligistaBox1.getValue().equals("Áno"))
				leaguest.setTextContent("true");
			else if (ligistaBox1.getValue().equals("Nie"))
				leaguest.setTextContent("false");
			
			player.appendChild(firstname);
			player.appendChild(lastname);
			player.appendChild(gender);
			player.appendChild(leaguest);
			
			listOfPlayers.add(player);
		}
		
		if(!Meno2.getText().equals("") && !Priezvisko2.getText().equals("") && pohlavieBox2.getValue() != null && ligistaBox2.getValue() != null) {
			Element player = doc.createElement("player");
			Element firstname = doc.createElement("firstname");
			Element lastname = doc.createElement("lastname");
			Element gender = doc.createElement("gender");
			Element leaguest = doc.createElement("leaguest");
			
			firstname.setTextContent(Meno2.getText());
			lastname.setTextContent(Priezvisko2.getText());
			
			if(!checkString(Meno2.getText())) {
				setError("Error!", "Invalid string for name.", null);
				return null;
			}
			if(!checkString(Priezvisko2.getText())) {
				setError("Error!", "Invalid string for surname.", null);
				return null;
			}
			
			if (pohlavieBox2.getValue().equals("Muž"))
				gender.setTextContent("m");
			else if (pohlavieBox2.getValue().equals("Žena"))
				gender.setTextContent("f");
			if (ligistaBox2.getValue().equals("Áno"))
				gender.setTextContent("true");
			else if (ligistaBox2.getValue().equals("Nie"))
				gender.setTextContent("false");
			
			player.appendChild(firstname);
			player.appendChild(lastname);
			player.appendChild(gender);
			player.appendChild(leaguest);
			
			listOfPlayers.add(player);
		}
		
		if(!Meno3.getText().equals("") && !Priezvisko3.getText().equals("") && pohlavieBox3.getValue() != null && ligistaBox3.getValue() != null) {
			Element player = doc.createElement("player");
			Element firstname = doc.createElement("firstname");
			Element lastname = doc.createElement("lastname");
			Element gender = doc.createElement("gender");
			Element leaguest = doc.createElement("leaguest");
			
			firstname.setTextContent(Meno3.getText());
			lastname.setTextContent(Priezvisko3.getText());
			
			if(!checkString(Meno3.getText())) {
				setError("Error!", "Invalid string for name.", null);
				return null;
			}
			if(!checkString(Priezvisko3.getText())) {
				setError("Error!", "Invalid string for surname.", null);
				return null;
			}
			
			if (pohlavieBox3.getValue().equals("Muž"))
				gender.setTextContent("m");
			else if (pohlavieBox3.getValue().equals("Žena"))
				gender.setTextContent("f");
			if (ligistaBox3.getValue().equals("Áno"))
				gender.setTextContent("true");
			else if (ligistaBox3.getValue().equals("Nie"))
				gender.setTextContent("false");
			
			player.appendChild(firstname);
			player.appendChild(lastname);
			player.appendChild(gender);
			player.appendChild(leaguest);
			
			listOfPlayers.add(player);
		}
		
		if(!Meno4.getText().equals("") && !Priezvisko4.getText().equals("") && pohlavieBox4.getValue() != null && ligistaBox4.getValue() != null) {
			Element player = doc.createElement("player");
			Element firstname = doc.createElement("firstname");
			Element lastname = doc.createElement("lastname");
			Element gender = doc.createElement("gender");
			Element leaguest = doc.createElement("leaguest");
			
			firstname.setTextContent(Meno4.getText());
			lastname.setTextContent(Priezvisko4.getText());
			
			if(!checkString(Meno4.getText())) {
				setError("Error!", "Invalid string for name.", null);
				return null;
			}
			if(!checkString(Priezvisko4.getText())) {
				setError("Error!", "Invalid string for surname.", null);
				return null;
			}
			
			if (pohlavieBox4.getValue().equals("Muž"))
				gender.setTextContent("m");
			else if (pohlavieBox4.getValue().equals("Žena"))
				gender.setTextContent("f");
			if (ligistaBox4.getValue().equals("Áno"))
				gender.setTextContent("true");
			else if (ligistaBox4.getValue().equals("Nie"))
				gender.setTextContent("false");
			
			player.appendChild(firstname);
			player.appendChild(lastname);
			player.appendChild(gender);
			player.appendChild(leaguest);
			
			listOfPlayers.add(player);
		}		
		
		if(!Meno5.getText().equals("") && !Priezvisko5.getText().equals("") && pohlavieBox5.getValue() != null && ligistaBox5.getValue() != null) {
			Element player = doc.createElement("player");
			Element firstname = doc.createElement("firstname");
			Element lastname = doc.createElement("lastname");
			Element gender = doc.createElement("gender");
			Element leaguest = doc.createElement("leaguest");
			
			firstname.setTextContent(Meno5.getText());
			lastname.setTextContent(Priezvisko5.getText());
			
			if(!checkString(Meno5.getText())) {
				setError("Error!", "Invalid string for name.", null);
				return null;
			}
			if(!checkString(Priezvisko5.getText())) {
				setError("Error!", "Invalid string for surname.", null);
				return null;
			}
			
			if (pohlavieBox5.getValue().equals("Muž"))
				gender.setTextContent("m");
			else if (pohlavieBox5.getValue().equals("Žena"))
				gender.setTextContent("f");
			if (ligistaBox5.getValue().equals("Áno"))
				gender.setTextContent("true");
			else if (ligistaBox5.getValue().equals("Nie"))
				gender.setTextContent("false");
			
			player.appendChild(firstname);
			player.appendChild(lastname);
			player.appendChild(gender);
			player.appendChild(leaguest);
			
			listOfPlayers.add(player);
		}
		
		for(Element e: listOfPlayers)
			playersElem.appendChild(e);
		
		return playersElem;
	}
	
	private void setInformation(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}	
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
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	
	@FXML
	protected void ValidateXML(ActionEvent event) {
		File xsdfile = null;
		File xmlfile = null;
		FileChooser fileChooser = new FileChooser();
		
		fileChooser.setTitle("Please choose an XML file.");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		xmlfile = fileChooser.showOpenDialog(null);
		if (xmlfile != null) {
			fileChooser.setTitle("Please choose an XSD file.");
			xsdfile = fileChooser.showOpenDialog(null);
			if (xsdfile != null) {
				Source xmlFile = new StreamSource(xmlfile);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				try {
					Schema schema = schemaFactory.newSchema(xsdfile);
					Validator validator = schema.newValidator();
					validator.validate(xmlFile);					
					setInformation("Success!", xmlfile.getName() + " is valid.", null);
				} catch (SAXException e) {
					setError("Error!", xmlfile.getName() + " is invalid.", "Reason: Line " + ((SAXParseException) e).getLineNumber() + ". " + e.getMessage());
				} catch (IOException e) {
					this.setError("Error!", e.getMessage(), e.getStackTrace().toString());
				}
			} 
			else setError("Error!", "No file was selected! Please choose an XSD file.", null);		
		} 
		else setError("Error!", "No file was selected! Please choose an XML file.", null);
	}

	@FXML
	protected void GenerateXML(ActionEvent event) {
		Document doc = null;
		try {
			doc = docBuilder.newDocument();

			Element root = doc.createElement("ufl_team");
			doc.appendChild(root);
			
			Element teamName = doc.createElement("team_name");
			teamName.setTextContent(this.nazovTimu.getText());
			Element email = doc.createElement("email");
			email.setTextContent(this.email.getText());
			Element phoneNum = doc.createElement("phone_number");
			phoneNum.setTextContent(this.telCislo.getText());
			
			Element players = getPlayersElem(doc);
			if(players == null)
				return;
			
			root.appendChild(teamName);
			root.appendChild(email);
			root.appendChild(phoneNum);
			root.appendChild(players);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource xmlSource = new DOMSource(doc);
			
			if(!checkEmail(this.email.getText())) {
				setError("Error!", "Wrong format!", null);
				return;
			}
			if(!checkNum(this.telCislo.getText())) {
				setError("Error!", "Wrong format of phone number!", "Allowed are 0900000000 or +421900000000!");
				return;
			}
			
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Save file as");
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
			
			File file = chooser.showSaveDialog(null);
			if(file != null) {
				StreamResult stream = new StreamResult(file);
				transformer.transform(xmlSource, stream);
			}
			else setError("Error!", "No file was selected for creation!", null);
			
		} catch (Exception e) {
			setError("Error!", e.getMessage(), e.getStackTrace().toString());
		}
	}
	
	@FXML
	protected void VisualizeXML(ActionEvent event) {
		
	}
}

/*
 * UNUSED
 * 
 * @FXML private void checkName() { nazovCheck =
 * this.checkString(this.nazovTimu.getText().toString());
 * 
 * if (nazovCheck == false) { this.setWarning("Pouzite nepovelene znaky",
 * "Skontroluj si nazov timu", "Napis nazov timu bez specialnych znakov");
 * return; }
 * 
 * }
 * 
 * @FXML private void checkEmail() { emailCheck =
 * this.checkEmail(this.email.getText().toString());
 * 
 * if (emailCheck == false) { this.setWarning("Email v zlom formate",
 * "Skontroluj si zadany email", "Napis email v spravnom formate"); return; } }
 */
