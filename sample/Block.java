package sample;

import javafx.util.Pair;

public class Block {
    private int x, y;
    boolean free = true;

    Block(int x, int y){
        this.x = x;
        this.y = y;
    }

    boolean isEqual(int x, int y){
        return x >= this.x && x <= this.x + ShipActions.getBlockSize() &&
                y >= this.y && y <= this.y + ShipActions.getBlockSize();
    }

    boolean isFree(){
        return free;
    }

    void use(){
        free = false;
    }

    void free(){
        free = true;
    }

    Pair<Integer, Integer> getPosition(){
        return new Pair<>(x, y);
    }

     boolean isInOneRowWith(Block other){
        return this.y == other.getY();
     }

    public int getX() {
        return x;
    }

    int getY(){
        return this.y;
     }
}
