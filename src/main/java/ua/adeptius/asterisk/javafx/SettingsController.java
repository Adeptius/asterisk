package ua.adeptius.asterisk.javafx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.Settings;
import ua.adeptius.asterisk.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;


public class SettingsController implements Initializable {

    private Stage stage;

    public SettingsController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private Button btnCancel;

    @FXML
    private TextField textUpdateRate;

    @FXML
    private Button btnSave;

    @FXML
    private TextField textCleanRate;

    @FXML
    private TextField textServerAdress;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(event -> close());
        btnSave.setOnAction(event -> save());
        textServerAdress.setText(Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT"));
        textUpdateRate.setText(Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));
        textCleanRate.setText(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"));

    }

    private void save(){
        String serverAdress = textServerAdress.getText();
        String updateRate = textUpdateRate.getText();
        String cleanRate = textCleanRate.getText();
        stage.hide();

        Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT", serverAdress);
        Settings.setSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE", updateRate);
        Settings.setSetting("SECONDS_TO_REMOVE_OLD_PHONES", cleanRate);


    }

    private void close(){
        stage.hide();
    }
}
