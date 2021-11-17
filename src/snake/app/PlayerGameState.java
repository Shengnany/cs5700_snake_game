package snake.app;

import static snake.app.Config.BOARD_SIZE;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class PlayerGameState {
    private static PlayerGameState INSTANCE;

    private Position apple;
    private LinkedList<Position> snake1;
    private LinkedList<Position> snake2;
    private int res;
    private String winner;

    private PlayerGameState() {
        initSnake();
        generateApple();
        res = -1;
        winner = null;
    }

    private void initSnake() {
        Position head1 = Position.random(BOARD_SIZE);
        Position head2 = Position.random(BOARD_SIZE);
        snake1 = Stream.of(head1,
                Position.copy(head1).move(Direction.DOWN),
                Position.copy(head1).move(Direction.DOWN).move(Direction.DOWN))
            .collect(Collectors.toCollection(LinkedList::new));
        snake2 = Stream.of(head2,
                Position.copy(head2).move(Direction.DOWN),
                Position.copy(head2).move(Direction.DOWN).move(Direction.DOWN))
            .collect(Collectors.toCollection(LinkedList::new));
        //a static method, className
        //an instance method, or className/objectName
        //a constructor, className
    }

    private void generateApple() {

            apple = Position.random(BOARD_SIZE);
            System.out.println("Generated apple");
    }
    // synchronized code,method,static method
    public static synchronized PlayerGameState get() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerGameState();
        }
        return INSTANCE;
    }

    public void setApple(Position apple) {
        this.apple = apple;
    }

    public Position getApple() {
        return apple;
    }

    public void setSnake1(LinkedList<Position> snake1) {
        this.snake1 = snake1;
    }

    public void setSnake2(LinkedList<Position> snake2) {
        this.snake2 = snake2;
    }

    public Stream<Position> getSnake1Position() {
        return snake1.stream();
    }
    public Stream<Position> getSnake2Position() {
        return snake2.stream();
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
