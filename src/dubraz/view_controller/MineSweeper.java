/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubraz.view_controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import dubraz.model.Board;
import dubraz.model.Tile;

/**
 * Vue/Contrôleur
 *
 * @author p1509019
 */
public class MineSweeper extends Application {
    
    /**
     * Taille d'une case par défault
     */
    protected static final int TILE_SIZE = 30;
    /**
     * Map liant un boutton et une ccase (Tile)
     */
    protected HashMap<Node, Tile> buttons;
    /**
     * Liste de liste représentant la grille
     */
    protected ArrayList<ArrayList<Pair>> grid;
    /**
     * Objet visuel de grille pour contenir les bouttons
     */
    protected GridPane gPane;
    /**
     * Composant qui place les objets dans la fenêtre
     */
    protected BorderPane border;
    /**
     * Grille pour le placement de l'horloge et de l'emoji
     */
    protected GridPane gPaneScore;
    /**
     * Label de l'horloge
     */
    protected Label clock;
    /**
     * Pool de thread
     */
    protected ExecutorService pool;
    /**
     * Timeline de l'horloge : Un evenement se déclanche toute les secondes pour raffraichir l'horloge
     */
    protected Timeline timeline;
    /**
     * Date de début du timer
     */
    protected Date startDate;
    /**
     * Date de fin du timer si le challenge de temps est lancé
     */
    protected Date stopDate;
    /**
     * Durée du timer pour le challenge de temps
     */
    protected int timeout;
    /**
     * Image de l'émoji
     */
    protected ImageView emojiView;
    /**
     * Plateau/Modèle associé à la vue
     */
    protected Board board;
    /**
     * Scène principale
     */
    protected Scene scene;
    /**
     * Longueur de la grille
     */
    protected int width;
    /**
     * Hauteur de la grille
     */
    protected int height;
    /**
     * Test si le jeu à déjà commencé
     */
    protected boolean firstClic;

