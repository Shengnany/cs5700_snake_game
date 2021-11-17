package snake.app;
// for player:
// send:
// create 1
// join 2
// dir 3
// receive:
// receive:
// wait for seconde 4
// wait for game to start 5
// game over 6
// pos 7
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class SnakePlayer extends JFrame implements Runnable {
    private final String gameId;
    private final String name;
    private final InetAddress server_ip;
    private final int server_port;
    private static final int BUF_SIZE = 1024;
    private static byte[] snd_buf;
    private final PlayerBoard board;
    private LinkedList<Position> s1;
    private LinkedList<Position> s2;
    private Position apple;
    private DatagramSocket clientSocket;
    private int player;
    private InetAddress ip;
    private int port;

    SnakePlayer(String gameId, String name, InetAddress server_ip, int server_port, int player,
        InetAddress ip, int port) {
        this.gameId = gameId;
        this.name = name;
        this.server_ip = ip;
        this.server_port = server_port;
        this.player = player;
        this.ip = ip;
        this.port = port;
        snd_buf = new byte[BUF_SIZE];
        setTitle("Snake Game");
        board = new PlayerBoard();
        setState(PlayerBoard.WAIT);
        add(board);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        try {
            clientSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        s1 = new LinkedList<>();
        s2 = new LinkedList<>();
        sendInfoPacket();
        System.out.println("Player " + player + " starts.");
    }

    @Override
    public void run() {
        try {
            while(true){
                byte[] rcv_buf = new byte[BUF_SIZE];
                DatagramPacket rcvPkt = new DatagramPacket(rcv_buf, BUF_SIZE);
                clientSocket.receive(rcvPkt);
                byte[] data = rcvPkt.getData();
                if((int)data[0] == 4){
                    System.out.println("waiting for opponent");
                    board.setState(PlayerBoard.WAIT);
                    board.repaint();
                }
                else if((int)data[0] == 5){
                    System.out.println("waiting for start");
                    board.setState(PlayerBoard.READY);
                    board.repaint();
                    waitForStart();
                }
                else if((int)data[0] == 6){
                    System.out.println("received gameover");
                    terminateGame(data,1);
                    break;
                }
                else if((int)data[0] == 7){
                    System.out.println("received snake");
                    rcvSnake(data);
                }
            }
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    private void terminateGame(byte[] data, int offset) throws InterruptedException {
        int res = data[offset++];
        int len = data[offset++];
        String name = new String(data, offset, len);
        PlayerGameState1.get().setWinner(name);
        PlayerGameState1.get().setRes(res);
        PlayerGameState2.get().setWinner(name);
        PlayerGameState2.get().setRes(res);
        board.repaint();
        Thread.sleep(5000);
        System.exit(0);
    }

    private void waitForStart() throws InterruptedException {
//        TimeUnit.SECONDS.sleep();
        board.setState(PlayerBoard.START);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        sendDirPacket(3);
                        break;
                    case KeyEvent.VK_RIGHT:
                        sendDirPacket(1);
                        break;
                    case KeyEvent.VK_UP:
                        sendDirPacket(0);
                        break;
                    case KeyEvent.VK_DOWN:
                        sendDirPacket(2);
                        break;
                }
            }
        });
    }

    private void rcvSnake(byte[] data){
        int appleRow = data[2];
        int appleCol = data[3];
        this.apple = new Position(appleRow, appleCol, 32);
        int[][] snake1 = new int[32][32];
        int[][] snake2 = new int[32][32];
        makeSnake(snake1, data, 4);
        makeSnake(snake2, data, 132);
        s1.clear();
        s2.clear();
        for (int row = 0; row < 32; row++) {
            for (int col = 0; col < 32; col++) {
                if (snake1[row][col] == 1) {
                    Position p = new Position(row, col, 32);
                    s1.add(p);
                }
            }
        }
        for (int row = 0; row < 32; row++) {
            for (int col = 0; col < 32; col++) {
                if (snake2[row][col] == 1) {
                    Position p = new Position(row, col, 32);
                    s2.add(p);
                }
            }
        }
        runOnce();
    }

    private void makeSnake(int[][] snake, byte[] data, int offset){
        int r = 0, c = 0;
        for (int i = offset; i < offset + 128; i++) {
            String bits = Integer.toBinaryString(data[i] & 255 | 256).substring(1);
            for (char bit : bits.toCharArray()) {
                if (bit == '0') {
                    snake[r][c] = 0;
                } else {
                    snake[r][c] = 1;
                }
                c++;
                if (c == 32) {
                    c = 0;
                    r++;
                }
            }
        }
    }

    private void runOnce() {
        PlayerGameState1.get().setApple(apple);
        PlayerGameState1.get().setSnake(s1);
        PlayerGameState2.get().setApple(apple);
        PlayerGameState2.get().setSnake(s2);
        System.out.println("snake1 running: " + s1);
        System.out.println("snake2 running: " + s2);
        board.repaint();
    }

    private void sendInfoPacket(){
        try {
            System.out.println("Sending info packet");
            snd_buf = new byte[BUF_SIZE];
            int offset = writeFirst(player);
            byte[] b = ip.getAddress();
            write4Bytes(b,snd_buf,offset);
            offset+=4;
            write2Bytes(port,snd_buf,offset);
            offset+=2;
            System.out.println(new String(snd_buf));
            System.out.println(server_ip);
            DatagramPacket pkt = new DatagramPacket(snd_buf, offset, server_ip, server_port);
            clientSocket.send(pkt);
            System.out.println("Info packet sent");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDirPacket(int dir){
        snd_buf = new byte[BUF_SIZE];
        int offset = 0;
        snd_buf[offset++] = 3;
        snd_buf[offset++] = (byte)gameId.getBytes().length;
        snd_buf[offset++] = (byte)name.getBytes().length;
        writeBytes(gameId,snd_buf,offset);
        offset += gameId.getBytes().length;
        writeBytes(name,snd_buf,offset);
        offset += name.getBytes().length;
        snd_buf[offset++] = (byte)dir;
        try {
            DatagramPacket pkt = new DatagramPacket(snd_buf, offset, server_ip, server_port);
            clientSocket.send(pkt);
            System.out.println("Direction packet sent");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void writeBytes(String str, byte[] buf, int offset) {
        byte[] b = str.getBytes();
        for (int i = 0; i < b.length; i++) {
            buf[offset + i] = b[i];
        }
    }

    private int writeFirst(int type){
        int offset = 0;
        snd_buf[0] = (byte) type;
        snd_buf[1] = (byte) gameId.getBytes().length;
        snd_buf[2] = (byte) name.getBytes().length;
        offset += 3;
        writeBytes(gameId,snd_buf,offset);
        offset+=gameId.getBytes().length;
        writeBytes(name,snd_buf,offset);
        offset+=name.getBytes().length;
        return offset;
    }

    private void write2Bytes(int n, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte) (n & 0xFF);
        buf[offset+1] = (byte) ((n >> 8) & 0xFF);
    }

    private void write4Bytes(byte[] b, byte[] buf, int offset) throws IOException {
        for(int i=0; i<b.length;i++){
            buf[offset+i] = b[i];
        }
    }
}
