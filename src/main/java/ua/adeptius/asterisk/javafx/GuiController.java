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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.io.IOException;
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

    @FXML
    private Button btnSettings;

    private Parent fxmlEdit;
    private FXMLLoader fxmlLoader = new FXMLLoader();
    private FilterEditDialogController editDialogController;
    private Stage editDialogStage;

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

        try {
            fxmlLoader.setLocation(getClass().getResource("../../../../filteredit.fxml"));
            fxmlEdit = fxmlLoader.load();
            editDialogController = fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void actionButtonPressed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (!(source instanceof Button)) {
            return;
        }

        Button clickedButton = (Button) source;

        Window parentWindow = ((Node) actionEvent.getSource()).getScene().getWindow();

        if (clickedButton.getId().equals("btnSettings")){
            showDialog(parentWindow);
        }

    }

    private void showDialog(Window parentWindow) {

        if (editDialogStage==null) {
            editDialogStage = new Stage();
            editDialogStage.setTitle("Редактирование настроек");
            editDialogStage.setMinHeight(150);
            editDialogStage.setMinWidth(300);
            editDialogStage.setResizable(false);
            editDialogStage.setScene(new Scene(fxmlEdit));
            editDialogStage.initModality(Modality.WINDOW_MODAL);
            editDialogStage.initOwner(parentWindow);
        }

//      editDialogStage.showAndWait(); // для ожидания закрытия окна

        editDialogStage.show();

    }
}
