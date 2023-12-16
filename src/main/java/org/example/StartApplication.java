package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.controller.MessageController;
import org.example.controller.StartController;
import org.example.domain.validators.Validator;
import org.example.repository.DataBaseRepository.*;
import org.example.service.Service;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class StartApplication extends Application {

    private DB_Access data;
    private UtilizatorRepoDB repoUser;
    private PrietenieRepoDB repoFriendship;
    private MessageRepoDB repoMessage;
    private InvitationRepoDB repoInvitation;
    public Service service;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        String url="jdbc:postgresql://localhost:5432/socialnetwork";
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
        this.service = new Service(val, repoUser, repoFriendship, repoMessage, repoInvitation);

        initView(primaryStage);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException{
        FXMLLoader stageLoader = new FXMLLoader();
        stageLoader.setLocation(getClass().getResource("/org/example/views/start-view.fxml"));
        AnchorPane setLayout = stageLoader.load();
        primaryStage.setTitle("Social Network");
        primaryStage.setScene(new Scene(setLayout, Color.BEIGE));

        StartController ctrl = stageLoader.getController();
        ctrl.setService(service);

        try
        {
            URL resourceUrl = getClass().getResource("/org/example/views/message-view.fxml");
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(resourceUrl);

            AnchorPane root=(AnchorPane) loader.load();

            Stage dialogStage=new Stage();
            dialogStage.setTitle("Mesaje");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene=new Scene(root);
            dialogStage.setScene(scene);


            MessageController msgController = loader.getController();
            msgController.setService(service);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

