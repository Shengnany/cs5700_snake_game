package snake.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class DirectionChangeHandler extends KeyAdapter {
    private DatagramSocket clientSocket;
    private InetAddress server_ip;
    private int server_port;
    private static final int BUF_SIZE = 1024;
    public DirectionChangeHandler(DatagramSocket clientSocket, InetAddress server_ip, int server_port) {
        this.clientSocket = clientSocket;
        this.server_ip = server_ip;
        this.server_port = server_port;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                sendPacket(Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                sendPacket(Direction.RIGHT);
                break;
            case KeyEvent.VK_UP:
                sendPacket(Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                sendPacket(Direction.DOWN);
                break;
        }
    }
    private void sendPacket(Direction dir){
        byte[] buf = new byte[BUF_SIZE];
        int len = 0;

        try {
            DatagramPacket pkt = new DatagramPacket(buf, len, server_ip, server_port);
            clientSocket.send(pkt);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}


// client arguments:
// join [game_id] [nick_name] [IP] [port_number]
// bazel run :snake_app -- start_server
// The first player is responsible for creating the game, using the following command
// bazel run :snake_app -- create [game_id] [nick_name] [IP] [port_number]
// bazel run :snake_app -- join [game_id] [nick_name] [IP] [port_number]



// send: Keyevent
// reveive: while true

//    System.out.println("UDP echo client");
//    try (Scanner scanner = new Scanner(System.in);
//         DatagramSocket clientSocket = new DatagramSocket()) {
//      InetAddress addr = InetAddress.getByName("localhost");
//      while (true) {
//        System.out.print("Enter message: ");
//        String msg = scanner.nextLine();
//        if (msg.equals("quit")) {
//          break;
//        }
//        DatagramPacket pkt = new DatagramPacket(msg.getBytes(), msg.length(), addr, SERVER_PORT);
//        clientSocket.send(pkt);
//        // Recv echo msg.
//        byte[] buf = new byte[BUF_SIZE];
//        DatagramPacket echoPkt = new DatagramPacket(buf, BUF_SIZE);
//        clientSocket.receive(echoPkt);
//        String echoMsg = new String(echoPkt.getData());
//        System.out.println("Recv from server: " + echoMsg);
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }


//        scheduler.scheduleAtFixedRate(this::runOnce, /* initialDelay */ 0, GAME_SPEED_MS, MILLISECONDS);
//        addKeyListener(new DirectionChangeHandler ());
//
//    public void launchWaitingFrame(){
//        add(new WaitingBoard());
//    }
//    public void launchStartingFrame(){
//        add(new StartingBoard());
//    }


//            String[] s = ip.split(".");
//            byte[] b = new byte[4];
//            for(int i=0; i<s.length; i++){
//                b[i] = Byte.valueOf(s[i]);
//            }

/**
 * send = scheduler
 * receive: while truue
 */

//    try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
//      System.out.println("UDP echo server");
//      while (true) {
//        byte[] buf = new byte[BUF_SIZE];
//        DatagramPacket pkt = new DatagramPacket(buf, BUF_SIZE);
//        serverSocket.receive(pkt);
//        InetAddress addr = pkt.getAddress();
//        int port = pkt.getPort();
//        String msg = new String(pkt.getData());
//        System.out.println(String.format("[%s:%d]: %s", addr, port, msg));
//        // Echo back the same message.
//        DatagramPacket echoPkt = new DatagramPacket(msg.getBytes(), msg.length(), addr, port);
//        serverSocket.send(echoPkt);
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }


//public static int byteArrayToInt(byte[] b)
//{
//    return   b[3] & 0xFF |
//            (b[2] & 0xFF) << 8 |
//            (b[1] & 0xFF) << 16 |
//            (b[0] & 0xFF) << 24;
//}
//
//public static byte[] intToByteArray(int a)
//{
//    return new byte[] {
//        (byte) ((a >> 24) & 0xFF),
//        (byte) ((a >> 16) & 0xFF),
//        (byte) ((a >> 8) & 0xFF),
//        (byte) (a & 0xFF)
//    };
//}

//    public static byte[] intToByteArray(int a) {
//        return BigInteger.valueOf(a).toByteArray();
//    }
//
//    public static int byteArrayToInt(byte[] b) {
//        return new BigInteger(b).intValue();
//    }

//byte[] arr = { 0x01, 0x00, 0x00, 0x00, 0x48, 0x01};
//// say we want to consider indices 1, 2, 3, 4 {0x00, 0x00, 0x00, 0x48};
//ByteBuffer bf = ByteBuffer.wrap(arr, 1, 4); // big endian by default
//int num = bf.getInt();    // 72

// ByteBuffer newBuf = ByteBuffer.allocate(4);
//newBuf.putInt(num);
//byte[] bytes = newBuf.array();  // [0, 0, 0, 72] {0x48 = 72}