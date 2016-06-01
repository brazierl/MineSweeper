/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author p1509019
 */
public class Tile {

    public final static int DISCOVER = 1;
    public final static int FLAG = 2;
    private boolean trapped;
    private boolean visible;
    private int nbTrappedNeighbours;
    private boolean flagged;

    public Tile(boolean trapped, boolean visible, int nbNearNeighbours, boolean flagged) {
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

    public void clic(int action) {
        switch (action) {
            case Tile.DISCOVER:
                this.flagged = false;
                this.visible = true;
                break;
            case Tile.FLAG:
                if(!this.visible)
                    this.flagged = true;
                break;
            default:
                break;
        }
    }

}
