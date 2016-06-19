/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubraz.model;

import java.util.ArrayList;

/**
 *
 * @author p1509019
 */
public class Tile {

    /**
     * Static variables to signal the type of clic
     */
    public final static int DISCOVER = 1;
    public final static int FLAG = 2;
    /**
     * true if the tile is trapped
     */
    private boolean trapped;
    /**
     * true if the tile is visible
     */
    private boolean visible;
    
    private int nbTrappedNeighbours;
    private boolean flagged;

    public Tile(boolean trapped, boolean visible, int nbTrappedNeighbours, boolean flagged) {
        this.trapped = trapped;
        this.visible = visible;
        this.nbTrappedNeighbours = nbTrappedNeighbours;
        this.flagged = flagged;
    }

    public boolean isTrapped() {
        return trapped;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setNbTrappedNeighbours(int nbTrappedNeighbours) {
        this.nbTrappedNeighbours = nbTrappedNeighbours;
    }

    public int getNbTrappedNeighbours() {
        return nbTrappedNeighbours;
    }

    public boolean isVisible() {
        return visible;
    }

    public void clic(int action, Board board) {
        switch (action) {
            case Tile.DISCOVER:
                if (!this.visible) {
                    this.flagged = false;
                    this.visible = true;
                    this.discover(board);
                }
                break;
            case Tile.FLAG:
                if (!this.visible) {
                    if (!this.flagged) {
                        this.flagged = true;
                    } else {
                        this.flagged = false;
                    }
                }
                break;
            default:
                break;
        }
    }

    /** 
     * update the state of the tiles and set them visible according to the rules of the game
     * @param board state of the board, contains the neighbours
     */
    public void discover(Board board) {
        for (Tile neighbour : board.getTiles().get(this)) {
            if (neighbour.isTrapped()) {
                this.setNbTrappedNeighbours(this.getNbTrappedNeighbours() + 1);
            }
        }
        this.setVisible(true);
        if (this.getNbTrappedNeighbours() == 0 && !this.isTrapped()) {
            ArrayList<Tile> neighbours = board.getTiles().get(this);
            for (Tile neighbour : neighbours) {
                if (!neighbour.isVisible()) {
                    neighbour.discover(board);
                }
            }
        }
    }

}
