package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import java.util.*;

public class ShipActions {

    private GraphicsContext gc = null;
    private GraphicsContext gc_opp = null;
    private static int blockSize;
    private int canvasWidth;





   static ArrayList<Block> blocks = new ArrayList<>();

   static ArrayList<Ship> ships = new ArrayList<>();

    ShipActions(GraphicsContext gc, GraphicsContext opp, int width ){
        this.gc = gc;
        this.gc_opp = opp;
        canvasWidth = width;
        blockSize = canvasWidth/10;
    }

    public void addBlock(int x, int y){
        blocks.add(new Block(x, y));
    }

    static int getBlockSize(){
        return blockSize;
    }

    boolean addShip(int shipSize){
        Random rand = new Random();
        int randBlock;
        /// try to add random
        for(int i = 0; i < 10; i++){
            randBlock = Math.abs(rand.nextInt()) % 100;

            if(blocks.get(randBlock).isFree()){

                var freeSpace = getFreeSpace(randBlock, shipSize, (randBlock%2 == 0 ? -1 : 1)   );
                if(!freeSpace.isEmpty()){

                    ships.add(new Ship(freeSpace.get(0), shipSize, (randBlock%2 == 0 ? -1 : 1)));
                    drawShip(freeSpace, gc);

                    return true;
                }

            }
        }

        for(Block b : blocks){
            if(b.isFree()){

                var freeSpace = getFreeSpace(blocks.indexOf(b), shipSize, -1);
                if(!freeSpace.isEmpty()){

                    ships.add(new Ship(freeSpace.get(0), shipSize, -1));
                    drawShip(freeSpace, gc);

                    return true;
                }
            }
        }
        return false;
    }


    public void rotateShip(Ship ship){
        // set new ship direction
        if(ship == null){return;}

        int currIndex = ((int) ship.getY()/blockSize) * 10 + ((int) ship.getX()/blockSize);
        var freeSpace = getFreeSpace(currIndex, ship.getSize(), ship.getDirection() * (-1));

        if(!freeSpace.isEmpty()){
            ship.rotate();

            // clear place from old position and set blocks free
            setPaint(0);
            int step = (ship.getDirection() == -1 ? 10 : 1);

            for(int i = currIndex; i <= currIndex + step * ship.getSize() - 1; i+=step){
                blocks.get(i).free();
                gc.strokeRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize );
                gc.fillRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize );
            }

