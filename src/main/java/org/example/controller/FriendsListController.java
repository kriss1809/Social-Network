package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.domain.Utilizator;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendsListController implements Observer<ChangeEvent>{

    Service service;
    String mesaj;
    List<String> prieteni;
    String username;


    @FXML
    private Button btn_trimitere_mesaje;
    @FXML
    private ListView<CheckBox> lista_prieteni;
    private ObservableList<CheckBox> checkBoxList;

    public void setService(Service service, String mesaj, List<String> prieteni, String curent_username) {
        this.service = service;
        service.addObserver(this);
        this.prieteni = prieteni;
        this.mesaj = mesaj;
        this.username = curent_username;

        checkBoxList = FXCollections.observableArrayList();
        for (String username : this.prieteni) {
            CheckBox checkBox = new CheckBox(username);
            checkBox.setSelected(false);
            checkBoxList.add(checkBox);
        }
        lista_prieteni.setItems(checkBoxList);
    }

    @Override
    public void update(ChangeEvent changeEvent, Service updated_service) {
        this.service = updated_service;

        checkBoxList.clear();
        for (String username : prieteni) {
            CheckBox checkBox = new CheckBox(username);
            checkBox.setSelected(false);
            checkBoxList.add(checkBox);
        }
    }

    @FXML
    private void handleUserPanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_trimitere_mesaje)
            gui_trimitere_mesaje();
    }

    private void gui_trimitere_mesaje() {
        Long raspuns = null;

        // creez o copie a listei ca sa evit ConcurrentModificationException
        List<CheckBox> checkBoxCopy = new ArrayList<>(checkBoxList);

        for (CheckBox checkBox : checkBoxCopy) {
            if (checkBox.isSelected()) {
                String username_prieten = checkBox.getText();

                Optional<Utilizator> prietenOptional = service.cautare_utilizator_username(username_prieten);

                if (prietenOptional.isPresent()) {
                    Utilizator prieten = prietenOptional.get();
                    Utilizator currentuser = service.cautare_utilizator_username(username).orElse(null);

                    if (currentuser != null) {
                        service.adaugare_mesaj(currentuser.getId(), prieten.getId(), mesaj, LocalDateTime.now(), raspuns);
                    }
                }
            }
        }

        Stage stage = (Stage) btn_trimitere_mesaje.getScene().getWindow();
        stage.close();
    }
}
