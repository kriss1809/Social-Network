package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.domain.validators.ValidationException;
import org.example.repository.DataBaseRepository.Pageable;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.observer.Observer;
import org.example.repository.DataBaseRepository.Page;
import org.example.repository.DataBaseRepository.Pageable;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageController implements Observer<ChangeEvent> {

    Service service;

    @FXML
    TableView<Message> messageTbl;
    @FXML
    TableColumn<Message, Long> id_mesaj;
    @FXML
    TableColumn<Message, Long> id_emitator;
    @FXML
    TableColumn<Message, Long> id_receptor;
    @FXML
    TableColumn<Message, String> text;
    @FXML
    TableColumn<Message, String> data;
    @FXML
    TableColumn<Message, Long> raspuns;

    @FXML
    private Button btn_trimitere_mesaj;
    @FXML
    private Button btn_afisare_mesaje;

    @FXML
    private TextField input_id_mesaj;
    @FXML
    private TextArea input_text;
    @FXML
    private TextField input_emitator;
    @FXML
    private TextField input_receptor;
    @FXML
    private TextField input_raspuns;

    @FXML
    private TextField input_user1;
    @FXML
    private TextField input_user2;
    public ObservableList<Message> messagesModel = FXCollections.observableArrayList();


    // pentru paginare mesaje
    private int currentPage4 = 0;
    private int pageSize4 = 10;
    private int totalNumberOfElements4 = 0;
    @FXML
    private Label pageNumber4;
    @FXML
    private Button nextButton4;
    @FXML
    private Button previousButton4;
    @FXML
    private Button btn_pagesize;
    @FXML
    private ComboBox cb_pagesize;
    ObservableList<Integer> elemente = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);
        //initMessageModel();
        initMessageModelPage();
        cb_pagesize.setItems(elemente);
        cb_pagesize.setValue(10);
    }

    @FXML
    public void initialize()
    {
        initializeTableMessages();
    }

    private void initializeTableMessages()
    {
        id_mesaj.setCellValueFactory(new PropertyValueFactory<Message,Long>("id"));
        id_emitator.setCellValueFactory(new PropertyValueFactory<Message,Long>("from"));
        id_receptor.setCellValueFactory(new PropertyValueFactory<Message,Long>("to"));
        text.setCellValueFactory(new PropertyValueFactory<Message,String>("message"));
        data.setCellValueFactory(new PropertyValueFactory<Message,String>("data"));
        raspuns.setCellValueFactory(new PropertyValueFactory<Message,Long>("reply"));
        messageTbl.setItems(messagesModel);
    }

    private void initMessageModel() {
        Iterable<Message> msgs = service.getAllMessages();
        List<Message> msgList = StreamSupport.stream(msgs.spliterator(),false).collect(Collectors.toList());
        messagesModel.setAll(msgList);
    }

    private void initMessageModelPage()
    {
        Page<Message> page = service.getAllMessagesPage(new Pageable(currentPage4, pageSize4));

        int maxPage = (int) Math.ceil((double) page.getTotalElementCount() / pageSize4 ) - 1;
        if(currentPage4 > maxPage) {
            currentPage4 = maxPage;
            page = service.getAllMessagesPage(new Pageable(currentPage4, pageSize4));
        }

        messagesModel.setAll(StreamSupport.stream(page.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
        totalNumberOfElements4 = page.getTotalElementCount();

        previousButton4.setDisable(currentPage4 == 0);
        nextButton4.setDisable((currentPage4+1)*pageSize4 >= totalNumberOfElements4);

        pageNumber4.setText("Page " + (currentPage4+1) +"/" + (maxPage+1));
    }

    public void onPrevious4(ActionEvent actionEvent) {
        currentPage4--;
        initMessageModelPage();
    }

    public void onNext4(ActionEvent actionEvent) {
        currentPage4++;
        initMessageModelPage();
    }

    @Override
    public void update(ChangeEvent ChangeEvent) {
        initMessageModelPage();
    }

    @FXML
    private void handleMessagePanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_trimitere_mesaj)
            gui_adaugare_mesaj();
        else if(event.getSource()==btn_afisare_mesaje)
            gui_afisare_mesaje();
        else if(event.getSource()==btn_pagesize)
            gui_incarcare_pagina();
    }

    private void gui_incarcare_pagina()
    {
        pageSize4 = Integer.parseInt(cb_pagesize.getValue().toString());
        initMessageModelPage();
    }


    private void gui_adaugare_mesaj()
    {
        String idString = input_id_mesaj.getText();
        Long id_msg = Long.parseLong(idString);
        String idString1 = input_emitator.getText();
        Long id_emitator = Long.parseLong(idString1);
        String idString2 = input_receptor.getText();
        String[] receptori = idString2.split(" ");
        String text = input_text.getText();
        String idString3 = input_raspuns.getText();
        Long raspuns;
        if(idString3=="")
            raspuns = null;
        else
            raspuns = Long.parseLong(idString3);
        try {
            for(int i = 0; i<receptori.length; ++i)
                service.adaugare_mesaj(id_msg+i, id_emitator, Long.parseLong(receptori[i]), text, LocalDateTime.now(), raspuns);

            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Adaugare","Mesajul a fost salvat cu succes");
        } catch (ValidationException e) {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Adaugarea a esuat",e.getMessage());
        }

       // initializeTableMessages();
       // initMessageModel();

        input_id_mesaj.setText("");
        input_emitator.setText("");
        input_receptor.setText("");
        input_text.setText("");
        input_raspuns.setText("");
    }

    private void gui_afisare_mesaje()
    {
        String idString1 = input_user1.getText();
        Long id_user1 = Long.parseLong(idString1);
        String idString2 = input_user2.getText();
        Long id_user2 = Long.parseLong(idString2);

        try {
            String s = service.afisare_mesaje(id_user1, id_user2);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Afisare conversatii",s);
        } catch (ValidationException e) {
            MessageAlert.showMessage(null,Alert.AlertType.ERROR,"Afisarea a esuat",e.getMessage());
        }
    }

}