    @Override
    public void start(Stage primaryStage) {
        // Création d'un pool de thread (dans le controleur ?)
        pool = Executors.newFixedThreadPool(4);

        // gestion du placement (permet de placer les scores en haut, et GridPane gPane au centre)
        border = new BorderPane();

        // Initialisation du timeline qui gère le minuteur
        initTimer();

        // Initiation du jeu avec une board apr défaut
        initGame(primaryStage, 0, 10, 10);

        scene = new Scene(border);

        // Création d'un controleur pour les saisies clavier
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (t.getCode().equals(KeyCode.F2) || (t.getCode().equals(KeyCode.N) && t.isControlDown())) { // F2 ou ctrl + n pour recommencer
                    initGame(primaryStage, board.getNbTrappedTiles(), width, height);
                }
            }
        });

        // Initialisation de la fenètre javafx
        primaryStage.setTitle("Démineur");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(
                new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event
                    ) {
                        Platform.exit();
                        System.exit(0);
                    }
                }
        );
        primaryStage.setResizable(
                false);
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
     * @param b a button
     * @return a Tile
     */
    protected Tile getButtonTile(Button b) {
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
     * Get the associate button of a Tile
     * 
     * @param t, a tile 
     * @return a button
     */
    protected Button getTileButton(Tile t) {
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getValue() == t) {
                    return p.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Get a button coordinates
     * @param b a button
     * @return a pair of coordinates
     */
    protected Pair<Integer, Integer> getCoordinatesButton(Button b) {
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

    /**
     * Get a tile coordinates
     * @param t a Tile
     * @return a pair of coordinates
     */
    protected Pair<Integer, Integer> getCoordinatesTile(Tile t) {
        int i = 0;
        int j = 0;
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (p.getValue() == t) {
                    return new Pair<>(i, j);
                }
                j++;
            }
            i++;
            j = 0;
        }
        return new Pair<>(-1, -1);
    }

    /**
     * Get a tile from coordinates
     * @param x width
     * @param y height
     * @return a TIle
     */
    protected Tile getTile(int x, int y) {
        int i = 0;
        int j = 0;
        for (ArrayList<Pair> al : grid) {
            for (Pair<Button, Tile> p : al) {
                if (x == i && y == j) {
                    return p.getValue();
                }
                j++;
            }
            i++;
            j = 0;
        }
        return null;
    }
    /**
     * Get neighbours of a tile during grid creation
     * @param t a Tile
     * @return List of tile neighbours
     */
    protected ArrayList<Tile> getTileNeighbours(Tile t) {
        ArrayList<Tile> neighbours = new ArrayList<>();
        Pair<Integer, Integer> coord = getCoordinatesTile(t);
        for (int j = -1; j < 2; j++) {
            for (int i = -1; i < 2; i++) {
                if (coord.getKey() + i >= 0 && coord.getValue() + j >= 0 && coord.getKey() + i < height && coord.getValue() + j < width) {
                    Tile currT = getTile(coord.getKey() + i, coord.getValue() + j);
                    if (!(i == 0 && j == 0)) {
                        neighbours.add(currT);
                    }
                }
            }
        }
        return neighbours;
    }
    /**
     * Initialize the menus 
     * @param primaryStage 
     */
    protected void initMenu(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        // Menu fichier pour la gestion de la partie
        Menu menuFile = new Menu("Fichier");
        MenuItem reset = new MenuItem("Recommencer (F2)");
        reset.setOnAction((ActionEvent t) -> {
            initGame(primaryStage, board.getNbTrappedTiles(), width, height);
        });
        Menu game = new Menu("Partie");
        MenuItem nbMine = new MenuItem("Nombre de mines");
        nbMine.setOnAction((ActionEvent t) -> {
            initGame(primaryStage, getPopupValues(primaryStage, "Nombre de mines : ")[0], 0, 0);
        });
        MenuItem gridSize = new MenuItem("Taille de la grille");
        gridSize.setOnAction((ActionEvent t) -> {
            int[] res = getPopupValues(primaryStage, "Entrez la hauteur : ", "Entrez la largeur : ");
            initGame(primaryStage, board.getNbTrappedTiles(), res[1], res[0]);
        });
        // Menu challenge pour ajouter des contraintes
        Menu menuChallenge = new Menu("Challenge");
        MenuItem temps = new MenuItem("Définir un temps limite");
        temps.setOnAction((ActionEvent t) -> {
            int[] res = getPopupValues(primaryStage, "Entrez un temps \nlimite (en minutes) : ", "Entrez un temps \nlimite (en secondes) : ");
            initGame(primaryStage, board.getNbTrappedTiles(), 0, 0);
            if (res[0] > 0 || res[1] > 0) {
                timeout = res[0] * 60 + res[1];
            } else {
                timeout = 0;
            }

        });

        game.getItems().addAll(nbMine, gridSize);

        menuFile.getItems().addAll(reset, game);

        menuChallenge.getItems().addAll(temps);

        menuBar.getMenus().addAll(menuFile, menuChallenge);

        border.setTop(menuBar);
    }

    /**
     * Generic function to create popups and return values entered
     * @param primaryStage main stage 
     * @param fields List of fields to entered
     * @return 
     */
    protected int[] getPopupValues(Stage primaryStage, String... fields) {
        final Stage dialog = new Stage();
        int[] res = new int[fields.length];
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);
        GridPane gpane = new GridPane();
        gpane.setHgap(10);
        gpane.setVgap(10);
        gpane.setAlignment(Pos.CENTER);
        int i = 0;
        for (String f : fields) {
            Text t = new Text(f);
            TextField tf = new TextField();
            gpane.add(t, 0, i);
            gpane.add(tf, 1, i);
            i++;
        }
        Button b = new Button("Valider");
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int j = 0;
                for (Node n : gpane.getChildren()) {
                    if (n instanceof TextField) {
                        try {
                            res[j] = Integer.parseInt(((TextField) n).getText());
                        } catch (Exception ex) {
                            res[j] = 0;
                        }
                        j++;
                    }
                }
                dialog.close();
            }
        });
        b.setDefaultButton(true);
        gpane.add(b, 1, i);
        dialogVbox.getChildren().add(gpane);
        dialogVbox.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
        return res;
    }

    /**
     * Main function to init the game.
     * @param primaryStage main stage
     * @param nbTrappedCells number of trapped cells for the Board
     * @param width size of the grid
     * @param height size of the grid
     */
    protected void initGame(Stage primaryStage, int nbTrappedCells, int width, int height) {
        firstClic = true;
        
        // gestion du placement (permet de palcer les composants des scores)
        gPaneScore = new GridPane();

        // permet de placer les diffrents boutons dans une grille
        gPane = new GridPane();

        // horloge
        clock = new Label();
        
        clock.setText("00:00");

        if (width == 0 || height == 0) {
            width = this.width;
            height = this.height;
        } else {
            this.width = width;
            this.height = height;
        }

        primaryStage.setWidth((width + 1) * TILE_SIZE);
        primaryStage.setHeight((height + 5) * TILE_SIZE);

        if (nbTrappedCells != 0) {
            board = new Board(width, height, (int) nbTrappedCells);
        } else {
            board = new Board(width, height, Board.TRAPPED_TILES_PROP);
        }

        // TimeLine gérant l'évolution de l'horloge
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.stop();

        // Création des menus
        initMenu(primaryStage);

        // initialisation de la grille graphique
        initGrid();

        // initialisation de l'observer pour mettre à jour l'affichage
        initObserver();

        board.update();
    }

    /**
     * Initialize the graphical grid into the window
     */
    protected void initGrid() {
        int column = 0;
        int row = 0;

        // création des bouton et placement dans la grille
        buttons = new HashMap<>();
        ArrayList<Pair> rowList = new ArrayList<>();
        grid = new ArrayList<>();
        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
            Button b = new Button();
            b.setMinSize(TILE_SIZE, TILE_SIZE);
            b.setMaxSize(TILE_SIZE, TILE_SIZE);

            gPane.add(b, column++, row);

            b.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (firstClic) {
                        startDate = new Date();
                        timeline.play();
                        firstClic = false;
                    }
                    // Right Clic
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                                                Runnable rDisco = new Runnable() {
                            @Override
                            public void run() {
                                buttons.get(b).clic(Tile.DISCOVER, board);
                                System.out.println("rDisco : thread " + Thread.currentThread().getName());
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        board.update();
                                    }
                                });
                            }
                        };

                        pool.execute(rDisco);
                    } // Left Clic
                    else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        Runnable rFlag = new Runnable() {
                            @Override
                            public void run() {
                                buttons.get(b).clic(Tile.FLAG, board);
                                System.out.println("rFlag : thread " + Thread.currentThread().getName());
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        board.update();
                                    }
                                });
                            }
                        };

                        pool.execute(rFlag);
                    }
                }
            });

            Pair<Button, Tile> couple = new Pair<>(b, tile.getKey());
            rowList.add(couple);

            if (column >= width) {
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
        gPane.setGridLinesVisible(true);
        gPane.setAlignment(Pos.BOTTOM_CENTER);

        // Image 
        emojiView = new ImageView("/dubraz/images/smiley.PNG");
        emojiView.setFitHeight(TILE_SIZE);
        emojiView.setFitWidth(TILE_SIZE);
        HBox hbEmoji = new HBox();
        hbEmoji.getChildren().add(emojiView);
        hbEmoji.setAlignment(Pos.CENTER);

        //Ajout des composants au gPane
        gPaneScore.setAlignment(Pos.CENTER);
        gPaneScore.setVgap(10);
        gPaneScore.add(clock, 0, 0);
        gPaneScore.add(hbEmoji, 0, 1);

        border.setBottom(gPane);
        border.setCenter(gPaneScore);

        clock.setStyle("-fx-font-size: " + TILE_SIZE + ";");
    }

    /**
     * Observer for the Board
     */
    protected void initObserver() {
        board.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
                    Tile t = tile.getKey();
                    Button b = getTileButton(t);
                    b.setGraphic(null);
                    if (t.isVisible()) {
                        if (t.isTrapped()) {
                            Image imageMine = new Image("dubraz/images/mine.png");
                            b.setGraphic(new ImageView(imageMine));
                            b.setStyle("-fx-opacity: 1.0; -fx-background-color: rgb(245,245,245);");
                        } else if (t.getNbTrappedNeighbours() != 0) {
                            b.setText("" + t.getNbTrappedNeighbours());
                            b.setDisable(true);
                            String style = "-fx-opacity: 1.0; -fx-background-color: rgb(245,245,245); -fx-font-weight: bold;  -fx-text-fill: ";
                            switch (t.getNbTrappedNeighbours()) {
                                case 1:
                                    b.setStyle(style + "blue;");
                                    break;
                                case 2:
                                    b.setStyle(style + "green;");
                                    break;
                                case 3:
                                    b.setStyle(style + "red;");
                                    break;
                                case 4:
                                    b.setStyle(style + "midnightblue;");
                                    break;
                                default:
                                    b.setStyle(style + "brown;");
                                    break;
                            }
                        } else {
                            b.setDisable(true);
                            b.setStyle("-fx-background-color: rgb(245,245,245);");
                        }
                    }
                    if (t.isFlagged()) {
                        ImageView flagView = new ImageView("dubraz/images/flag.png");
                        flagView.setFitHeight(TILE_SIZE / 2);
                        flagView.setFitWidth(TILE_SIZE / 2);
                        b.setGraphic(flagView);
                    }
                }
                if (board.isGameOver()) {
                    firstClic = true;
                    gPane.setDisable(true);
                    gPane.setStyle("-fx-opacity: 1.0;");
                    if (board.isWin()) {
                        emojiView.setImage(new Image("/dubraz/images/victory.PNG"));
                    } else {
                        emojiView.setImage(new Image("/dubraz/images/lost.PNG"));
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            timeline.stop();
                        }
                    });

                }
            }
        });
    }

    /**
     * Initialize components for the timer
     */
    private void initTimer() {
        // TimeLine pour animation du minuteur
        DateFormat dateFormat = new SimpleDateFormat("mm:ss");
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Date date;
                                if (timeout > 0) {
                                    stopDate = new Date(startDate.getTime() + new Date(timeout * 1000).getTime());
                                    date = new Date(stopDate.getTime() + 1000 - new Date().getTime());
                                    if (stopDate.getTime() - new Date().getTime() <= 0) {
                                        board.setGameOver(true);
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                board.update();
                                            }
                                        });
                                    }
                                } else {
                                    date = new Date(new Date().getTime() - startDate.getTime());
                                }
                                clock.setText(dateFormat.format(date));
                            }
                        }
                )
        );
    }
}
