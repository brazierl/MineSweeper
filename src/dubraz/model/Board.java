/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubraz.model;

import java.util.*;

/**
 *
 * @author p1509019
 */
public class Board extends Observable {

    /**
     * Default tiles proportion in a Board
     */
    public static final double TRAPPED_TILES_PROP = 0.15;
    /**
     * List of tiles in the grid with their associated neighbours
     */
    private HashMap<Tile, ArrayList<Tile>> tiles;
    /**
     * true if the game is over
     */
    private boolean gameOver;
    /**
     * True if the player winned the game
     */
    private boolean win;
    /**
     * number of trapped tiles in the board
     */
    private int nbTrappedTiles;
    /**
     * number of free tiles in the grid
     */
    private int nbFreeTiles;

    /**
     * Constructor with a proportion of trapped tiles
     * @param width size
     * @param height size
     * @param trappedTilesProportion proportion of trapped tiles in the board
     */
    public Board(int width, int height, double trappedTilesProportion) {
        this.nbTrappedTiles = 0;
        this.nbFreeTiles = 0;
        this.gameOver = false;
        this.win = false;
        this.tiles = new HashMap<>();
        int nbTrappedTiles = (int) Math.floor(height * width * trappedTilesProportion);
        ArrayList<Integer> trappedTilesPositions = randomTrappedTilesPositons(nbTrappedTiles);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (trappedTilesPositions.contains(i * height + j)) {
                    this.nbTrappedTiles++;
                    tiles.put(new Tile(true, false, 0, false), null);

                } else {
                    nbFreeTiles++;
                    tiles.put(new Tile(false, false, 0, false), null);
                }
            }
        }
    }

    /**
     * Constructor with a number of trapped tiles
     * @param width size
     * @param height size
     * @param nbTrappedTiles number of trapped tiles in the board
     */
    public Board(int width, int height, int nbTrappedTiles) {
        this.gameOver = false;
        tiles = new HashMap<>();
        ArrayList<Integer> trappedTilesPositions = randomTrappedTilesPositons(nbTrappedTiles);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (trappedTilesPositions.contains(i * height + j)) {
                    this.nbTrappedTiles++;
                    tiles.put(new Tile(true, false, 0, false), null);
                } else {
                    nbFreeTiles++;
                    tiles.put(new Tile(false, false, 0, false), null);
                }
            }
        }
    }

    public HashMap<Tile, ArrayList<Tile>> getTiles() {
        return tiles;
    }
    /**
     * Generate random positions for the trapped tiles
     * @param nbTrappedTiles
     * @return 
     */
    private ArrayList<Integer> randomTrappedTilesPositons(int nbTrappedTiles) {
        ArrayList<Integer> trappedTilesPositions = new ArrayList<>();
        int val;
        for (int j = 0; j < nbTrappedTiles; j++) {
            val = (int) Math.round(Math.random() * nbTrappedTiles);
            if (!trappedTilesPositions.contains(val)) {
                trappedTilesPositions.add(val);
            } else {
                j--;
            }
        }
        return trappedTilesPositions;
    }

    /**
     * Method called to update the board state and notify the observers
     */
    public void update() {
        check();

        setChanged();
        notifyObservers();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWin() {
        return win;
    }

    public int getNbTrappedTiles() {
        return nbTrappedTiles;
    }

    /**
     * check the state of the board (if the game is over and if it is a victory)
     */
    private void check() {
        int i = 0;
        for (Map.Entry<Tile, ArrayList<Tile>> tile : tiles.entrySet()) {
            if (tile.getKey().isVisible()) {
                if (tile.getKey().isTrapped()) {
                    gameOver = true;
                    discoverTrappedTiles();
                } else {
                    i++;
                }
                if (i == nbFreeTiles) {
                    gameOver = true;
                    win = true;
                    setAllVisible();
                }
            }
        }
    }

    /**
     * set all the trapped tiles to visible when the game is over
     */
    private void discoverTrappedTiles() {
        for (Map.Entry<Tile, ArrayList<Tile>> tile : tiles.entrySet()) {
            if (tile.getKey().isTrapped()) {
                tile.getKey().setVisible(true);
            }
        }
    }
    
    /**
     * set all the tiles to visible when the game is over
     */
    private void setAllVisible(){
        for (Map.Entry<Tile, ArrayList<Tile>> tile : tiles.entrySet()) {
            tile.getKey().setVisible(true);
        }
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

}
