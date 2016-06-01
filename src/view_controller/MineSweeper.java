/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view_controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Board;
import model.Tile;

/**
 * Vue/Contrôleur
 * @author p1509019
 */
public class MineSweeper extends Application {

    private static final int TILE_SIZE = 30;
    private static final double SCORE_ZONE_SIZE_COEF = 1.2;
    private HashMap<Button, Tile> buttons;
    private ArrayList<ArrayList<Pair>> grid;
    private GridPane gPane;
    private BorderPane border;

    @Override
    public void start(Stage primaryStage) {
        // gestion du placement (permet de palcer le champ Text affichage en haut, et GridPane gPane au centre)
        border = new BorderPane();

        // permet de placer les diffrents boutons dans une grille
        gPane = new GridPane();

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
                        board.discover(buttons.get(b));
                    } // Left Clic
                    else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        buttons.get(b).clic(Tile.FLAG);
                    }
                    board.update();
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
                for (Map.Entry<Tile, ArrayList<Tile>> tile : board.getTiles().entrySet()) {
                    Tile t = tile.getKey();
                    Button b = getTileButton(t);
                    if (t.isVisible()) {
                        if (t.isTrapped()) {
                            Image imageMine = new Image("images/mine.png");
                            b.setGraphic(new ImageView(imageMine));
                        } else {
                            b.setGraphic(null);
                            if(t.getNbTrappedNeighbours()!=0)
                                b.setText("" + t.getNbTrappedNeighbours());
                        }
                        b.setDisable(true);
                        b.setStyle("-fx-opacity: 1.0; -fx-background-color:rgb(245,245,245);");
                    }
                    if (t.isFlagged()) {
                       Image imageFlag = new Image("images/flag.png");
                       b.setGraphic(new ImageView(imageFlag));
                    }

                }
            }
        });

        board.update();

        gPane.setGridLinesVisible(true);
        gPane.setAlignment(Pos.BOTTOM_CENTER);

        border.setCenter(gPane);

        Scene scene = new Scene(border, (dimension + 1) * TILE_SIZE, (dimension + 1) * TILE_SIZE * SCORE_ZONE_SIZE_COEF);

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
            i=0;
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
            i=0;
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

}
