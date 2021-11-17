package snake.app;

import java.util.LinkedList;
import java.util.stream.Stream;


class PlayerGameState1 {
    private static PlayerGameState1 INSTANCE;

    private Position apple;
    private LinkedList<Position> snake;
    private int res;
    private String winner;

    private PlayerGameState1() {
        res = -1;
        winner = null;
    }

    public static synchronized PlayerGameState1 get() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerGameState1();
        }
        return INSTANCE;
    }

    public void setApple(Position apple) {
        this.apple = apple;
    }

    public Position getApple() {
        return apple;
    }

    public void setSnake(LinkedList<Position> snake1) {
        this.snake = snake1;
    }


    public Stream<Position> getSnakePosition() {
        return snake.stream();
    }

    public int getResult() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }
}
