package ua.adeptius.asterisk.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Site;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuiController implements Initializable{

    @FXML
    private ListView<String> siteList;

    @FXML
    private TableView phoneTable;

    @FXML
    private TextArea logArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> sitesNames = MainController.sites.stream().map(Site::getName).collect(Collectors.toList());
        ObservableList<String> sites = FXCollections.observableArrayList(sitesNames);
        siteList.setItems(sites);
    }

    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }
}
