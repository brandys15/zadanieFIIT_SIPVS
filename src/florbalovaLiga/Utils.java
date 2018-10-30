package florbalovaLiga;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Semaphore;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

public class Utils {
	private static Utils instance = null;
	private static Semaphore sem = null;
	private static FileChooser fileChooser = null;
	private Utils() {}
	
	public static Utils getInstance() {
		if(Utils.instance == null) {
			Utils.instance = new Utils();
			Utils.sem = new Semaphore(0);
			Utils.fileChooser = new FileChooser();
			Utils.fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		}
		return Utils.instance;
	}
	
	public FileChooser getFileChooser() {
		return Utils.fileChooser;
	}
	public Semaphore getSemaphore() {
		return Utils.sem;
	}
	public static void setInformation(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	public static void setWarning(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	public static void setError(String title, String headerText, String contentText) {
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
	public static void setErrorPlain(String title, String headerText, String contentText) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		alert.showAndWait();
	}
	public static String getStackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		ex.printStackTrace(pw);
		
		return sw.toString();
	}	
}
