package snake.app;
// for server:
// receive:
// create 1
// join 2
// dir 3
// send:
// wait for seconde 4
// wait for game to start 5
// game over 6
// pos 7

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static snake.app.Config.GAME_SPEED_MS;
import static snake.app.Config.MOVE_SPEED_MS;


public class SnakeServer implements Runnable {
    private static final int PORT = 8080;
    private static final int BUF_SIZE = 1024;
    private static Client[] clients;
    private final ScheduledExecutorService scheduler;

    DatagramSocket serverSocket;
    private InetAddress server_ip;
    private int server_port;
    private byte[] buf;
    private static int[][] snake1;
    private static int[][] snake2;
    SnakeServer(InetAddress server_ip, int server_port) {
        this.server_ip = server_ip;
        this.server_port = server_port;
        try {
            serverSocket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    @Override
    public void run() {
        try{ // close the resource if implements java.lang.AutoCloseable
            System.out.println("UDP server started.");
            clients = new Client[2];
            //ServerGameState.get().gameOver() == -1
            while (true){
                byte[] buf = new byte[1024];
                DatagramPacket pkt = new DatagramPacket(buf,BUF_SIZE);
                serverSocket.receive(pkt);
                System.out.println("Receiving pakcet");
                byte[] data = pkt.getData();
                // rcv 1
                if((int)data[0] == 1){
                    System.out.println("First user created game");
                    sendCreatePlayer(data,pkt);
                }
                // rcv 2
                else if((int)data[0] == 2){
                    System.out.println("Second user joined game");
                    sendJoinPlayer(data,pkt);
                    scheduler.scheduleAtFixedRate(this::sendSnake, 0, GAME_SPEED_MS, MILLISECONDS);
                }
                // rcv 3
                else if((int)data[0] == 3){
                    System.out.println("Received direction from "+pkt.getAddress());
                    changeDirection(data);
                    if(checkState()) break;
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private boolean checkState() {
        int flag = ServerGameState.get().gameOver();
        if(flag != -1){
            sendGameOver(flag);
            return true;
        }
        return false;
    }

    // send 4
    private void sendCreatePlayer(byte[] data, DatagramPacket pkt) throws UnknownHostException {
        this.buf = new byte[BUF_SIZE];
        int IDLen = data[1];
        int nameLen = data[2];
        int offset = 3;
        String id = new String(data,offset,IDLen);
        offset += IDLen;
        String name = new String(data, offset,nameLen);
        offset += nameLen;
        InetAddress ip = pkt.getAddress();
        offset += 4;
        int port = pkt.getPort();
        clients[0] = new Client(ip,port,name,id);
        buf[0] = (byte) 4;
        try {
            for (Client c : clients) {
                if (c != null) {
                    DatagramPacket snd_pkt = new DatagramPacket(buf, 1, c.IP, c.UDP_PORT);
                    System.out.println("sending to " + c + " " + buf[0]);
                    serverSocket.send(snd_pkt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send 5
    private void sendJoinPlayer(byte[] data, DatagramPacket pkt) throws UnknownHostException {
        this.buf = new byte[BUF_SIZE];
        int IDLen = data[1];
        int nameLen = data[2];
        int offset = 3;
        String id = new String(data,3,IDLen);
        offset+=IDLen;
        String name = new String(data, 3+IDLen,nameLen);
        offset+=nameLen;
        InetAddress ip = pkt.getAddress();
        offset+=4;
        byte[] p = Arrays.copyOfRange(data,offset,offset+2);
        int port = pkt.getPort();
        clients[1] = new Client(ip,port,name,id);
        buf[0] = (byte) 5;
        try {
          for(Client c: clients) {
              if(c != null){
                  DatagramPacket snd_pkt = new DatagramPacket(buf, 1, c.IP, c.UDP_PORT);
                  serverSocket.send(snd_pkt);
                  System.out.println("sending to " + c + buf[0]);
              }
          }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send 7
    private void sendSnake() {
        try {
            if (checkState()) {
                scheduler.shutdown();
                return;
            }
            ServerGameState.get().moveSnake1();
            ServerGameState.get().moveSnake2();
            System.out.println("prepare to send snake ");
            int offset = 0;
            this.buf = new byte[BUF_SIZE];
            buf[offset++] = (byte) 7;
            List<Position> snake1Position = ServerGameState.get().getSnake1Position().collect(Collectors.toList());
            List<Position> snake2Position = ServerGameState.get().getSnake2Position().collect(Collectors.toList());
            snake1 = new int[32][32];
            snake2 = new int[32][32];
            snake1Position.forEach(SnakeServer::fillArray1);
            snake2Position.forEach(SnakeServer::fillArray2);
            buf[offset++] = 0;
            Position p = ServerGameState.get().getApple();
            System.out.println("apple position");
            buf[offset++] = (byte) p.getRow();
            buf[offset++] = (byte) p.getCol();
            StringBuilder bits;
            int count = 0;
            System.out.println(clients[0].toString());
            System.out.println(clients[1].toString());
            for (int i = 0; i < clients.length; i++) {
                System.out.println(i);
                bits = new StringBuilder();
                if (i == 0) {
                    for (int row = 0; row < 32; row++) {
                        for (int col = 0; col < 32; col++) {
                            if (snake1[row][col] == 0) {
                                bits.append("0");
                            } else {
                                bits.append("1");
                            }
                            count++;
                            if (count == 8) {
                                buf[offset++] = (byte)(int) Integer.valueOf(bits.toString(), 2);
                                count = 0;
                                bits = new StringBuilder();
                            }
                        }
                    }
                } else {
                    count = 0;
                    for (int row = 0; row < 32; row++) {
                        for (int col = 0; col < 32; col++) {
                            if (snake2[row][col] == 0) {
                                bits.append("0");
                            } else {
                                bits.append("1");
                            }
                            count++;
                            if (count == 8) {
                                buf[offset++] = (byte)(int) Integer.valueOf(bits.toString(), 2);
                                count = 0;
                                bits = new StringBuilder();
                            }
                        }
                    }
                }
            }
            for (Client c : clients) {
                DatagramPacket snd_pkt = new DatagramPacket(buf, offset, c.IP, c.UDP_PORT);
                serverSocket.send(snd_pkt);
                System.out.println(
                    "sending snake packet to " + c + " message: " + buf[0]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send 6
    private void sendGameOver(int flag) {
        System.out.println("Shutdown");
        int offset = 0;
        this.buf = new byte[BUF_SIZE];
        buf[offset++] = (byte) 6;
        int result = ServerGameState.get().gameOver() == 0 ? 0 : 1;
        buf[offset++] = (byte) result;
        String name;

        if (flag != 0) {
            name = clients[flag - 1].name;
            buf[offset++] = (byte) clients[flag - 1].name.length();
        } else {
            name = "draw";
            buf[offset++] = (byte) "draw".getBytes().length;
        }
        writeBytes(name, buf, offset);
        offset += name.getBytes().length;
        try {
            for(Client c: clients) {
                if(c != null){
                    DatagramPacket snd_pkt = new DatagramPacket(buf, offset, c.IP, c.UDP_PORT);
                    serverSocket.send(snd_pkt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void changeDirection(byte[] data){
        int IDLen = data[1];
        int nameLen = data[2];
        int offset = 3;
        String id = new String(data, offset, IDLen);
        offset += IDLen;
        String name = new String(data, offset, nameLen);
        offset += nameLen;
        Direction dir;
        int s = 0;
        for (int i = 0; i < clients.length; i++) {
            if (clients[i].name.equals(name)) {
                s = i + 1;
                break;
            }
        }
        switch ((int) data[offset]){
            case 0: dir = Direction.UP; break;
            case 1: dir = Direction.RIGHT; break;
            case 2: dir = Direction.DOWN; break;
            default: dir = Direction.LEFT; break;
        }
        ServerGameState.get().updateDirection(s, dir);
    }

    public static void fillArray2(Position position) {
        System.out.println(position.toString());
        snake2[position.getRow()][position.getCol()] = 1;
    }

    public static void fillArray1(Position position) {
        System.out.println(position.toString());
        snake1[position.getRow()][position.getCol()] = 1;
    }

    public static void writeBytes(String str, byte[] buf, int offset) {
        byte[] b = str.getBytes();
        for (int i = 0; i < b.length; i++) {
            buf[offset + i] = b[i];
        }
    }

    class Client{
        InetAddress IP;
        int UDP_PORT;
        String name;
        String id;

        public Client(InetAddress ipAddr, int UDP_PORT, String s, String id ){
            this.IP = ipAddr;
            this.UDP_PORT = UDP_PORT;
            this.name = s;
            this.id = id;
        }

        @Override
        public String toString() {
            return "Client{" +
                "IP=" + IP +
                ", UDP_PORT=" + UDP_PORT +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
        }
    }
}
