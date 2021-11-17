package snake.app;

import javax.swing.*;
import java.awt.*;


public class PlayerBoard extends JPanel {
    private static final Image DOT = new ImageIcon("src/images/dot.png").getImage();
    private static final Image APPLE = new ImageIcon("src/images/apple.png").getImage();
    private static final Image HEAD = new ImageIcon("src/images/head.png").getImage();
    private int state = 0;
    public static final int WAIT = 0;
    public static final int READY = 1;
    public static final int START = 2;

    public void setState(int state) {
        this.state = state;
    }

    public PlayerBoard() {
        int size = Config.UNIT_SIZE * Config.BOARD_SIZE;
        setPreferredSize(new Dimension(size, size));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));
        setBackground(Color.BLACK);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(state == WAIT){
            renderWaiting(g);
        }
        else if(state == READY){
            renderStarting(g);
        }
        else if(state == START){
            renderApple(g);
            renderSnake(g);
            renderSnake2(g);
            if (PlayerGameState1.get().getResult() != -1) {
                renderGameOver(g);
            }
        }
    }
    private void renderApple(Graphics g) {
        Position p = PlayerGameState1.get().getApple();
        render(g, APPLE, p);
    }

    private void renderSnake(Graphics g) {
        PlayerGameState1.get()
                .getSnakePosition()
                .findFirst()
                .ifPresent(p -> render(g, DOT, p));
        PlayerGameState1.get()
                .getSnakePosition()
                .skip(1)
                .forEach(p -> render(g, DOT, p));
    }

    private void renderSnake2(Graphics g) {
        PlayerGameState2.get()
                .getSnakePosition()
                .findFirst()
                .ifPresent(p -> render(g, DOT, p));
        PlayerGameState2.get()
                .getSnakePosition()
                .skip(1)
                . forEach(p -> render(g, DOT, p));
    }


    private void render(Graphics g, Image image, Position p) {
        g.drawImage(image, p.getCol() * Config.UNIT_SIZE, p.getRow() * Config.UNIT_SIZE, this);
    }

    private void renderWaiting(Graphics g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        g.drawString("Waiting For opponent", 32, 32);
    }


    private void renderStarting(Graphics g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        g.drawString("Game is about to start", 32, 32);
    }

    private void renderGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
        String result = PlayerGameState1.get().getResult() == 0? "It is a draw" : "Winner is "+ PlayerGameState1.get().getWinner();
        g.drawString(result, 32, 32);
    }
}
