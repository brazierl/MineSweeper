/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view_controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import model.Board;
import model.Tile;

/**
 * Vue/Contrôleur
 *
 * @author p1509019
 */
public class MineSweeper extends Application {

    private static final int TILE_SIZE = 30;
    private static final double SCORE_ZONE_SIZE_COEF = 1.2;
    private HashMap<Button, Tile> buttons;
    private ArrayList<ArrayList<Pair>> grid;
    private GridPane gPane;
    private BorderPane border;
    private GridPane gPaneScore;
    private Label clock;
    private ExecutorService pool;
    private Timeline timeline;
    private Date startDate;
    private ImageView emojiView;

    @Override
    public void start(Stage primaryStage) {
        // Création d'un pool de thread (dans le controleur ?)
        pool = Executors.newFixedThreadPool(4);

        // gestion du placement (permet de placer les scores en haut, et GridPane gPane au centre)
        border = new BorderPane();

        // gestion du placement (permet de palcer les composants des scores)
        gPaneScore = new GridPane();

        // permet de placer les diffrents boutons dans une grille
        gPane = new GridPane();
        
        // Création des menus
        initMenu();

        // horloge
        clock = new Label();

        int column = 0;
        int row = 0;

        Board board = new Board(0.15);
        int dimension = (int) Math.sqrt(board.getTiles().size()) - 1;

        // création des bouton et placement dans la grille
        buttons = new HashMap<>();
        ArrayList<Pair> rowList = new ArrayList<>();
        grid = new ArrayList<>();
        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
            Button b = new Button();
            b.setMinSize(TILE_SIZE, TILE_SIZE);
            /*
             final Text t = new Text();
             t.setWrappingWidth(TILE_SIZE);
             t.setFont(Font.font("Verdana", 20));
             t.setTextAlignment(TextAlignment.CENTER);
             */

            gPane.add(b, column++, row);

            b.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    // Right Clic
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        buttons.get(b).clic(Tile.DISCOVER);

                        Runnable rDisco = new Runnable() {
                            @Override
                            public void run() {
                                board.discover(buttons.get(b));
                                System.out.println("rDisco : thread " + Thread.currentThread().getName());
                                board.update();
                            }
                        };

                        pool.execute(rDisco);
                    } // Left Clic
                    else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        Runnable rFlag = new Runnable() {
                            @Override
                            public void run() {
                                buttons.get(b).clic(Tile.FLAG);
                                System.out.println("rFlag : thread " + Thread.currentThread().getName());
                                board.update();
                            }
                        };

