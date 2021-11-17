package snake.app;

import java.net.InetAddress;
import java.net.UnknownHostException;

class Config {
    public static final int BOARD_SIZE = 32;
    public static final int UNIT_SIZE = 10;
    public static final int GAME_SPEED_MS = 200;
    public static final int MOVE_SPEED_MS = 500;
    public static InetAddress SERVER_IP;
    public static final int SERVER_PORT = 8080;

    static {
        try {
            SERVER_IP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
