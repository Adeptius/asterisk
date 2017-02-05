package ua.adeptius.asterisk.javafx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;


public class ScriptController implements Initializable {

    private Site site;
    private Stage stage;

    public ScriptController(Site site, Stage stage) {
        this.site = site;
        this.stage = stage;
    }

    @FXML
    private Button btnClose;

    @FXML
    private TextArea textForScript;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnClose.setOnAction(event -> close());
        textForScript.setWrapText(true);
        textForScript.setText(Utils.getScriptForSite(site));
    }

    private void close(){
        stage.hide();
    }
}
