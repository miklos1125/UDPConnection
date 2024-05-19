package udpconnection;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class FXMLDocumentController implements Initializable {
    
    Connector connector;
    private boolean connectionIsSET = false;
    
    @FXML
    private TextField nameField;
    @FXML
    private TextField messageField;
    @FXML
    private TextFlow textBox;
    @FXML
    private Button startButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button sendButton;
    
    @FXML
    private void handleServerButtonAction(ActionEvent event) {
        if(nameField.getText().trim().isEmpty()) return;
        connector.setConnector(nameField.getText(), Connector.Mode.SERVERSETUP, this);
        connector.start();
        nameField.setEditable(false);
        connectButton.setDisable(true);
        startButton.setDisable(true);
        
    }    
    
    @FXML
    private void handleConnectButtonAction(ActionEvent event) {
        if(nameField.getText().trim().isEmpty()) return;
        connector.setConnector(nameField.getText(), Connector.Mode.CLIENTSETUP, this);
        connector.start();
        nameField.setEditable(false);
        connectButton.setDisable(true);
        startButton.setDisable(true);

    }
    
    @FXML
    private void sendButtonAction(ActionEvent event) {
        System.out.println(connectionIsSET);
        if(nameField.getText().trim().isEmpty() 
                || messageField.getText().trim().isEmpty()) return;
        if (connector!=null && connectionIsSET){
            String toAdd = messageField.getText();
            messageField.setText("");
            Text lineBreak = new Text("\n");
            Text text = new Text(nameField.getText() + ": " + toAdd);
            text.setFill(Color.GREEN);
            text.setFont(Font.font("System", FontPosture.REGULAR, 12));
            textBox.getChildren().add(text);
            textBox.getChildren().add(lineBreak);
            connector.send(toAdd);
        }
    }
    
    public void setConnector(Connector c){
        this.connector = c;
    }
    
    public void closeConnection(){
        connector.close();
        connectionIsSET = false;
    }
    
    public void showMessage(Text t){
        Text lineBreak = new Text("\n");
        textBox.getChildren().add(t);
        textBox.getChildren().add(lineBreak);
    }
    
    public void watchConnection(boolean isConnected){
        System.out.println("watch: " +isConnected);
        connectionIsSET = isConnected;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
}
