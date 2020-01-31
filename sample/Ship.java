package sample;

public class Ship {
    // -1 horizontal, 1 - vertical
    int direction;
    int size;
    int firstBlockIndex;


    Ship(int firstBlock, int size,  int direction){

        this.firstBlockIndex = firstBlock;
        this.size = size;
        this.direction = direction;
    }

    public int getX() {
        return ShipActions.getBlock(firstBlockIndex).getX();
    }

    public int getY() {
        return ShipActions.getBlock(firstBlockIndex).getY();
    }

    public int getDirection() {
        return direction;
    }

    public void rotate(){
        this.direction *= (-1);
    }

    public int getSize() {
        return size;
    }

    public int getFirstBlock() {
        return firstBlockIndex;
    }

    public void setFirstBlockIndex(int firstBlockIndex) {
        this.firstBlockIndex = firstBlockIndex;

    }
}
