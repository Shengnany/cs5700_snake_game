package snake.app;

import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static snake.app.Config.BOARD_SIZE;


class ServerGameState {
    private static ServerGameState INSTANCE;
    private Position apple;
    private LinkedList<Position> snake1;
    private LinkedList<Position> snake2;
    private Direction dir1;
    private Direction dir2;
    private boolean gameOver;
    private int winner = -1;

    private ServerGameState() {
        initSnake();
        generateApple();
        dir1 = firstDir(snake1);
        dir2 = firstDir(snake2);
        gameOver = false;
    }

    //after initial create, set the direction as go straight
    private Direction firstDir(LinkedList<Position> snake) {
        Position first = snake.getFirst();
        Position second = snake.get(1);
        if (first.getRow() == second.getRow()) {
            if (first.getCol() - second.getCol() == 1) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        } else {
            if (first.getRow() - second.getRow() == 1) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        }
    }

    public static synchronized ServerGameState get() {
        if (INSTANCE == null) {
            INSTANCE = new ServerGameState();
        }
        return INSTANCE;
    }

    public Position getApple() {
        return apple;
    }

    public Stream<Position> getSnake1Position() {
        return snake1.stream();
    }
    public Stream<Position> getSnake2Position() {
        return snake2.stream();
    }

    public int gameOver() {
        return winner;
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
    }

    private void generateApple() {
        do {
            apple = Position.random(BOARD_SIZE);
        } while (isInSnake(snake1,apple) || isInSnake(snake2,apple));
    }

    public void updateDirection(int snake, Direction newDir) {
        if (snake == 1) {
            Position next = Position.copy(snake1.getFirst()).move(newDir);
            if (next.equals(snake1.get(1))) {
                return;
            }
            dir1 = newDir;
        } else {
            Position next = Position.copy(snake2.getFirst()).move(newDir);
            if (next.equals(snake2.get(1))) {
                return;
            }
            dir2 = newDir;
        }
    }

    public void moveSnake1() {
        if (gameOver) {
            return;
        }
        Position nextHead = Position.copy(snake1.getFirst()).move(dir1);

        // Eat itself
        if (isInSnakeButNotHeadOrTail(snake1, nextHead)) {
            System.out.println("isInSnakeButNotHeadOrTail" + ": snake1");
            gameOver = true;
            winner = 2;
            return;
        }

        // Head to head
        if(nextHead.equals(snake2.getFirst())){
            gameOver = true;
            winner = 0;
            return;
        }

        // Hit the wall
        if (Math.abs(snake1.getFirst().getRow() - nextHead.getRow()) > 1 || Math.abs(
            snake1.getFirst().getCol() - nextHead.getCol()) > 1) {
            gameOver = true;
            winner = 2;
            return;
        }

        // Hit another snake
        if (hitAnotherSnake(snake2, nextHead)) {
            gameOver = true;
            winner = 2;
            return;
        }

        snake1.addFirst(nextHead);
        if (nextHead.equals(apple)) {
            generateApple();
            return;
        }
        snake1.removeLast();
    }

    public void moveSnake2() {
        if (gameOver) {
            return;
        }
        Position nextHead = Position.copy(snake2.getFirst()).move(dir2);

        // Eat itself
        if (isInSnakeButNotHeadOrTail(snake2, nextHead)) {
            System.out.println("isInSnakeButNotHeadOrTail" + ": snake2");
            gameOver = true;
            winner = 1;
            return;
        }

        // Head to head
        if(nextHead.equals(snake1.getFirst())){
            gameOver = true;
            winner = 0;
            return;
        }

        // Hit the wall
        if (Math.abs(snake2.getFirst().getRow() - nextHead.getRow()) > 1 || Math.abs(
            snake2.getFirst().getCol() - nextHead.getCol()) > 1) {
            gameOver = true;
            winner = 1;
            return;
        }

        // Hit another snake
        if (hitAnotherSnake(snake1, nextHead)) {
            gameOver = true;
            winner = 1;
            return;
        }

        snake2.addFirst(nextHead);
        if (nextHead.equals(apple)) {
            generateApple();
            return;
        }
        snake2.removeLast();
    }

    private boolean isInSnake(LinkedList<Position> snake, Position o) {
        return snake.stream().anyMatch(p -> p.equals(o));
    }

    private boolean isInSnakeButNotHeadOrTail(LinkedList<Position> snake, Position o1) {
        for (Position p : snake) {
            if (p.equals(snake.getFirst())) {
                continue;
            }
            if (p.equals(o1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hitAnotherSnake(LinkedList<Position> anotherSnake, Position o1) {
        for (Position p : anotherSnake) {
            if (p.equals(o1)) {
                return true;
            }
        }
        return false;
    }
}
