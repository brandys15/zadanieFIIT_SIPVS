package florbalovaLigaView;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javax.swing.JFileChooser;

import java.awt.Component;
import java.io.File;    
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException;
import java.io.IOException;
import javax.swing.JOptionPane;
public class MainItemsController {
	
	ObservableList<String> pohlavieList = FXCollections.observableArrayList("Muz","Zena");
	ObservableList<String> ligistaList = FXCollections.observableArrayList("Ano","Nie");
//informacie v tabulke 
//	private boolean nazovCheck;
//	private boolean checkNum;
//	private boolean emailCheck;
	@FXML 
	private TextField nazovTimu;
	@FXML 
	private TextField email;	
	@FXML 
	private TextField pocetHracov;
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
	}
	
    @FXML
    private void checkName() {
        nazovCheck = this.checkString(this.nazovTimu.getText().toString());
        
        if(nazovCheck == false) {
            this.setWarning("Pouzite nepovelene znaky", "Skontroluj si nazov timu", "Napis nazov timu bez specialnych znakov");
            return;
        }

    }  

    @FXML
    private void checkEmail() {
    	emailCheck = this.checkEmail(this.email.getText().toString());
        
        if(emailCheck == false) {
            this.setWarning("Email v zlom formate", "Skontroluj si zadany email", "Napis email v spravnom formate");
            return;
        }
    }  
    
    private boolean checkString(String text) {
        return text.matches("[a-zA-Z ]+");
    }
    
    private boolean checkEmail(String text) {
        return text.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}");
    }
    
    private boolean checkNum(String text) {
        return text.matches("[0-9]");
    }
    
    private void setWarning(String title, String headerText, String contentText) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }
    
    @FXML protected void ValidateXML(ActionEvent event) {
    	Component frame = null;
    	File xsdfile = null;
    	File xmlfile = null;
    	JFileChooser fileChooser = new JFileChooser();
    	fileChooser.setMultiSelectionEnabled(true);
    	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    	fileChooser.showOpenDialog(frame);
    	File[] files = fileChooser.getSelectedFiles();
    	if(files.length != 2) {
    		this.setWarning("Chybny pocet suborov", "Vyber 2 subory", "Len XML a XSD");
    		return;
    	}
    	for(int i=0; i<2; i++){
    		if (files[i].getName().toLowerCase().contains("xml")) {
        		xmlfile = files[i];
        	} else {
        		xsdfile = files[i];
        	}
    	}
    	if (xmlfile == null || xsdfile == null) {
    		this.setWarning("Chybny pocet suborov", "Vyber 2 subory", "Len XML a XSD");
    		return;
    	}
    	Source xmlFile = new StreamSource(xmlfile);
    	SchemaFactory schemaFactory = SchemaFactory
    	    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    	try {
    		Schema schema = schemaFactory.newSchema(xsdfile);
    	  	Validator validator = schema.newValidator();
    	  	validator.validate(xmlFile);
    	  	JOptionPane.showMessageDialog(frame,
    	  			xmlfile.getName() + " is valid",
    			    "System message",
    			    JOptionPane.PLAIN_MESSAGE);
    	} catch (SAXException e) {
    		JOptionPane.showMessageDialog(frame,
      			  xmlfile.getName() + " is invalid. Reason: Line " + ((SAXParseException) e).getLineNumber() + ". " + e.getMessage(),
      			    "System message",
      			    JOptionPane.PLAIN_MESSAGE);
    	} catch (IOException e) {}
    }

}
