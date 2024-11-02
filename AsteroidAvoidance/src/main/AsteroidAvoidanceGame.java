package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class AsteroidAvoidanceGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 600;
    static final int SPACESHIP_WIDTH = 50;
    static final int SPACESHIP_HEIGHT = 50;
    static final int ASTEROID_SIZE = 40;
    static final int BEAM_WIDTH = 5;
    static final int BEAM_HEIGHT = 20;
    static final int INITIAL_ASTEROIDS = 5;
    static final int GAME_SPEED = 30;
    static final int ALIEN_COUNT_PER_WAVE = 3;
    static final int ALIEN_SHOOT_INTERVAL = 40;
    static final int WAVE_DURATION = 600; // Wave duration in ticks (~20 seconds)
    static final int MAX_WAVES = 10;
    static final int ALIEN_BEAM_SPEED = 10;
    static final int ASTEROID_SPEED_INCREMENT = 1;
    static final int ALIEN_SPEED_INCREMENT = 1;

    int waveCounter = 0;
    int spaceshipX = SCREEN_WIDTH / 2 - SPACESHIP_WIDTH / 2;
    int spaceshipY = SCREEN_HEIGHT - SPACESHIP_HEIGHT - 30;
    int score = 0;
    boolean running = true;
    Timer timer;
    ArrayList<Asteroid> asteroids = new ArrayList<>();
    ArrayList<Alien> aliens = new ArrayList<>();
    ArrayList<Rectangle> beams = new ArrayList<>();
    Random rand = new Random();
    int alienBeamCooldown = 0;
    boolean paused = false;
    int highscore = 0;
    int wave = 1;
    int waveTimer = 0;
    
    // Increased speeds
    int alienSpeed = 3;    // Increased from 1 to 3
    int asteroidSpeed = 4; // Increased from 2 to 4
    int destroyedCount = 0;

    public AsteroidAvoidanceGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && spaceshipX > 0) {
                    spaceshipX -= 20;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && spaceshipX < SCREEN_WIDTH - SPACESHIP_WIDTH) {
                    spaceshipX += 20;
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    shootBeam();
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause();
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    restartGame();
                }
            }
        });
        initializeAsteroids();
        timer = new Timer(1000 / GAME_SPEED, this);
        timer.start();
    }

    public void togglePause() {
        paused = !paused;
        if (paused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    public void restartGame() {
        running = true;
        paused = false;
        wave = 1;
        score = 0;
        asteroidSpeed = 4; // Reset to new speed
        alienSpeed = 3;    // Reset to new speed
        destroyedCount = 0;
        asteroids.clear();
        aliens.clear();
        beams.clear();
        initializeAsteroids();
        timer.start();
        repaint();
    }

    public void initializeAsteroids() {
        for (int i = 0; i < INITIAL_ASTEROIDS; i++) {
            int x = rand.nextInt(SCREEN_WIDTH - ASTEROID_SIZE);
            asteroids.add(new Asteroid(x, -ASTEROID_SIZE));
        }
    }

    public void shootBeam() {
        beams.add(new Rectangle(spaceshipX + SPACESHIP_WIDTH / 2 - BEAM_WIDTH / 2, spaceshipY - BEAM_HEIGHT, BEAM_WIDTH, BEAM_HEIGHT));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!paused) {
            drawSpaceship(g);
            drawAsteroids(g);
            drawAliens(g);
            drawBeams(g);
            drawScore(g);
            if (!running) {
                drawGameOver(g);
            }
        } else {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            g.drawString("Paused", (SCREEN_WIDTH - metrics.stringWidth("Paused")) / 2, SCREEN_HEIGHT / 2);
        }
    }

    public void drawSpaceship(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillPolygon(new int[]{spaceshipX, spaceshipX + SPACESHIP_WIDTH / 2, spaceshipX + SPACESHIP_WIDTH},
                      new int[]{spaceshipY + SPACESHIP_HEIGHT, spaceshipY, spaceshipY + SPACESHIP_HEIGHT}, 3);
    }

    public void drawAsteroids(Graphics g) {
        for (Asteroid asteroid : asteroids) {
            g.setColor(Color.GRAY);
            g.fillPolygon(asteroid.getPolygon());
        }
    }

    public void drawAliens(Graphics g) {
        for (Alien alien : aliens) {
            g.setColor(Color.GREEN);
            g.fillRect(alien.x, alien.y, alien.width, alien.height);
        }
    }

    public void drawBeams(Graphics g) {
        g.setColor(Color.YELLOW);
        for (Rectangle beam : beams) {
            g.fillRect(beam.x, beam.y, BEAM_WIDTH, BEAM_HEIGHT);
        }
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Wave: " + wave, 10, 40);
        g.drawString("Highscore: " + highscore, 10, 60);
    }

    public void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        g.drawString("Game Over!", (SCREEN_WIDTH - metrics.stringWidth("Game Over!")) / 2, SCREEN_HEIGHT / 2);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press R to Restart", (SCREEN_WIDTH - metrics.stringWidth("Press R to Restart")) / 2, SCREEN_HEIGHT / 2 + 90);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            moveAsteroids();
            moveAliens();
            moveBeams();
            alienShootBeams();
            checkCollisions();
            checkWaveProgression();
        }
        repaint();
    }

    public void moveAsteroids() {
        for (Asteroid asteroid : asteroids) {
            asteroid.move(asteroidSpeed);
            if (asteroid.y > SCREEN_HEIGHT) {
                asteroid.y = -ASTEROID_SIZE;
                asteroid.x = rand.nextInt(SCREEN_WIDTH - ASTEROID_SIZE);
            }
        }
    }

    public void moveAliens() {
        for (int i = 0; i < aliens.size(); i++) {
            Alien alien = aliens.get(i);
            alien.move();
            if (alien.y > SCREEN_HEIGHT) {
                aliens.remove(i);
                i--; // Adjust index after removal
            }
        }
    }

    public void moveBeams() {
        for (int i = 0; i < beams.size(); i++) {
            Rectangle beam = beams.get(i);
            beam.y -= 10;
            if (beam.y < 0) {
                beams.remove(i);
                i--; // Adjust index after removal
            }
        }
    }

    public void alienShootBeams() {
        alienBeamCooldown++;
        if (alienBeamCooldown % ALIEN_SHOOT_INTERVAL == 0) {
            for (Alien alien : aliens) {
                alien.shootBeam(); // Implement alien shooting logic here if needed
            }
        }
    }

    public void checkCollisions() {
        Rectangle spaceshipRect = new Rectangle(spaceshipX, spaceshipY, SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
        for (Asteroid asteroid : asteroids) {
            if (spaceshipRect.intersects(asteroid.getPolygon().getBounds())) {
                running = false;
                highscore = Math.max(score, highscore);
            }
        }
        
        // Removed beam collision detection with asteroids
        for (int i = 0; i < beams.size(); i++) {
            Rectangle beam = beams.get(i);

            // Check collision with aliens
            for (int j = 0; j < aliens.size(); j++) {
                Alien alien = aliens.get(j);
                Rectangle alienRect = new Rectangle(alien.x, alien.y, alien.width, alien.height);
                if (beam.intersects(alienRect)) {
                    beams.remove(i);
                    i--; // Adjust index after removal
                    aliens.remove(j);
                    j--; // Adjust index after removal
                    score += 10; // Increment score for hitting an alien
                    destroyedCount++; // Keep track of destroyed aliens
                    break; // Break out of the inner loop
                }
            }
        }
    }

    public void checkWaveProgression() {
        waveTimer++;
        if (waveTimer >= WAVE_DURATION) {
            if (wave < MAX_WAVES) {
                wave++;
                waveTimer = 0;
                spawnAliensAndAsteroids();
            } else {
                running = false; // Game over after reaching max waves
                highscore = Math.max(score, highscore);
            }
        }
    }

    public void spawnAliensAndAsteroids() {
        for (int i = 0; i < ALIEN_COUNT_PER_WAVE; i++) {
            int x = rand.nextInt(SCREEN_WIDTH - 40);
            aliens.add(new Alien(x, -40, alienSpeed));
        }
        score += 100; // Increase score for progressing waves
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Asteroid Avoidance Game");
        AsteroidAvoidanceGame game = new AsteroidAvoidanceGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }
}

