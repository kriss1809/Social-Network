package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.Parola;
import org.example.exceptions.ValidationException;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.observer.Observer;

import java.io.IOException;

public class LoginController implements Observer<ChangeEvent> {

    Service service;
    @FXML
    private Button btn_login;
    @FXML
    private Button btn_signup;
    @FXML
    private TextField input_username;
    @FXML
    private PasswordField input_parola;

    public void setService(Service service) {
        this.service = service;
        service.addObserver(this);
    }


    @FXML
    private void handleUserPanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_login)
            gui_login();
        else if(event.getSource()==btn_signup)
           gui_signup();
    }

    @Override
    public void update(ChangeEvent changeEvent) {
    ;
    }

    private void gui_login()
    {
        Parola parola = new Parola(input_parola.getText(), input_username.getText());
        Parola parola_criptata = new Parola(parola.criptare(), input_username.getText());

        try {
            service.cautare_parola(parola_criptata);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Autentificare reusita", "");
        }
        catch (RuntimeException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Autentificare esuata", e.getMessage());
        }

        input_username.setText("");
        input_parola.setText("");
    }

    private void gui_signup()
    {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/views/signup-view.fxml"));
            Parent signupView = loader.load();

            Stage signupStage = new Stage();
            signupStage.setTitle("Creeaza un cont");
            signupStage.setScene(new Scene(signupView));

            SignupController signupController = loader.getController();
            signupController.setService(service);
            signupStage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }


}
