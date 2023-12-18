package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.domain.Parola;
import org.example.domain.Utilizator;
import org.example.domain.validators.ValidationException;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.observer.Observer;

import java.util.Optional;

public class SignupController implements Observer<ChangeEvent> {

    Service service;

    @FXML
    private Button btn_signup;
    @FXML
    private TextField input_username;
    @FXML
    private PasswordField input_parola;
    @FXML
    private PasswordField input_parola2;
    @FXML
    private TextField input_nume;
    @FXML
    private TextField input_prenume;

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);
    }

    @Override
    public void update(ChangeEvent changeEvent, Service updated_service) {
        this.service = updated_service;
    }

    @FXML
    private void handleUserPanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_signup)
            gui_signup();
    }

    private void gui_signup() {
        String nume = input_nume.getText();
        String prenume = input_prenume.getText();
        String username = input_username.getText();
        String parola = input_parola.getText();
        String parola2 = input_parola2.getText();

        if(!parola.equals(parola2)) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "EROARE", "Parolele nu corespund!");
            input_parola.setText("");
            input_parola2.setText("");
        }
        else
        {
            try
            {
                Optional<Utilizator> u = service.cautare_utilizator_username(username);
                if(u.isPresent()){
                    MessageAlert.showMessage(null, Alert.AlertType.ERROR, "EROARE", "Exista deja un utilizator cu acest username");
                    input_username.setText("");
                    input_parola.setText("");
                    input_parola2.setText("");
                }
                else
                {
                    try {
                        service.adaugare_utilizator(nume, prenume, username);
                        if(service.cautare_utilizator_username(username).isPresent())
                        {
                            Parola p = new Parola(parola, username);
                            String parola_criptata = p.criptare();
                            service.adaugare_parola(username, parola_criptata);
                        }
                        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "CONTUL A FOST CREAT", "Inchide fereastra pentru a te intoarce la Autentificare.");
                    } catch (ValidationException e) {
                        MessageAlert.showMessage(null, Alert.AlertType.ERROR, "CREAREA CONTULUI A ESUAT", e.getMessage());
                    }
                }
            }
            catch (ValidationException ex) {
                MessageAlert.showMessage(null,Alert.AlertType.ERROR,"CREAREA CONTULUI A ESUAT",ex.getMessage());
            }
        }

        input_nume.setText("");
        input_prenume.setText("");
        input_username.setText("");
        input_parola.setText("");
        input_parola2.setText("");

    }
}
