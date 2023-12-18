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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.Message;
import org.example.domain.Parola;
import org.example.exceptions.ValidationException;
import org.example.service.Service;
import org.example.utils.events.ChangeEvent;
import org.example.utils.observer.Observer;

import java.io.IOException;
import java.net.URL;

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
            gui_user_window(input_username.getText());
        }
        catch (RuntimeException e) {
            MessageAlert.showMessage(null, Alert.AlertType.ERROR, "Autentificare esuata", e.getMessage());
        }

        input_username.setText("");
        input_parola.setText("");
    }

    private void gui_user_window(String username)
    {
        if(username.equals("admin"))
        {
            try
            {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/org/example/views/start-view.fxml"));
                Parent adminview = loader.load();

                Stage adminStage = new Stage();
                adminStage.setTitle("Bun venit!");
                adminStage.setScene(new Scene(adminview));

                StartController startController = loader.getController();
                startController.setService(service);
                adminStage.show();

                FXMLLoader loadermsg = new FXMLLoader();
                loadermsg.setLocation(getClass().getResource("/org/example/views/message-view.fxml"));
                Parent adminviewmsg = loadermsg.load();

                Stage adminmsgstage = new Stage();
                adminmsgstage.setTitle("Bun venit!");
                adminmsgstage.setScene(new Scene(adminviewmsg));

                MessageController msgController = loadermsg.getController();
                msgController.setService(service);
                adminmsgstage.show();

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/org/example/views/user-view.fxml"));
                Parent signupView = loader.load();

                Stage signupStage = new Stage();
                signupStage.setTitle("Bun venit!");
                signupStage.setScene(new Scene(signupView));

                UserController userController = loader.getController();
                userController.setService(service, username);
                signupStage.show();

            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
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
