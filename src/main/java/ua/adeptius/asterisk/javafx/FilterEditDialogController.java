package ua.adeptius.asterisk.javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import ua.adeptius.asterisk.utils.Settings;

import java.net.URL;
import java.util.ResourceBundle;


public class FilterEditDialogController implements Initializable {

    @FXML
    private CheckBox MAIL_ANTISPAM;

    @FXML
    private CheckBox BLOCKED_BY_IP;

    @FXML
    private CheckBox INCOMING_CALL;

    @FXML
    private CheckBox ANSWER_CALL;

    @FXML
    private CheckBox REQUEST_NUMBER;

    @FXML
    private CheckBox ENDED_CALL;

    @FXML
    private CheckBox NUMBER_FREE;

    @FXML
    private CheckBox INCOMING_CALL_NOT_REGISTER;

    @FXML
    private CheckBox SENDING_NUMBER;

    @FXML
    private CheckBox NO_NUMBERS_LEFT;

    @FXML
    private CheckBox REPEATED_REQUEST;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MAIL_ANTISPAM.setSelected(Settings.getSettingBoolean("MAIL_ANTISPAM"));
        BLOCKED_BY_IP.setSelected(Settings.getSettingBoolean("BLOCKED_BY_IP"));
        INCOMING_CALL.setSelected(Settings.getSettingBoolean("INCOMING_CALL"));
        ANSWER_CALL.setSelected(Settings.getSettingBoolean("ANSWER_CALL"));
        REQUEST_NUMBER.setSelected(Settings.getSettingBoolean("REQUEST_NUMBER"));
        ENDED_CALL.setSelected(Settings.getSettingBoolean("ENDED_CALL"));
        NUMBER_FREE.setSelected(Settings.getSettingBoolean("NUMBER_FREE"));
        INCOMING_CALL_NOT_REGISTER.setSelected(Settings.getSettingBoolean("INCOMING_CALL_NOT_REGISTER"));
        SENDING_NUMBER.setSelected(Settings.getSettingBoolean("SENDING_NUMBER"));
        NO_NUMBERS_LEFT.setSelected(Settings.getSettingBoolean("NO_NUMBERS_LEFT"));
        REPEATED_REQUEST.setSelected(Settings.getSettingBoolean("REPEATED_REQUEST"));
    }

    public void actionClose(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();
    }


    public void checkBoxPressed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (!(source instanceof CheckBox)) {
            return;
        }

        CheckBox clickedCheckBox = (CheckBox) source;

//        Window parentWindow = ((Node) actionEvent.getSource()).getScene().getWindow();

        String id = clickedCheckBox.getId();
        boolean state = clickedCheckBox.isSelected();

        Settings.setSettingBoolean(id, state);
    }
}
