package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class Controller {
    final int size = 300;
    int block_width = size/10;
    private  boolean dragDetected = false;
    private boolean movesBlocked = false;
    private boolean turn = true;

    private int score = 0;

    DataInputStream input;
    DataOutputStream output;
    Socket socket;

    @FXML
    Label _message_label;

    @FXML
    Canvas _currentPlayerField, _opponentField;
    GraphicsContext gc, gc_op;

    @FXML
    Label _label_x1, _label_x2, _label_x3, _label_x4;

    @FXML
    GridPane _availableShips;

    ShipActions shipActions;

    private int x1, y1;


    public void initialize(){
        int width = 10;
        gc = _currentPlayerField.getGraphicsContext2D();
        gc_op = _opponentField.getGraphicsContext2D();

//        gc.fillRect(0, 0, 50, 50);
        gc.setFill(Paint.valueOf("aliceblue"));
        gc_op.setFill(Paint.valueOf("aliceblue"));
        gc.setStroke(Paint.valueOf("Lightgray"));
        gc_op.setStroke(Paint.valueOf("Lightgray"));

        shipActions = new ShipActions(gc, gc_op, size);

        int x1 =0, y1 = 0;

        for(int r=0; r < 100; r++){
            gc.fillRect(x1, y1, block_width, block_width);
            gc_op.fillRect(x1, y1, block_width, block_width);
            gc.strokeRect(x1, y1, block_width, block_width);
            gc_op.strokeRect(x1, y1, block_width, block_width);
            shipActions.addBlock(x1, y1);

            if((x1 + block_width)  != this.size){
                x1+=block_width;
            }else{
                x1=0;
                y1+=block_width;
            }
        }

        gc.setFill(Paint.valueOf("darkgreen"));
        _message_label.setText("Move all your ship to the field and then click  PLAY button ");

    }


    public void locateShip(MouseEvent mouseEvent) {

        int ship_size = (int) (((Button) (mouseEvent.getSource())).getWidth() / block_width);
        Label l;
        switch (ship_size){
            case(4): l = _label_x4; break;
            case(3): l = _label_x3; break;

            case(2): l = _label_x2; break;
            case(1): l = _label_x1; break;
                default: return;
        };

        if (Integer.parseInt(l.getText()) < 1){
           return;
        }

        if(shipActions.addShip(ship_size)){
            l.setText(String.valueOf(Integer.parseInt(l.getText()) -1 ));
        }

    }





    public void rotateShip(MouseEvent mouseEvent) {
        // if game is started - block moving

        shipActions.rotateShip(shipActions.findShip((int ) mouseEvent.getX(), (int ) mouseEvent.getY()));

    }

    public void moveShip(MouseEvent mouseEvent) {
        if(movesBlocked){
            _message_label.setText("You can't move your ships during the game");
            return;
        }

        if(!dragDetected) {
            rotateShip(mouseEvent);
            return;
        }
        int newX = (int) mouseEvent.getX(), newY = (int) mouseEvent.getY();
        Ship sh = shipActions.findShip(x1, y1);

        if(sh == null) { return;}

        if(newX - x1 > block_width/2){
            shipActions.moveShip(sh,  Direction.RIGHT);
            x1 = sh.getX() + 15;
            y1 = sh.getY();

        }else if(x1 - newX > block_width/2){
            shipActions.moveShip(sh,  Direction.LEFT);
            x1 = sh.getX();
            y1 = sh.getY();

        }else if(newY - y1 > block_width/2){
            shipActions.moveShip(sh,  Direction.DOWN);
            x1 = sh.getX() ;
            y1 = sh.getY() ;

        }else if(y1 - newY > block_width/2){
            shipActions.moveShip(sh,  Direction.UP);
            x1 = sh.getX() ;
            y1 = sh.getY() ;
        }

        dragDetected = false;
    }

    public void setX(MouseEvent mouseEvent) {
        dragDetected = true;
        x1 = (int) mouseEvent.getX();
        y1 = (int) mouseEvent.getY();


    }

    public void startGame(MouseEvent mouseEvent) {

        if(shipActions.getShipCount() == 10 ){
            movesBlocked = true;
            _message_label.setText("Make your move and wait for the server");

        }else{
            _message_label.setText("Please, put all your ships on the field");
        }
    }


    private boolean connectToServer(){

        try{
            socket = new Socket("localhost", 8888);
            output =new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
        }catch (Exception e){
            System.out.println("Client::exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    void writeToServer(int blockIndex){

        String inputmsg;
        try{
            turn = false;
            _message_label.setText("Wait ...");
            //send game move
            output.writeUTF(String.valueOf(blockIndex));
            output.flush();

            //get opponent move
            inputmsg = input.readUTF();
            if(inputmsg.equals("GAME OVER")){
                _message_label.setText(inputmsg);
                closeConnection();
                turn = false;
                return;
            }
            int opponentMove = Integer.parseInt(inputmsg);

            // sheck  and send a result of opponent move

            output.writeUTF((shipActions.getUsedBlocks().contains(opponentMove) ? 1 : 0)  + "," + 0 + "," );
            output.flush();


            // check move result
            inputmsg = input.readUTF();
            int ifBlockWasInjured = Integer.parseInt(inputmsg.substring(0,1));

            shipActions.setMoveResult(ifBlockWasInjured, blockIndex, gc_op);

            // set opponent move on current player screen
            score -= (shipActions.getUsedBlocks().contains(opponentMove) ? 1 : 0);
            shipActions.setMoveResult(shipActions.getUsedBlocks().contains(opponentMove) ? 1 : 0 , opponentMove, gc);


            if(score == -20) {
                _message_label.setText("YOU WON");
                output.writeUTF("GAME OVER");
                output.flush();

                closeConnection();
                turn = false;
                return;
            }

            turn = true;
            _message_label.setText("Your turn!");

        }catch (Exception e){
            System.out.println("server exception");
        }
    }

    void closeConnection(){
        try{
            output.close();
            input.close();
            socket.close();
        }catch (Exception e){
            System.out.println("server exception");
            e.printStackTrace();
        }
    }


    public void killBlock(MouseEvent mouseEvent) {

        if(!movesBlocked) {
            return;
        }
        if(input == null){
            connectToServer();
        }
        if(turn){
            writeToServer( shipActions.findBlockIndex((int ) mouseEvent.getX(),(int) mouseEvent.getY()));
            turn = true;
        }
    }


}