            // draw ship in new position
            drawShip(freeSpace, gc);
        }


    }

     ArrayList<Integer> getFreeSpace(int firstIndex, int shipSize, int  direction){

        ArrayList<Integer> freeBoxes = new ArrayList<Integer>();
        freeBoxes.add(firstIndex);
        if(direction == -1){
            // look for horizontal space
            int Y = blocks.get(firstIndex).getY();
            for(int i= firstIndex; i < firstIndex + shipSize && i < 100; i++ ){
                freeBoxes.add(i);
                if(!blocks.get(i).isFree() || blocks.get(i).getY() != Y){
                    freeBoxes.clear();
                }
            }

        }else{
            // look for vertical space
            for(int i = firstIndex; i < firstIndex + (shipSize -1) * 10  && i < 100; ){
                i+=10;
                freeBoxes.add(i);
                if(!blocks.get(i).isFree()){
                    freeBoxes.clear();
                    break;
                }
            }
        }
        return  freeBoxes;
    }


    Ship findShip(int x, int y){

        int currIndex = ((int) x/blockSize) + ((int) y/blockSize) * 10;
        for(Ship s : ships){
            if(blocks.get(s.getFirstBlock()).isEqual(x, y)){
                return s;
            }
        }

        return null;
    }


    static Block getBlock(int index){
        if(index < blocks.size()){
            return blocks.get(index);
        }
        return null;
    }


    void drawShip(ArrayList<Integer> array, GraphicsContext gc){

        setPaint(1);

        for(Integer i : array){
            gc.fillRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize);
            gc.strokeRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize);
            blocks.get(i).use();
        }
        setPaint(0);
    }


    void setPaint(int val){
        if(val == 0){
            gc.setStroke(Paint.valueOf("Lightgray"));
            gc.setFill(Paint.valueOf("aliceblue"));
        }else{
            gc.setFill(Paint.valueOf("Darkblue"));
            gc.setStroke(Paint.valueOf("lightblue"));
        }
        gc.setLineWidth(2);
    }

    ArrayList<Integer> getUsedBlocks(){
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for(Block b : blocks){
            if(!b.isFree()){
                arr.add(blocks.indexOf(b));
            }
        }
        return arr;
    }

    void moveShip(Ship ship, Direction dir){

        if(ship == null){
            return;
        }

        ArrayList<Integer> newShipBlocks;
        newShipBlocks =  freeSpace(ship.firstBlockIndex, ship.getSize(), ship.getDirection()* (-1));

        if((dir == Direction.RIGHT && ship.getDirection() == -1)
                ||( dir == Direction.DOWN && ship.getDirection() == 1)){

            int max = Collections.max(newShipBlocks);
            int addBlockIndex = max + ((ship.getDirection() == -1) ? 1 : 10);

            if(blocks.get(addBlockIndex).isFree() && ((ship.getDirection() == -1 && addBlockIndex%10 != 0)
                    || (blocks.get(addBlockIndex).getY() + blockSize <= canvasWidth && ship.getDirection() == 1))){

                newShipBlocks.remove(Collections.min(newShipBlocks));
                newShipBlocks.add(addBlockIndex);
            }
        }

        else if((dir == Direction.LEFT && ship.getDirection() == -1)
                || (dir == Direction.UP  && ship.getDirection() == 1) ){

            int step = (ship.getDirection() == -1 ? 1 : 10);

            if((step == 1 && ship.getX() >= blockSize ) || (step == 10 && ship.getY() >= blockSize) ){
                if(blocks.get(ship.getFirstBlock() - step ).isFree()){

                    newShipBlocks.remove(Collections.max(newShipBlocks));
                    newShipBlocks.add(ship.getFirstBlock() - step);
                }
            }
        }

        else if((dir == Direction.DOWN && ship.getDirection() == -1) || (dir == Direction.RIGHT && ship.getDirection() == 1)){

            int step = (ship.getDirection() == -1 ? 10 : 1);

            if((step == 10 && ship.getY() + blockSize < canvasWidth) || (
                    step == 1 && ship.getX() <= canvasWidth - blockSize)){
                // check if all needed blocks below are free
                for(int i = ship.getFirstBlock(); i < ship.getFirstBlock() + ship.getSize(); i++){

                    if(!blocks.get(i+step).isFree()){
                        return;
                    }
                }

                ArrayList<Integer> newIndeces = new ArrayList<>(ship.getSize());
                for(int i = 0; i <ship.getSize(); i++){
                    newIndeces.add(newShipBlocks.get(i) + step);
                }

                newShipBlocks.clear();
                newShipBlocks = newIndeces;
            }
        }
        else if((dir == Direction.UP && ship.getDirection() == -1) || (dir == Direction.LEFT && ship.getDirection() == 1)){

            int step = (ship.getDirection() == -1 ? 10 : 1);

            if((step == 10 && ship.getY() >= blockSize-1) || (step==1 && ship.getX() >= blockSize ) ){
                // check if all needed blocks below are free
                for(int i = ship.getFirstBlock(); i < ship.getFirstBlock() + ship.getSize(); i+=(step == 1 ? 10 : 1)){
                    if(!blocks.get(i-step).isFree()){
                        return;
                    }
                }

                ArrayList<Integer> newIndeces = new ArrayList<>(ship.getSize());
                for(int i = 0; i <ship.getSize(); i++){
                    newIndeces.add(newShipBlocks.get(i) - step);
                }
                newShipBlocks.clear();
                newShipBlocks = newIndeces;
            }
        }

        ship.setFirstBlockIndex(Collections.min(newShipBlocks));
        drawShip(newShipBlocks, gc);

    }

    ArrayList<Integer> freeSpace(int firstIndex, int size, int direction){

        ArrayList<Integer> clearedBlocks = new ArrayList<>();
        setPaint(0);
        int step = (direction == -1 ? 10 : 1);

        for(int i = firstIndex; i < firstIndex + step * size; i+=step ){

            blocks.get(i).free();
            clearedBlocks.add(i);
            gc.strokeRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize );
            gc.fillRect(blocks.get(i).getX(), blocks.get(i).getY(), blockSize, blockSize );

        }

        setPaint(1);
        return clearedBlocks;
    }


    int getShipCount(){
        return ships.size();
    }



    int findBlockIndex(int x,  int y){
        return ((int) x/blockSize) + ((int) y/blockSize) * 10;
    }

    void setMoveResult(int val, int blockIndex, GraphicsContext gc){
        setMovePaint(val, gc);
        gc.fillRect(blocks.get(blockIndex).getX(), blocks.get(blockIndex).getY(), blockSize, blockSize);

    }
    void setMovePaint(int value, GraphicsContext gc){
        switch (value){
            case 0 :
                gc.setFill(Paint.valueOf("lightgray"));
                break;
            case 1:
                gc.setFill(Paint.valueOf("darkred"));
                break;
            case 2:
                gc.setFill(Paint.valueOf("black"));
                break;
        }

    }

    int[] IsFullyKilled(int blockIndex){
        int[] result = new int[3];
        int size = -1, step;
        for(Ship s: ships){
            size = s.getSize();
            step = (s.getDirection() == -1 ? 1 : 10);

            for(int i = s.getFirstBlock(); i < s.getFirstBlock() + size * step; i+=step){
                if(i == blockIndex){
                    // ship is found

                    for(int j = s.getFirstBlock(); j < s.getFirstBlock() + size * step; j+=step){
                        if(blocks.get(j).isFree()){
                            result[0]= -1;
                            return result;
                        }
                    }
                    result[0] = size;
                    result[1] = s.getFirstBlock();
                    result[2] = s.getDirection();

                    break;
                }
            }
        }
        return result;
    }
}
