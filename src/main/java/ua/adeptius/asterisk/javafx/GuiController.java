package ua.adeptius.asterisk.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GuiController implements Initializable {

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

    @FXML
    private Button btnSettings;

//    private Parent fxmlEdit;
//    private FXMLLoader fxmlLoader = new FXMLLoader();
//    private FilterEditDialogController editDialogController;
//    private Stage editDialogStage;
    public static String selectedSiteString;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> sitesNames = MainController.sites.stream().map(Site::getName).collect(Collectors.toList());
        sites = FXCollections.observableArrayList(sitesNames);
        siteList.setItems(sites);
        siteList.setOnMouseClicked(event -> {
            String siteSelected = siteList.getSelectionModel().getSelectedItem();
            setPhones(siteSelected);
        });
        setPhones("e404");
        siteList.getSelectionModel().select(1);

        phoneNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        phoneGoogleId.setCellValueFactory(new PropertyValueFactory<>("googleId"));
        phoneTime.setCellValueFactory(new PropertyValueFactory<>("busyTime"));
        phoneIp.setCellValueFactory(new PropertyValueFactory<>("ip"));

//        try {
//            fxmlLoader.setLocation(getClass().getResource("../../../../filteredit.fxml"));
//            fxmlEdit = fxmlLoader.load();
//            editDialogController = fxmlLoader.getController();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void setPhones(String sitename) {
        Site site = MainController.getSiteByName(sitename);
        phones = FXCollections.observableArrayList(site.getPhones());
        phoneTable.setItems(phones);
        selectedSiteString = sitename;
    }


    public void appendLog(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    public void actionButtonPressed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (!(source instanceof Button)) {
            return;
        }
        Button clickedButton = (Button) source;
        Window parentWindow = ((Node) actionEvent.getSource()).getScene().getWindow();
        if (clickedButton.getId().equals("btnSettings")) {
            showFilters();
        } else if (clickedButton.getId().equals("btnDelete")) {
            showDelete();
        }
    }

    private void showDelete() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../dbdelete.fxml"));
            loader.setController(new DeleteController(selectedSiteString));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Удаление сайта");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL); // Перекрывающее окно
            stage.initOwner(siteList.getScene().getWindow()); // Указание кого оно перекрывает
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showFilters() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../filteredit.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Настройка фильтров");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL); // Перекрывающее окно
            stage.initOwner(siteList.getScene().getWindow()); // Указание кого оно перекрывает
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
