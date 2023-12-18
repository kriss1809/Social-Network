package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.domain.*;
import org.example.domain.validators.ValidationException;
import org.example.repository.DataBaseRepository.Page;
import org.example.repository.DataBaseRepository.Pageable;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.events.FriendshipStatusType;
import org.example.utils.observer.Observer;

/*  !!! Daca nu fac import manual fisierele cu Observer/Observable, se va incerca folosirea Observerului din JavaFX   */

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StartController implements Observer<ChangeEvent> {

    Service service;

    // tabel users
    @FXML
    TableView<Utilizator> usersTbl;
    @FXML
    TableColumn<Utilizator, Long> id_utilizator;
    @FXML
    TableColumn<Utilizator, String> prenume;
    @FXML
    TableColumn<Utilizator, String> nume;
    @FXML
    TableColumn<Utilizator, String> username;
    ObservableList<Utilizator> usersModel = FXCollections.observableArrayList();

    // tabel prietenii
    @FXML
    TableView<Prietenie> friendshipsTbl;
    @FXML
    TableColumn<Prietenie, String> user1;
    @FXML
    TableColumn<Prietenie, String> user2;
    @FXML
    TableColumn<Prietenie, String> data;
    public ObservableList<Prietenie> friendshipsModel = FXCollections.observableArrayList();

    // tabel invitatii
    @FXML
    TableView<Invitatie> tblInvitatii;
    @FXML
    TableColumn<Invitatie, Long> tbl_invitatii_user1;
    @FXML
    TableColumn<Invitatie, Long> tbl_invitatii_user2;
    @FXML
    TableColumn<Invitatie, String> tbl_invitatii_status;
    public ObservableList<Invitatie> invitationsModel = FXCollections.observableArrayList();

    // butoane
    @FXML
    private Button adaugare_user;
    @FXML
    private Button stergere_user;
    @FXML
    private Button modificare_user;
    @FXML
    private Button cautare_user;
    @FXML
    private Button adaugare_prietenie;
    @FXML
    private Button modificare_invitatie;
    @FXML
    private Button stergere_prietenie;
    @FXML
    private Button nr_comunitati;
    @FXML
    private Button comunitate_sociabila;
    @FXML
    private Button minimN;
    @FXML
    private Button prieteni_luna;
    @FXML
    private Button user_string;

    // text field-uri
    @FXML
    private TextField input_username;
    @FXML
    private TextField input_prenume;
    @FXML
    private TextField input_nume;
    @FXML
    private TextField input_prieten1;
    @FXML
    private TextField input_prieten2;
    @FXML
    private ComboBox combo_status;
    ObservableList<FriendshipStatusType> statusuri = FXCollections.observableArrayList(FriendshipStatusType.pending, FriendshipStatusType.accepted, FriendshipStatusType.rejected);
    @FXML
    private TextField input_N;
    @FXML
    private TextField input_id_luna;
    @FXML
    private TextField input_luna;
    @FXML
    private TextField input_string;

    // pentru paginare Utilizatori
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalNumberOfElements = 0;
    @FXML
    private Label pageNumber;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private ComboBox cb_pagesize;
    ObservableList<Integer> elemente = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    @FXML
    private Button btn_pagesize;

    // paginare Prietenii
    private int currentPage2 = 0;
    private int totalNumberOfElements2 = 0;
    @FXML
    private Label pageNumber2;
    @FXML
    private Button nextButton2;
    @FXML
    private Button previousButton2;

    // paginare invitatii
    private int currentPage3 = 0;
    private int totalNumberOfElements3 = 0;
    @FXML
    private Label pageNumber3;
    @FXML
    private Button nextButton3;
    @FXML
    private Button previousButton3;

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);
        //initUsersModel();
        initUsersModelPage();
        //initFriendshipsModel();
        initFriendshipsModelPage();
        //initInvitationsModel();
        initInvitationsModelPage();
    }
    @FXML
    public void initialize()
    {
        initializeTableUsers();
        initializeTableFriendships();
        initializeTableInvitations();
        combo_status.setValue(FriendshipStatusType.pending);
        combo_status.setItems(statusuri);
        cb_pagesize.setItems(elemente);
        cb_pagesize.setValue(10);
    }

    private void initializeTableUsers()
    {
        id_utilizator.setCellValueFactory(new PropertyValueFactory<Utilizator,Long>("id"));
        prenume.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("firstName"));
        nume.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("lastName"));
        username.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("username"));
        usersTbl.setItems(usersModel);
    }

    private void initializeTableFriendships()
    {
        user1.setCellValueFactory(cellData1 -> new SimpleStringProperty(((Prietenie)cellData1.getValue()).getUser1().getFullName()));
        user2.setCellValueFactory(cellData2 -> new SimpleStringProperty(((Prietenie)cellData2.getValue()).getUser2().getFullName()));
        data.setCellValueFactory(new PropertyValueFactory<Prietenie,String>("date"));
        friendshipsTbl.setItems(friendshipsModel);
    }

    private void initializeTableInvitations()
    {
        tbl_invitatii_user1.setCellValueFactory(new PropertyValueFactory<Invitatie,Long>("id1"));
        tbl_invitatii_user2.setCellValueFactory(new PropertyValueFactory<Invitatie,Long>("id2"));
        tbl_invitatii_status.setCellValueFactory(new PropertyValueFactory<Invitatie,String>("status"));
        tblInvitatii.setItems(invitationsModel);
    }

    private void initUsersModel() {
        Iterable<Utilizator> users=service.getAllUsers();
        List<Utilizator> userList= StreamSupport.stream(users.spliterator(),false).collect(Collectors.toList());
        usersModel.setAll(userList);
    }

    private void initUsersModelPage()
    {
        Page<Utilizator> page = service.getAllUsersPage(new Pageable(currentPage, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElementCount() / pageSize ) - 1;
        if(currentPage > maxPage) {
            currentPage = maxPage;
            page = service.getAllUsersPage(new Pageable(currentPage, pageSize));
        }

        usersModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfElements = page.getTotalElementCount();

        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage+1)*pageSize >= totalNumberOfElements);

        pageNumber.setText("Page " + (currentPage+1) +"/" + (maxPage+1));
    }

    public void onPrevious(ActionEvent actionEvent) {
        currentPage--;
        initUsersModelPage();
    }

    public void onNext(ActionEvent actionEvent) {
        currentPage++;
        initUsersModelPage();
    }

    private void initFriendshipsModel() {
        Iterable<Prietenie> friendships = service.getAllFriendships();
        List<Prietenie> friendshipList = StreamSupport.stream(friendships.spliterator(),false).collect(Collectors.toList());
        friendshipsModel.setAll(friendshipList);
    }

    private void initFriendshipsModelPage()
    {
        Page<Prietenie> page = service.getAllFriendshipsPage(new Pageable(currentPage2, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElementCount() / pageSize ) - 1;
        if(currentPage2 > maxPage) {
            currentPage2 = maxPage;
            page = service.getAllFriendshipsPage(new Pageable(currentPage2, pageSize));
        }

        friendshipsModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfElements2 = page.getTotalElementCount();

        previousButton2.setDisable(currentPage2 == 0);
        nextButton2.setDisable((currentPage2+1)*pageSize >= totalNumberOfElements2);

        pageNumber2.setText("Page " + (currentPage2+1) +"/" + (maxPage+1));
    }
    public void onPrevious2(ActionEvent actionEvent) {
        currentPage2--;
        initFriendshipsModelPage();
    }

    public void onNext2(ActionEvent actionEvent) {
        currentPage2++;
        initFriendshipsModelPage();
    }

    private void initInvitationsModel()
    {
        Iterable<Invitatie> invitatii = service.getAllInvitations();
        List<Invitatie> lista = StreamSupport.stream(invitatii.spliterator(),false).collect(Collectors.toList());
        invitationsModel.setAll(lista);
    }

    private void initInvitationsModelPage()
    {
        Page<Invitatie> page = service.getAllInvitationsPage(new Pageable(currentPage3, pageSize));

        int maxPage = (int) Math.ceil((double) page.getTotalElementCount() / pageSize ) - 1;
        if(currentPage3 > maxPage) {
            currentPage3 = maxPage;
            page = service.getAllInvitationsPage(new Pageable(currentPage3, pageSize));
        }

        invitationsModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfElements3 = page.getTotalElementCount();

        previousButton3.setDisable(currentPage3 == 0);
        nextButton3.setDisable((currentPage3+1)*pageSize >= totalNumberOfElements3);

        pageNumber3.setText("Page " + (currentPage3+1) +"/" + (maxPage+1));
    }
    public void onPrevious3(ActionEvent actionEvent) {
        currentPage3--;
        initInvitationsModelPage();
    }

    public void onNext3(ActionEvent actionEvent) {
        currentPage3++;
        initInvitationsModelPage();
    }

    @Override
    public void update(ChangeEvent ChangeEvent, Service updated_service) {
        this.service = updated_service;
        //initUsersModel();
        initUsersModelPage();
        //initFriendshipsModel();
        initFriendshipsModelPage();
        //initInvitationsModel();
        initInvitationsModelPage();
    }


    private Utilizator getSelectedUser()
    {
        Utilizator selected = (Utilizator) usersTbl.getSelectionModel().getSelectedItem();
        return selected;
    }

    private Prietenie getSelectedFriendship()
    {
        Prietenie selected = (Prietenie) friendshipsTbl.getSelectionModel().getSelectedItem();
        return selected;
    }

    @FXML
    private void handleUserPanelClicks(ActionEvent event)
    {
        if(event.getSource()==stergere_user)
            gui_stergere_user(getSelectedUser());
        else if(event.getSource()==adaugare_user)
            gui_adaugare_user();
        else if(event.getSource()==modificare_user)
            gui_modificare_user(getSelectedUser());
        else if(event.getSource()==cautare_user)
            gui_cautare_user();
        else if(event.getSource()==adaugare_prietenie)
            gui_adaugare_invitatie();
        else if(event.getSource()==modificare_invitatie)
            gui_modificare_invitatie();
        else if(event.getSource()==stergere_prietenie)
            gui_stergere_prietenie(getSelectedFriendship());
        else if(event.getSource()==nr_comunitati)
            gui_nr_comunitati();
        else if(event.getSource()==comunitate_sociabila)
            gui_comunitate_sociabila();
        else if(event.getSource()==minimN)
            gui_listaNprieteni();
        else if(event.getSource()==prieteni_luna)
            gui_afisare_prieteni_luna();
        else if(event.getSource()==user_string)
            gui_afisare_utilizatori_string();
        else if(event.getSource()==btn_pagesize)
            gui_incarcare_pagina();
    }

    private void gui_incarcare_pagina()
    {
        pageSize = Integer.parseInt(cb_pagesize.getValue().toString());
        initUsersModelPage();
        initFriendshipsModelPage();
        initInvitationsModelPage();
    }

    private void gui_adaugare_user()
    {
        String username = input_username.getText();
        String FirstName=input_prenume.getText();
        String LastName=input_nume.getText();
        Utilizator u = new Utilizator(FirstName,LastName, username);
        try {
            service.adaugare_utilizator(u.getFirstName(),u.getLastName(), u.getUsername());
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Adaugare","utilizatorul a fost salvat cu succes");
        } catch (ValidationException e) {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Adaugarea a esuat",e.getMessage());
        }

        input_nume.setText("");
        input_prenume.setText("");
        input_username.setText("");

//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();
    }

    private void gui_modificare_user(Utilizator selected)
    {
        String username = input_username.getText();
        String FirstName = input_prenume.getText();
        String LastName = input_nume.getText();
        try {
            service.modificare_utilizator(FirstName, LastName, username);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Modificare", "Utilizatorul a fost modificat cu succes.");
        } catch (ValidationException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Modificarea a esuat", e.getMessage());
        }
        input_nume.setText("");
        input_prenume.setText("");
        input_username.setText("");
//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();

    }

    private void gui_stergere_user(Utilizator selected)
    {
        if(selected!=null)
        {
            try {
                service.stergere_utilizator(selected.getId());
                MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Stergere","Utilizatorul a fost sters cu succes.");
            }
            catch (ValidationException ex) {
                MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Stergerea a esuat",ex.getMessage());
            }
        }
        else MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Stergerea a esuat","Nu ati selectat niciun utilizator!");
//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();
    }

    private void gui_stergere_prietenie(Prietenie selected)
    {
        if(selected!=null)
        {
            try {
                service.stergere_prieten(selected.getId().getLeft(), selected.getId().getRight());
                MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Stergere","Prietenia a fost stearsa cu succes.");
            }
            catch (ValidationException ex) {
                MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Stergerea a esuat",ex.getMessage());
            }
        }
        else MessageAlert.showMessage(null, Alert.AlertType.ERROR,"Stergerea a esuat","Nu ati selectat nicio prietenie!");
//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();
    }

    private void  gui_cautare_user()
    {
        String username = input_username.getText();
        try
        {
            Optional<Utilizator> u = service.cautare_utilizator_username(username);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Cautare",u.get().toString());
        }
        catch (ValidationException ex) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Cautarea a esuat", ex.getMessage());
        }
        input_username.setText("");
    }

    private void  gui_adaugare_invitatie()
    {
        String idString1 = input_prieten1.getText();
        Long id1 = Long.parseLong(idString1);
        String idString2 = input_prieten2.getText();
        Long id2 = Long.parseLong(idString2);

        FriendshipStatusType status = FriendshipStatusType.valueOf(combo_status.getValue().toString());


        try {
            service.adaugare_invitatie(id1, id2, status);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Invitatie adaugata","Utilizatorul cu id " + id1 + " i-a trimis o cerere de prietenie utilizatorului cu id " + id2 + "!");
        } catch (ValidationException e) {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Cererea de prietenie a esuat",e.getMessage());
        }

        input_prieten1.setText("");
        input_prieten2.setText("");
        combo_status.setValue(FriendshipStatusType.pending);

//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();
    }

    private void  gui_modificare_invitatie()
    {
        String idString1 = input_prieten1.getText();
        Long id1 = Long.parseLong(idString1);
        String idString2 = input_prieten2.getText();
        Long id2 = Long.parseLong(idString2);

        FriendshipStatusType status = FriendshipStatusType.valueOf(combo_status.getValue().toString());

        try {
            service.modificare_invitatie(id1, id2, status);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Modificare","Statusul cererii a fost modificat.");
        } catch (ValidationException e) {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Modificarea a esuat",e.getMessage());
        }

        input_prieten1.setText("");
        input_prieten2.setText("");
        combo_status.setValue("pending");

//        initUsersModel();
//        initFriendshipsModel();
//        initInvitationsModel();
    }

    private void gui_nr_comunitati()
    {
        int n = service.nr_comunitati();
        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Numar comunitati","Exista " + n + " comunitati.");
    }

    private void gui_comunitate_sociabila()
    {
        String s = service.comunitate_sociabila();
        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Comunitatea cea mai sociabila", s);
    }

    private void gui_listaNprieteni()
    {
        String string_n = input_N.getText();
        Long n = Long.parseLong(string_n);
        List<Utilizator> lista = service.listaNprieteni(n);
        StringBuilder str = new StringBuilder();
        if(lista.isEmpty())
            str.append("Nu exista utilizatori cu minim " + n + " prieteni!");
        else
            for (Utilizator u: lista)
            {
                str.append(u.getFirstName()).append(" ").append(u.getLastName()).append("\n");
            }
        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Utilizatorii cu minim " + n + " prieteni", str.toString());

        input_N.setText("");
    }

    private void gui_afisare_prieteni_luna()
    {
        String string_id = input_id_luna.getText();
        Long id = Long.parseLong(string_id);
        String string_luna = input_luna.getText();
        Long luna = Long.parseLong(string_luna);

        if(luna<1 || luna>12)
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Afisarea a esuat", "Luna data este invalida.");
        else
        {
            List<PrietenDTO> lista = service.prietenii_utilizator_dat(id, luna);
            StringBuilder str = new StringBuilder();
            if(lista.isEmpty())
                str.append("Utilizatorul dat nu si-a facut prieteni in luna data");
            else
                for (PrietenDTO p: lista)
                    str.append(p.getFriend().getFirstName()).append(" ").append(p.getFriend().getLastName()).append("\n");
            MessageAlert.showMessage(null,Alert.AlertType.CONFIRMATION,"Afisare prieteni", str.toString());
        }

        input_id_luna.setText("");
        input_luna.setText("");
    }

    private void gui_afisare_utilizatori_string()
    {
        String s = input_string.getText();
        Iterable<Utilizator> lista = service.utilizatori_string(s);

        StringBuilder str = new StringBuilder();
        boolean isEmpty = StreamSupport.stream(lista.spliterator(), false)
                .allMatch(element -> false);

        if(isEmpty)
            str.append("Nu exista utilizatori");
        else
            for (Utilizator u: lista)
            {
                str.append(u.getFirstName()).append(" ").append(u.getLastName()).append("\n");
            }
        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Utilizatorii al caror nume/prenume contine string-ul dat", str.toString());

        input_string.setText("");
    }

}