                        pool.execute(rFlag);
                    }
                }
            });

            Pair<Button, Tile> couple = new Pair<>(b, tile.getKey());
            rowList.add(couple);

            if (column > dimension) {
                grid.add(rowList);
                rowList = new ArrayList<>();
                column = 0;
                row++;
            }

            // ajouter la recherche des voisins pour mise à jour du modèle
            buttons.put(b, tile.getKey());
        }

        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
            // Mise à jour des voisins
            tile.setValue(getTileNeighbours(tile.getKey()));
        }

        board.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
                            Tile t = tile.getKey();
                            Button b = getTileButton(t);
                            b.setGraphic(null);
                            if (t.isVisible()) {
                                if (t.isTrapped()) {
                                    Image imageMine = new Image("images/mine.png");
                                    b.setGraphic(new ImageView(imageMine));
                                } else {
                                    if (t.getNbTrappedNeighbours() != 0) {
                                        b.setText("" + t.getNbTrappedNeighbours());
                                        switch(t.getNbTrappedNeighbours()){
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                                
                                        }
                                    }
                                }
                                b.setDisable(true);
                                b.setStyle("-fx-opacity: 1.0; -fx-background-color:rgb(245,245,245);");
                            }
                            if (t.isFlagged()) {
                                Image imageFlag = new Image("images/flag.png");
                                b.setGraphic(new ImageView(imageFlag));
                            }
                            if (board.isGameOver()) {
                                gPane.setDisable(true);
                                gPane.setStyle("-fx-opacity: 1.0;");
                                timeline.stop();
                                
                                emojiView.setImage(new Image("/images/lost.jpg"));
                                
                                
                            }
                        }
                    }
                });
            }
        });

        board.update();

        gPane.setGridLinesVisible(true);
        gPane.setAlignment(Pos.BOTTOM_CENTER);

        // TimeLine gérant l'évolution de l'horloge
        startDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("mm:ss");
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Date date = new Date(new Date().getTime() - startDate.getTime());;
                                clock.setText(dateFormat.format(date));
                            }
                        }
                )
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Image 
        
        emojiView = new ImageView("/images/smiley.jpg");
        HBox hbEmoji =new HBox();
        hbEmoji.getChildren().add(emojiView);
        hbEmoji.setAlignment(Pos.CENTER);
                
        //Ajout des composants au gPane
        gPaneScore.setAlignment(Pos.CENTER);
        gPaneScore.setVgap(10);
        gPaneScore.add(clock, 0, 0);
        gPaneScore.add(hbEmoji, 0, 1);

        border.setBottom(gPane);
        border.setCenter(gPaneScore);
        
        clock.setStyle("-fx-font-size: 30;");

        Scene scene = new Scene(border, (dimension + 1) * TILE_SIZE, (dimension + 1) * TILE_SIZE * SCORE_ZONE_SIZE_COEF);

        primaryStage.setTitle("Démineur");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Get the associate tile of a button.
     *
     * @param b
     * @return
     */
    private Tile getButtonTile(Button b) {
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getKey() == b) {
                    return p.getValue();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param b
     * @return
     */
    private Button getTileButton(Tile t) {
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getValue() == t) {
                    return p.getKey();
                }
            }
        }
        return null;
    }

    private Pair<Integer, Integer> getCoordinatesButton(Button b) {
        int x = 0;
        int y = 0;
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getKey() == b) {
                    return new Pair<>(x, y);
                }
                x++;
            }
            y++;
        }
        return new Pair<>(-1, -1);
    }

    private Pair<Integer, Integer> getCoordinatesTile(Tile t) {
        int i = 0;
        int j = 0;
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getValue() == t) {
                    return new Pair<>(i, j);
                }
                i++;
            }
            j++;
            i = 0;
        }
        return new Pair<>(-1, -1);
    }

    private Tile getTile(int x, int y) {
        int i = 0;
        int j = 0;
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (x == i && y == j) {
                    return p.getValue();
                }
                i++;
            }
            j++;
            i = 0;
        }
        return null;
    }

    private ArrayList<Tile> getTileNeighbours(Tile t) {
        ArrayList<Tile> neighbours = new ArrayList<>();
        Pair<Integer, Integer> coord = getCoordinatesTile(t);
        for (int j = -1; j < 2; j++) {
            for (int i = -1; i < 2; i++) {
                if (coord.getKey() + i >= 0 && coord.getValue() + j >= 0 && coord.getKey() + i < grid.size() && coord.getValue() + j < grid.get(0).size()) {
                    Tile currT = getTile(coord.getKey() + i, coord.getValue() + j);
                    if (!(i == 0 && j == 0)) {
                        neighbours.add(currT);
                    }
                }
            }
        }
        return neighbours;
    }
    private void initMenu(){
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("Fichier");
        MenuItem reset = new MenuItem("Recommencer");
        reset.setOnAction((ActionEvent t) -> {
            System.err.println("recommencer");
        });
        Menu game = new Menu("Partie");
        MenuItem nbMine = new MenuItem("Nombre de mines");
        nbMine.setOnAction((ActionEvent t) -> {
            System.err.println("Nb mines");
        });
        MenuItem gridSize = new MenuItem("Taille de la grille");
        gridSize.setOnAction((ActionEvent t) -> {
            System.err.println("taille grille");
        });
        
        game.getItems().addAll(nbMine,gridSize);
        
        menuFile.getItems().addAll(reset, game);
        
        menuBar.getMenus().add(menuFile);
        
        border.setTop(menuBar);
    }

}
