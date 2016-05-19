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
public class Board {
    private HashMap<Tile,ArrayList<Tile>> tiles;
    
    public Board(){
        tiles = new HashMap<>();
        int width = 20;
        int length = 20;
        for(int i=0; i<width;i++){
            for(int j=0; j<length; j++){
                tiles.put(new Tile(),null);
            }
        }
    }

    public Board(HashMap<Tile, ArrayList<Tile>> tiles) {
        this.tiles = tiles;
    }
    
}
