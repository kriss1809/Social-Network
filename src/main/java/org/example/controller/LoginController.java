package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.domain.Parola;

public class LoginController {

    @FXML
    private Button btn_login;
    @FXML
    private Button btn_signup;
    @FXML
    private TextField input_username;
    @FXML
    private TextField input_parola;

    @FXML
    private void handleUserPanelClicks(ActionEvent event)
    {
        if(event.getSource()==btn_login)
            gui_login();
    }

    private void gui_login()
    {
        String username = input_username.getText();
        Parola parola = new Parola(input_parola.getText());
        String parola_criptata = parola.criptare();



        input_username.setText("");
        input_parola.setText("");
    }

}
