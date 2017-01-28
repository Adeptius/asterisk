package ua.adeptius.asterisk.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuiController implements Initializable{

    ObservableList<Phone> phones;
    ObservableList<String> sites;

    @FXML
    private ListView<String> siteList;

    @FXML
    private TableView<Phone> phoneTable;

    @FXML
    private TextArea logArea;

    @FXML
    private TableColumn<Phone, String> phoneNumber;

    @FXML
    private TableColumn<Phone, String> phoneGoogleId;

    @FXML
    private TableColumn<Phone, String> phoneTime;

    @FXML
    private TableColumn<Phone, String> phoneIp;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> sitesNames = MainController.sites.stream().map(Site::getName).collect(Collectors.toList());
        sites = FXCollections.observableArrayList(sitesNames);
        siteList.setItems(sites);
        siteList.setOnMouseClicked(event -> {
            String siteSelected = siteList.getSelectionModel().getSelectedItem();
            List<Phone> newList = MainController.getSiteByName(siteSelected).getPhones();
            phones = FXCollections.observableArrayList(newList);
            phoneTable.setItems(phones);
        });
        setPhones("e404");
        siteList.getSelectionModel().select(1);


        phoneNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        phoneGoogleId.setCellValueFactory(new PropertyValueFactory<>("googleId"));
        phoneTime.setCellValueFactory(new PropertyValueFactory<>("busyTime"));
        phoneIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
    }

    private void setPhones(String sitename){
        Site site = MainController.getSiteByName(sitename);
        phones = FXCollections.observableArrayList(site.getPhones());
        phoneTable.setItems(phones);

    }


    public void appendLog(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }
}
