/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.*;

/**
 *
 * @author p1509019
 */
public class Board extends Observable {

    public static final double TRAPPED_TILES_PROP = 0.15;
    private HashMap<Tile, ArrayList<Tile>> tiles;
    private boolean gameOver;
    private boolean win;
    private int nbTrappedTiles;
    private int nbFreeTiles;

    public Board(int length, int width, double trappedTilesProportion) {
        this.nbTrappedTiles = 0;
        this.nbFreeTiles = 0;
        this.gameOver = false;
        this.win = false;
        this.tiles = new HashMap<>();
        int nbTrappedTiles = (int) Math.floor(width * length * trappedTilesProportion);
        ArrayList<Integer> trappedTilesPositions = randomTrappedTilesPositons(nbTrappedTiles);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                if (trappedTilesPositions.contains(i * width + j)) {
                    this.nbTrappedTiles++;
                    tiles.put(new Tile(true, false, 0, false), null);

                } else {
                    nbFreeTiles++;
                    tiles.put(new Tile(false, false, 0, false), null);
                }
            }
        }
    }

    public Board(int length, int width, int nbTrappedTiles) {
        this.gameOver = false;
        tiles = new HashMap<>();
        ArrayList<Integer> trappedTilesPositions = randomTrappedTilesPositons(nbTrappedTiles);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                if (trappedTilesPositions.contains(i * width + j)) {
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

    public void discover(Tile tile) {
        for (Tile neighbour : tiles.get(tile)) {
            if (neighbour.isTrapped()) {
                tile.setNbTrappedNeighbours(tile.getNbTrappedNeighbours() + 1);
            }
        }

        tile.setVisible(true);

        if (tile.getNbTrappedNeighbours() == 0 && !tile.isTrapped()) {
            ArrayList<Tile> neighbours = tiles.get(tile);
            for (Tile neighbour : neighbours) {
                if (!neighbour.isVisible()) {
                    discover(neighbour);
                }
            }
        }
    }

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
                }
            }
        }
    }

    private void discoverTrappedTiles() {
        ArrayList<Tile> trappedTiles = new ArrayList<>();
        for (Map.Entry<Tile, ArrayList<Tile>> tile : tiles.entrySet()) {
            if (tile.getKey().isTrapped()) {
                tile.getKey().setVisible(true);
            }
        }
    }

}
