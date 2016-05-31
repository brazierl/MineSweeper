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

    private HashMap<Tile, ArrayList<Tile>> tiles;

    public Board(double trappedTilesProportion) {
        tiles = new HashMap<>();
        int width = 20;
        int length = 20;
        int nbTrappedTiles = (int) Math.floor(width * length * trappedTilesProportion);
        ArrayList<Integer> trappedTilesPositions = randomTrappedTilesPositons(nbTrappedTiles);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                if (trappedTilesPositions.contains(i * width + j)) {
                    tiles.put(new Tile(true, false, 0, false), null);
                } else {
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

    // Probleme sur la condition de visible...
    public void discover(Tile tile) {
        tile.setVisible(true);
        
        for (Tile neighbour : tiles.get(tile)) {
            if (neighbour.isTrapped()) {
                tile.setNbTrappedNeighbours(tile.getNbTrappedNeighbours() + 1);
            }
        }

        if (tile.getNbTrappedNeighbours() == 0) {
            ArrayList<Tile> neighbours = tiles.get(tile);
            for (Tile neighbour : neighbours) {
                if (!neighbour.isVisible()) {
                    discover(neighbour);
                }
            }
        }
    }
    
    public void update(){
        setChanged();
        notifyObservers();
    }

}
