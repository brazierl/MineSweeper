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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import dubraz.model.Board;
import dubraz.model.Tile;
import static dubraz.view_controller.MineSweeper.TILE_SIZE;

/**
 *
 * @author p1509019
 */
public class MineSweeperHexagone extends MineSweeper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initObserver() {
        super.initObserver(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initGrid() {
        int column = 0;
        int row = 0;

        int dimension = (int) Math.sqrt(board.getTiles().size()) - 1;

        // création des bouton et placement dans la grille
        buttons = new HashMap<>();
        ArrayList<Pair> rowList = new ArrayList<>();
        grid = new ArrayList<>();
        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
            Polygon polygon = new Polygon();
            polygon.getPoints().addAll(new Double[]{
                0.0, (double) TILE_SIZE / 2,
                (double) TILE_SIZE / 3, 0.0,
                2 * (double) TILE_SIZE / 3, 0.0,
                (double) TILE_SIZE, (double) TILE_SIZE / 2,
                2 * (double) TILE_SIZE / 3, (double) TILE_SIZE,
                (double) TILE_SIZE / 3, (double) TILE_SIZE});

            gPane.add(polygon, column++, row);

            polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    // Right Clic
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        /*buttons.get(b).clic(Tile.DISCOVER);

                         Runnable rDisco = new Runnable() {
                         @Override
                         public void run() {
                         board.discover(buttons.get(b));
                         System.out.println("rDisco : thread " + Thread.currentThread().getName());
                         Platform.runLater(new Runnable() {
                         @Override
                         public void run() {
                         board.update();
                         }
                         });
                         }
                         };

                         pool.execute(rDisco);*/
                    } // Left Clic
                    else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        /*Runnable rFlag = new Runnable() {
                         @Override
                         public void run() {
                         buttons.get(b).clic(Tile.FLAG);
                         System.out.println("rFlag : thread " + Thread.currentThread().getName());
                         Platform.runLater(new Runnable() {
                         @Override
                         public void run() {
                         board.update();
                         }
                         });
                         }
                         };

                         pool.execute(rFlag);*/
                    }
                }
            });

            Pair<Node, Tile> couple = new Pair<>(polygon, tile.getKey());
            rowList.add(couple);

            if (column > dimension) {
                grid.add(rowList);
                rowList = new ArrayList<>();
                column = 0;
                row++;
            }

            // ajouter la recherche des voisins pour mise à jour du modèle
            buttons.put(polygon, tile.getKey());
        }

        for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
            // Mise à jour des voisins
            tile.setValue(getTileNeighbours(tile.getKey()));
        }
        gPane.setGridLinesVisible(true);
        gPane.setAlignment(Pos.BOTTOM_CENTER);

        // Image 
        emojiView = new ImageView("/images/smiley.PNG");
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

    @Override
    protected void initGame(Stage primaryStage, int nbTrappedCells, int width, int height) {
        // gestion du placement (permet de palcer les composants des scores)
        gPaneScore = new GridPane();

        // permet de placer les diffrents boutons dans une grille
        gPane = new GridPane();

        // horloge
        clock = new Label();

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

        // Création des menus
        initMenu(primaryStage);

        // initialisation de la grille graphique
        initGrid();

        // initialisation de l'observer pour mettre à jour l'affichage
        //initObserver();

        board.update();
    }

    @Override
    protected int[] getPopupValues(Stage primaryStage, String... fields) {
        return super.getPopupValues(primaryStage, fields); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initMenu(Stage primaryStage) {
        super.initMenu(primaryStage); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ArrayList<Tile> getTileNeighbours(Tile t) {
        return super.getTileNeighbours(t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Tile getTile(int x, int y) {
        return super.getTile(x, y); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Pair<Integer, Integer> getCoordinatesTile(Tile t) {
        return super.getCoordinatesTile(t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Pair<Integer, Integer> getCoordinatesButton(Button b) {
        return super.getCoordinatesButton(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Button getTileButton(Tile t) {
        return super.getTileButton(t); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Tile getButtonTile(Button b) {
        return super.getButtonTile(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage); //To change body of generated methods, choose Tools | Templates.
    }

}
