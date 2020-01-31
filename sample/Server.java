package sample;

import java.io.DataInputStream;
        import java.io.DataOutputStream;
        import java.net.ServerSocket;

public class Server {


    public static void main(String[] args) {

        System.out.println("Server is running...");
        try{
            var ss = new ServerSocket(8888);
            var s1 = ss.accept();

            DataInputStream dis1 = new DataInputStream(s1.getInputStream());
            DataOutputStream dos1 = new DataOutputStream(s1.getOutputStream());

            var s2 = ss.accept();

            DataInputStream dis2 = new DataInputStream(s2.getInputStream());
            DataOutputStream dos2 = new DataOutputStream(s2.getOutputStream());


            String move1, move2,  answer1, answer2;

            while(!(move1 =dis1.readUTF()).equals( "GAME OVER")){

                //read move from first gamer
                move2 = dis2.readUTF();
                if(move2.equals("GAME OVER")){

                    break;
                }

                dos2.writeUTF(move1);
                dos2.flush();

                dos1.writeUTF(move2);
                dos1.flush();

                answer1 = dis1.readUTF();
                answer2 = dis2.readUTF();

                dos2.writeUTF(answer1);
                dos2.flush();
                dos1.writeUTF(answer2);
                dos1.flush();


            }

            dos1.close();
            dos2.close();
            s1.close();
            s2.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
