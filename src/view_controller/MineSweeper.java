/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view_controller;

import java.util.Observer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 *
 * @author p1509019
 */
public class MineSweeper extends Application {

    @Override
    public void start(Stage primaryStage) {
        // gestion du placement (permet de palcer le champ Text affichage en haut, et GridPane gPane au centre)
        BorderPane border = new BorderPane();

        // permet de placer les diffrents boutons dans une grille
        GridPane gPane = new GridPane();

        int column = 0;
        int row = 0;

        // création des bouton et placement dans la grille
        for (String s : new String[]{}) {
            final Text t = new Text(s);
            t.setWrappingWidth(30);
            t.setFont(Font.font("Verdana", 20));
            t.setTextAlignment(TextAlignment.CENTER);

            gPane.add(t, column++, row);

            if (column > 3) {
                column = 0;
                row++;
            }

            // un controleur (EventHandler) par bouton écoute et met à jour le champ affichage
            t.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    
                }

            });
        }

        gPane.setGridLinesVisible(true);

        border.setCenter(gPane);

        Scene scene = new Scene(border, 300, 250);

        primaryStage.setTitle("Démineur");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
