package ua.adeptius.asterisk.javafx;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Site;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeleteController implements Initializable{

    private String sitename;
    private Stage stage;
    private GuiController guiController;


    public DeleteController(GuiController guiController, Stage stage, String sitename) {
        this.sitename = sitename;
        this.stage = stage;
        this.guiController = guiController;
    }

    @FXML
    private Label label;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnDelete;


    private void delete() {
        try{
            if (Main.mySqlDao.deleteSite(sitename)){
                List<String> l = new ArrayList<>();
                l.add("statistic_"+sitename);
                Main.mySqlDao.deleteTables(l);
                stage.hide();
                Site site = MainController.getSiteByName(sitename);
                MainController.sites.remove(site);
                guiController.removeAndUpdateList(sitename);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.setText("Внимание!\nСайт " + sitename + "\nбудет удалён!");
        btnDelete.setOnAction(event ->  delete());
        btnCancel.setOnAction(event ->  cancel());
        btnCancel.setFocusTraversable(true);
    }



    private void cancel(){
        stage.hide();
    }
}
