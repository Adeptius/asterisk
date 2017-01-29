package ua.adeptius.asterisk.javafx;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DeleteController implements Initializable{

    private String sitename;

    public DeleteController(String sitename) {
        this.sitename = sitename;
    }


    @FXML
    private Label label;

    @FXML
    private Button btn;


    private void delete() {
        System.out.println("DELETE!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.setText(sitename);
        btn.setOnAction(event ->  delete() );
    }
}
