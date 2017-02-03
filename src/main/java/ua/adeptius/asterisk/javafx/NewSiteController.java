package ua.adeptius.asterisk.javafx;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NewSiteController implements Initializable{

    private GuiController guiController;
    private Stage stage;
    private String selectedSiteString;

    public NewSiteController(GuiController guiController, Stage stage, String selectedSiteString) {
        this.guiController = guiController;
        this.stage = stage;
        this.selectedSiteString = selectedSiteString;
    }

    @FXML
    private Button btnCancel;

    @FXML
    private TextField textName;

    @FXML
    private TextField textNumber;

    @FXML
    private Button btnSave;

    @FXML
    private TextField textGoogleId;

    @FXML
    private TextArea textBlackList;

    @FXML
    private TextField textEmail;

    @FXML
    private TextArea textPhones;

    @FXML
    private TextField textURL;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(e -> cancel());
        if (selectedSiteString != null){// если мы меняем сайт
            btnSave.setText("Изменить");
            textName.setEditable(false);

            Site site = MainController.getSiteByName(selectedSiteString);
            textName.setText(site.getName());
            textURL.setText(site.getAccessControlAllowOrigin());
            textEmail.setText(site.getMail());
            textNumber.setText(site.getStandartNumber());
            textGoogleId.setText(site.getGoogleAnalyticsTrackingId());

            String phones = "";
            for (Phone phone : site.getPhones()) {
                phones += phone.getNumber() + "\n";
            }
            if (phones.length() >0){
                phones = phones.substring(0, phones.length() -1);
            }

            String ips = "";
            for (String s : site.getBlackIps()) {
                ips += s +"\n";
            }
            if (ips.length() >0){
                ips = ips.substring(0, ips.length() -1);
            }

            textPhones.setText(phones);
            textBlackList.setText(ips);
        }

        btnSave.setOnAction(e -> save());
    }

    private void cancel(){
        stage.hide();
    }

    private void save(){
        String name = textName.getText().trim();
        String url = textURL.getText().trim();
        String email = textEmail.getText().trim();
        String standartNumber = textNumber.getText().trim();
        String googleId = textGoogleId.getText().trim();
        String phones = textPhones.getText().trim();
        String blackList = textBlackList.getText().trim();

        phones = phones.replaceAll(" ", "").replaceAll("\t","");
        blackList = blackList.replaceAll(" ", "").replaceAll("\t","");

        List<Phone> phoneList = new ArrayList<>();
        String[] phonesArr = phones.split("\n");
        for (String s : phonesArr) {
            phoneList.add(new Phone(s));
        }

        List<String> blackIps = new ArrayList<>();
        String[] blackIpsArr = blackList.split("\n");
        for (String s : blackIpsArr) {
            blackIps.add(s);
        }

        Site site = new Site(name,url, phoneList,standartNumber,googleId,email, blackIps);

        try {
            if (selectedSiteString != null){// если мы меняем сайт
                if (Main.mySqlDao.editSite(site)){
                    stage.hide();
                    MainController.sites.remove(MainController.getSiteByName(site.getName()));
                    MainController.sites.add(site);
                    guiController.setPhones(site.getName());
                }else {
                    System.out.println("Ошибка");
                }


            }else {// если мы добавляем сайт
                if (Main.mySqlDao.saveSite(site)){
                    List<String> l = new ArrayList<>();
                    l.add(site.getName());
                    Main.mySqlDao.createStatisticTables(l);
                    stage.hide();
                    MainController.sites.add(site);
                    guiController.addAndUpdateList(site.getName());
                }else {
                    System.out.println("Ошибка");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
