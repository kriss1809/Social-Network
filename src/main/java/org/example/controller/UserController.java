package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.domain.validators.ValidationException;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.events.FriendshipStatusType;
import org.example.utils.observer.Observable;
import org.example.utils.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserController implements Observer<ChangeEvent> {

    Service service;
    String username;

    @FXML
    private Label label_username;
    @FXML
    private TableView<Utilizator> tbl_prietenii;
    @FXML
    private TableColumn<Utilizator, String> col_username;
    @FXML
    private TableColumn<Utilizator, String> col_status;
    public ObservableList<Utilizator> friendsModel = FXCollections.observableArrayList();

    @FXML
    private TableView<Utilizator> tbl_invitatii;
    @FXML
    private TableColumn<Utilizator, String> col_username_invitat;
    @FXML
    private TableColumn<Utilizator, String> col_invita;
    public ObservableList<Utilizator> invitationsModel = FXCollections.observableArrayList();

    @FXML
    private Button btn_deschidere_conversatie;
    @FXML
    private ComboBox cb_prieteni;
    @FXML
    private TextArea text_area_mesaje;
    @FXML
    private Button btn_trimite_mesaj;
    @FXML
    private TextArea input_mesaj;
    @FXML
    private TextField input_id_mesaj_raspuns;
    @FXML
    private Button btn_trimite_mesaje;


    public void setService(Service service, String username) {
        this.service = service;
        service.addObserver(this);
        this.label_username.setText("Bun venit, " + username + "!");
        this.username = username;
        initFriendsModel();
        initInvitationsModel();
        ObservableList<String> prieteni = FXCollections.observableArrayList(getFriendsForUser(username));
        cb_prieteni.setItems(prieteni);
        text_area_mesaje.setEditable(false);
    }

    @Override
    public void update(ChangeEvent changeEvent, Service updated_service) {
        this.service = updated_service;
        initFriendsModel();
        initInvitationsModel();
        ObservableList<String> prieteni = FXCollections.observableArrayList(getFriendsForUser(username));
        cb_prieteni.setItems(prieteni);
        gui_deschidere_conversatie();
    }
    @FXML
    public void initialize() {
        initializeTableFriends();
        initializeTableInvitations();
    }
    private void initializeTableFriends() {
        col_username.setCellValueFactory(new PropertyValueFactory<>("username"));

        col_status.setCellValueFactory(new PropertyValueFactory<>("username"));

        col_status.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");
            private final Button rejectButton = new Button("Reject");
            {
                acceptButton.setStyle("-fx-background-color: #00FF00;");
                acceptButton.setOnAction(event -> handleAcceptButton(getIndex()));
                rejectButton.setStyle("-fx-background-color: #FF0000;");
                rejectButton.setOnAction(event -> handleRejectButton(getIndex()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox buttonBox = new HBox(5, acceptButton, rejectButton);
                    buttonBox.setAlignment(Pos.CENTER); // Center the buttons horizontally
                    setGraphic(buttonBox);
                }
            }
        });

        tbl_prietenii.setItems(friendsModel);
    }

    private void handleAcceptButton(int index) {
        Optional<Utilizator> currentuser = service.cautare_utilizator_username(username);
        if (index >= 0 && index < friendsModel.size()) {
            Utilizator selectedItem = friendsModel.get(index);
            service.modificare_invitatie(selectedItem.getId(), currentuser.get().getId(), FriendshipStatusType.accepted);
            initFriendsModel();
            initInvitationsModel();
            ObservableList<String> prieteni = FXCollections.observableArrayList(getFriendsForUser(username));
            cb_prieteni.setItems(prieteni);
        }
    }

    private void handleRejectButton(int index) {
        Optional<Utilizator> currentuser = service.cautare_utilizator_username(username);
        if (index >= 0 && index < friendsModel.size()) {
            Utilizator selectedItem = friendsModel.get(index);
            service.modificare_invitatie(selectedItem.getId(), currentuser.get().getId(), FriendshipStatusType.rejected);
            initFriendsModel();
            initInvitationsModel();
            ObservableList<String> prieteni = FXCollections.observableArrayList(getFriendsForUser(username));
            cb_prieteni.setItems(prieteni);
        }
    }

    private void initFriendsModel() {
        Optional<Utilizator> utilizatorOptional = service.cautare_utilizator_username(username);
        if (utilizatorOptional.isPresent()) {
            Utilizator currentUser = utilizatorOptional.get();

            Iterable<Utilizator> utilizatori = service.cautare_utilizatori_pending(currentUser.getId());
            List<Utilizator> List = StreamSupport.stream(utilizatori.spliterator(),false).collect(Collectors.toList());
            friendsModel.setAll(List);
        }
    }

    private void initializeTableInvitations() {
        col_username_invitat.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_invita.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_invita.setCellFactory(param -> new TableCell<>() {
            private final Button inviteButton = new Button("Invite");
            {
                inviteButton.setOnAction(event -> handleInviteButton(getIndex()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox buttonBox = new HBox(5, inviteButton);
                    buttonBox.setAlignment(Pos.CENTER);
                    setGraphic(buttonBox);
                }
            }
        });

        tbl_invitatii.setItems(invitationsModel);
    }
    private void handleInviteButton(int index) {
        Optional<Utilizator> currentuser = service.cautare_utilizator_username(username);
        if (index >= 0 && index < invitationsModel.size()) {
            Utilizator selectedItem = invitationsModel.get(index);
            service.adaugare_invitatie(currentuser.get().getId(), selectedItem.getId(), FriendshipStatusType.pending);
            initInvitationsModel();
            initFriendsModel();
            ObservableList<String> prieteni = FXCollections.observableArrayList(getFriendsForUser(username));
            cb_prieteni.setItems(prieteni);
        }
    }

    private void initInvitationsModel() {
        Optional<Utilizator> utilizatorOptional = service.cautare_utilizator_username(username);
        if (utilizatorOptional.isPresent()) {
            Utilizator currentUser = utilizatorOptional.get();

            Iterable<Utilizator> utilizatori = service.cautare_utilizatori_neinvitati(currentUser.getId());
            List<Utilizator> List = StreamSupport.stream(utilizatori.spliterator(),false).collect(Collectors.toList());
            invitationsModel.setAll(List);
        }
    }

    private List<String> getFriendsForUser(String username)
    {
        Optional<Utilizator> utilizatorOptional = service.cautare_utilizator_username(username);
        Utilizator currentUser = utilizatorOptional.get();
        Iterable<String> lista = service.getFriendsForUser(currentUser.getId());
        return StreamSupport.stream(lista.spliterator(),false).collect(Collectors.toList());
    }

    @FXML
    private void handleMessagePanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_deschidere_conversatie)
            gui_deschidere_conversatie();
        if(event.getSource()==btn_trimite_mesaj)
            gui_adaugare_mesaj();
        if(event.getSource()==btn_trimite_mesaje)
            gui_afisare_prieteni_trimitere_mesaj();
    }

    private void gui_deschidere_conversatie()
    {
        String username_prieten;
        Object obj_username_prieten = cb_prieteni.getValue();
        if(obj_username_prieten != null)
        {
            username_prieten = obj_username_prieten.toString();
            Optional<Utilizator> prietenoptional = service.cautare_utilizator_username(username_prieten);

            Utilizator prieten = prietenoptional.get();
            Utilizator currentuser = service.cautare_utilizator_username(username).get();
            Iterable<Message> conversatie = service.afisare_lista_mesaje(currentuser.getId(), prieten.getId());
            List<Message> lista_mesaje = StreamSupport.stream(conversatie.spliterator(),false).collect(Collectors.toList());
            String mesaje = "";
            for(int i=0; i< lista_mesaje.size(); i++) {

                Long id =  lista_mesaje.get(i).getFrom();
                String from = service.cautare_utilizator(id).get().getUsername();

                mesaje += "(id mesaj: " + lista_mesaje.get(i).getId() + ") " + from + ": " + lista_mesaje.get(i).getMessage();

                Long id_reply = lista_mesaje.get(i).getReply();
                if(id_reply!=-1) {
                    Message reply_message = service.cautare_mesaj(id_reply).get();
                    mesaje += " {replied: " + reply_message.getMessage() + "}";
                }
                mesaje += "\n";
            }
            text_area_mesaje.setText(mesaje);
        }

    }

    private void  gui_adaugare_mesaj()
    {
        String text = input_mesaj.getText();
        Long raspuns = null;
        String string_id_mesaj = input_id_mesaj_raspuns.getText();
        try
        {
            raspuns = Long.parseLong(string_id_mesaj);

            Object obj_username_prieten = cb_prieteni.getValue();
            if(obj_username_prieten!=null) {
                String username_prieten = obj_username_prieten.toString();
                if (username_prieten != null) {
                    Optional<Utilizator> prietenoptional = service.cautare_utilizator_username(username_prieten);
                    if (prietenoptional.isPresent()) {
                        Utilizator prieten = prietenoptional.get();
                        Utilizator currentuser = service.cautare_utilizator_username(username).get();
                        if (raspuns != null) {
                            boolean verificare = verifica_id_mesaj(raspuns, currentuser, prieten);
                            if (verificare == true)
                                service.adaugare_mesaj(currentuser.getId(), prieten.getId(), text, LocalDateTime.now(), raspuns);
                        } else
                            service.adaugare_mesaj(currentuser.getId(), prieten.getId(), text, LocalDateTime.now(), raspuns);
                        gui_deschidere_conversatie();
                    }
                }
            }
            else
                MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Eroare","Nu ati deschis nicio conversatie");


        }
        catch (NumberFormatException e)
        {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Eroare", "Nu ati introdus un numar valid.");
        }

        input_mesaj.setText("");
        input_id_mesaj_raspuns.setText("");
    }

    private boolean verifica_id_mesaj(Long id, Utilizator curent, Utilizator prieten)
    {
        try {
            Message m = service.cautare_mesaj(id).get();
            if((m.getTo() == curent.getId() && m.getFrom() == prieten.getId()) || (m.getTo()==prieten.getId() && m.getFrom()==curent.getId()))
                return true;
            else
            {
                MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Eroare","Nu exista mesajul cu id dat in conversatie");
                return false;
            }
        }
        catch (ValidationException e)
        {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Mesajul nu exista",e.getMessage());
            return false;
        }
    }

    private void gui_afisare_prieteni_trimitere_mesaj() {
        String mesaj = input_mesaj.getText();
        List<String> lista_prieteni = getFriendsForUser(username);
        if (lista_prieteni.isEmpty())
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Eroare", "Nu aveti niciun prieten!");
        else if (mesaj.equals(""))
            MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Eroare","Nu ati introdus niciun mesaj");
        else {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/org/example/views/friends-list-view.fxml"));
                Parent friendsListView = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Selecteaza prieteni");
                stage.setScene(new Scene(friendsListView));

                FriendsListController ctrl = loader.getController();
                ctrl.setService(service, mesaj, lista_prieteni, username);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        input_mesaj.setText("");
    }

}
