package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.domain.Invitatie;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.events.FriendshipStatusType;
import org.example.utils.observer.Observer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserController implements Observer<ChangeEvent> {

    Service service;
    String username;

    @FXML
    private Label label_username;
    @FXML
    private TableView<Map.Entry<Utilizator, Invitatie>> tbl_prietenii;
    @FXML
    private TableColumn<Map.Entry<Utilizator, Invitatie>, String> col_username;
    @FXML
    private TableColumn<Map.Entry<Utilizator, Invitatie>, String> col_status;
    public ObservableList<Map.Entry<Utilizator, Invitatie>> friendsModel = FXCollections.observableArrayList();
    private Map<Utilizator, Invitatie> friendshipMap = new HashMap<>();

    @FXML
    private TableView<Utilizator> tbl_invitatii;
    @FXML
    private TableColumn<Utilizator, String> col_username_invitat;
    @FXML
    private TableColumn<Utilizator, String> col_invita;
    public ObservableList<Utilizator> invitationsModel = FXCollections.observableArrayList();

    public void setService(Service service, String username) {
        this.service = service;
        service.addObserver(this);
        this.label_username.setText("Esti autentificat ca: " + username);
        this.username = username;
        initFriendsModel();
        initInvitationsModel();
    }

    @Override
    public void update(ChangeEvent changeEvent) {
        initFriendsModel();
        initInvitationsModel();
    }

    @FXML
    public void initialize() {
        initializeTableFriends();
        initializeTableInvitations();
    }
    private void initializeTableFriends() {
        col_username.setCellValueFactory(param -> {
            Utilizator utilizator = param.getValue().getKey();
            return new SimpleStringProperty(utilizator != null ? utilizator.getUsername() : "");
        });

        col_status.setCellValueFactory(param -> {
            Invitatie invitatie = param.getValue().getValue();
            return new SimpleStringProperty(invitatie != null ? invitatie.getStatus() : "Not friends");
        });

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
            Utilizator selectedItem = friendsModel.get(index).getKey();
            service.modificare_invitatie(selectedItem.getId(), currentuser.get().getId(), FriendshipStatusType.accepted);
            initFriendsModel();
        }
    }

    private void handleRejectButton(int index) {
        Optional<Utilizator> currentuser = service.cautare_utilizator_username(username);
        if (index >= 0 && index < friendsModel.size()) {
            Utilizator selectedItem = friendsModel.get(index).getKey();
            service.modificare_invitatie(selectedItem.getId(), currentuser.get().getId(), FriendshipStatusType.rejected);
            initFriendsModel();
        }
    }

    private void initFriendsModel() {

        Optional<Utilizator> utilizatorOptional = service.cautare_utilizator_username(username);

        if (utilizatorOptional.isPresent()) {
            Utilizator currentUser = utilizatorOptional.get();

            Iterable<Invitatie> invitatii = service.cautare_invitatie(currentUser.getId());
            List<Invitatie> invList = StreamSupport.stream(invitatii.spliterator(), false).collect(Collectors.toList());

            for (Invitatie invitatie : invList) {
                Utilizator friend = service.cautare_utilizator(invitatie.getId1()).orElse(null);
                if (friend != null && invitatie.getStatus().equals(FriendshipStatusType.pending)) {
                    friendshipMap.put(friend, invitatie);
                }
                else
                    friendshipMap.put(friend, null);
            }
            List<Map.Entry<Utilizator, Invitatie>> entries = new ArrayList<>(friendshipMap.entrySet());
            friendsModel.setAll(entries);
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

}
