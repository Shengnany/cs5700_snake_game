package snake.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


public class SnakeApp{
    public static InetAddress serverIp;
    public static int serverPort  = 8080;

    // for multiple users use the same program to run client and server
    public static void main(String[] args) throws IOException, ServerErrorException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        serverIp = socket.getLocalAddress();
        System.out.println("server ip:" + serverIp);
        socket.close();
        if (args[0].equals("start_server")) {
            new SnakeServer(serverIp, serverPort).run();
            System.out.println("Starting server...");
        } else if (args[0].equals("create")) {
            String gameId = args[1];
            String name = args[2];
            InetAddress ip = InetAddress.getByName(args[3]);
            int port = Integer.parseInt(args[4]);
            if (serverIp == null) {
                throw new ServerErrorException("Server Not started");
            }
            new SnakePlayer(gameId, name, serverIp, serverPort, 1, ip, port).run();
        } else if (args[0].equals("join")) {
            String gameId = args[1];
            String name = args[2];
            InetAddress ip = InetAddress.getByName(args[3]);
            int port = Integer.parseInt(args[4]);
            if (serverIp == null) {
                throw new ServerErrorException("Server Not started");
            }
            new SnakePlayer(gameId, name, serverIp, serverPort, 2, ip, port).run();
        }
    }
}
