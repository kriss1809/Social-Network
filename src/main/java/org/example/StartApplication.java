package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.controller.LoginController;
import org.example.domain.validators.Validator;
import org.example.repository.DataBaseRepository.*;
import org.example.service.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class StartApplication extends Application {

    private DB_Access data;
    private UtilizatorRepoDB repoUser;
    private PrietenieRepoDB repoFriendship;
    private MessageRepoDB repoMessage;
    private InvitationRepoDB repoInvitation;
    private ParolaRepoDB repoParola;
    public Service service;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        String url="jdbc:postgresql://localhost:5432/social_network";
        String username="postgres";
        String password="18Kriss2003";

        this.data = new DB_Access(url, username, password);
        try {
            data.createConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Validator val = null;
        this.repoUser = new UtilizatorRepoDB(val,data, "users");
        this.repoFriendship = new PrietenieRepoDB(val, data, "friendship");
        this.repoMessage = new MessageRepoDB(val, data, "messages");
        this.repoInvitation = new InvitationRepoDB(val, data, "invitations");
        this.repoParola = new ParolaRepoDB(val, data, "parole");
        this.service = new Service(val, repoUser, repoFriendship, repoMessage, repoInvitation, repoParola);

        init(primaryStage);
        primaryStage.show();
    }

    private void init(Stage primaryStage) throws IOException
    {
        FXMLLoader stageLoader = new FXMLLoader();
        stageLoader.setLocation(getClass().getResource("/org/example/views/login-view.fxml"));
        AnchorPane setLayout = stageLoader.load();
        primaryStage.setTitle("Social Network");
        primaryStage.setScene(new Scene(setLayout, Color.BEIGE));

        LoginController ctrl = stageLoader.getController();
        ctrl.setService(service);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

