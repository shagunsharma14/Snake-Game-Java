import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int INITIAL_DELAY = 75;
    static final int SPEED_INCREMENT = 5;
    static final int MIN_DELAY = 30;

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX, appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;
    boolean borderlessMode = false;
    int currentDelay = INITIAL_DELAY;

    // Golden Apple Properties
    boolean goldenAppleActive = false;
    int goldenAppleX, goldenAppleY;
    int goldenAppleTimer = 0;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        currentDelay = INITIAL_DELAY;
        timer = new Timer(currentDelay, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw red apple
            g.setColor(Color.RED);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw golden apple if active
            if (goldenAppleActive) {
                g.setColor(Color.YELLOW);
                g.fillOval(goldenAppleX, goldenAppleY, UNIT_SIZE, UNIT_SIZE);
            }

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Change border color when borderless mode is enabled
            g.setColor(borderlessMode ? Color.BLUE : Color.RED);
            g.drawRect(0, 0, SCREEN_WIDTH - 1, SCREEN_HEIGHT - 1);

            g.setColor(Color.red);
            g.setFont(new Font("Int Free", Font.BOLD, 25));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

        // Spawn golden apple every 5 apples
        if (applesEaten % 5 == 0 && applesEaten > 0) {
            spawnGoldenApple();
        }
    }

    public void spawnGoldenApple() {
        goldenAppleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        goldenAppleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
        goldenAppleActive = true;
        goldenAppleTimer = 50; // Golden apple disappears after 10 frames
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
        if (borderlessMode) {
            if (x[0] < 0) x[0] = SCREEN_WIDTH - UNIT_SIZE;
            else if (x[0] >= SCREEN_WIDTH) x[0] = 0;
            if (y[0] < 0) y[0] = SCREEN_HEIGHT - UNIT_SIZE;
            else if (y[0] >= SCREEN_HEIGHT) y[0] = 0;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
        // Check if player eats golden apple
        if (goldenAppleActive && (x[0] == goldenAppleX) && (y[0] == goldenAppleY)) {
            applesEaten += 5; // Extra points
            if (currentDelay > MIN_DELAY) {
                currentDelay -= SPEED_INCREMENT; // Increase speed
                timer.setDelay(currentDelay);
            }
            goldenAppleActive = false; // Remove golden apple
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        if (!borderlessMode) {
            if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
                running = false;
            }
        }
        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Int Free", Font.BOLD, 25));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

        g.setFont(new Font("Int Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        g.setFont(new Font("Int Free", Font.BOLD, 30));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press R to Restart", (SCREEN_WIDTH - metrics3.stringWidth("Press R to Restart")) / 2, SCREEN_HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();

            // Handle golden apple disappearance
            if (goldenAppleActive) {
                goldenAppleTimer--;
                if (goldenAppleTimer <= 0) {
                    goldenAppleActive = false;
                }
            }
        }
        repaint();
    }

    public void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        running = true;
        currentDelay = INITIAL_DELAY;
        timer.setDelay(currentDelay);
        goldenAppleActive = false;

        for (int i = 0; i < bodyParts; i++) {
            x[i] = 50 - (i * UNIT_SIZE);
            y[i] = 50;
        }
        newApple();
        timer.start();
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: if (direction != 'R') direction = 'L'; break;
                case KeyEvent.VK_RIGHT: if (direction != 'L') direction = 'R'; break;
                case KeyEvent.VK_UP: if (direction != 'D') direction = 'U'; break;
                case KeyEvent.VK_DOWN: if (direction != 'U') direction = 'D'; break;
                case KeyEvent.VK_R: if (!running) restartGame(); break;
                case KeyEvent.VK_B: borderlessMode = !borderlessMode; repaint(); break;
            }
        }
    }
}