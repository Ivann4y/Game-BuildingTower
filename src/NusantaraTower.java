import data.*;
import logic.*;
import ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List; // Import List

import audio.AudioPlayer;

public class NusantaraTower extends JPanel implements Runnable {
    private GameManager game;
    private CityManager city;
    private HUD hud;
    private UpgradeMenu upgradeMenu;
    private Thread gameThread;

    // MODIFIED: 'backgroundImage' is now dynamic
    private Image backgroundImage;
    private List<Image> levelBackgrounds; // NEW: List to hold all background images

    private BufferedImage balokAbu, balokUngu, balokJendela, balokAtap;
    private long lastUpdateTime = System.nanoTime();
    private AudioPlayer backgroundMusic;
    private Image mainMenuBackground;
    private String currentMusic = "";


    public NusantaraTower() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        backgroundMusic = new AudioPlayer();
        backgroundMusic.playSound("/assets/music/hadroh.wav", true);
        currentMusic = "hadroh";

        mainMenuBackground = new ImageIcon(getClass().getResource("/assets/mainMenu.gif")).getImage();

        // MODIFIED: Load all backgrounds at startup
        loadLevelBackgrounds();
        // Set the initial background image from the list
        if (!levelBackgrounds.isEmpty()) {
            backgroundImage = levelBackgrounds.get(0);
        } else {
            // Fallback if no images were loaded
            backgroundImage = new ImageIcon(getClass().getResource("/assets/ville.png")).getImage();
        }

        // Load block images
        try {
            balokAbu = ImageIO.read(getClass().getResource("/assets/balokAbu.png"));
            balokUngu = ImageIO.read(getClass().getResource("/assets/balokUngu.png"));
            balokJendela = ImageIO.read(getClass().getResource("/assets/balokJendela.png"));
            balokAtap = ImageIO.read(getClass().getResource("/assets/balokAtap.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        game = new GameManager();
        city = new CityManager();
        hud = new HUD(game);
        upgradeMenu = new UpgradeMenu(game);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
        requestFocusInWindow();

        game.initFirstBlock();
        gameThread = new Thread(this);
        gameThread.start();
    }

    // NEW: Method to load all background images into a list
    private void loadLevelBackgrounds() {
        levelBackgrounds = new ArrayList<>();
        // List of all background files based on your project structure
        String[] backgroundFiles = {
                "1.png", "2.png", "3.png", "4.png", "5.png",
                "6.png", "7.png", "8.png", "9.png", "10.png",
                "11.jpeg", "12.jpeg", "13.jpeg", "14.jpeg", "15.jpeg",
                "16.jpeg", "17.jpeg", "18.jpeg", "19.png", "20.jpeg", "21.jpeg"
        };

        for (String fileName : backgroundFiles) {
            try {
                java.net.URL imgURL = getClass().getResource("/assets/" + fileName);
                if (imgURL != null) {
                    levelBackgrounds.add(new ImageIcon(imgURL).getImage());
                } else {
                    System.err.println("Could not find background file: /assets/" + fileName);
                }
            } catch (Exception e) {
                System.err.println("Error loading background image: " + fileName);
                e.printStackTrace();
            }
        }
    }


    private int getTowerLeftLimit() {
        return 350;  // area city grid
    }

    private int getTowerRightLimit(int w) {
        return w - 300;  // area HUD
    }

    private int getTowerAreaCenter(int w) {
        return (getTowerLeftLimit() + getTowerRightLimit(w)) / 2;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> {
            game.initFirstBlock();
            gameThread = new Thread(this);
            gameThread.start();
        });
    }

    private void startNewTower() {
        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) {
            SwingUtilities.invokeLater(this::startNewTower);
            return;
        }

        game.craneX = w / 2;
        game.craneDirection = 1;
        game.craneSpeedMultiplier = 1;
        game.blockIsFalling = false;
        game.blocksPlacedThisLevel = 0;

        resetTower(w, h);
        prepareNextHangingBlock();
    }

    private void resetTower(int w, int h) {
        game.towerStack.clear();

        int baseX = getTowerAreaCenter(w) - (game.baseBlockWidth / 2);
        int baseY = h - 50;
        if (baseY <= 0) baseY = 550;

        Block baseBlock = new Block(baseX, baseY, game.baseBlockWidth, 50, BlockType.PERUMAHAN, balokAbu);
        game.towerStack.push(baseBlock);
    }


    private void prepareNextHangingBlock() {
        game.blockIsFalling = false;
        BlockType nextType = game.getNextBlockType();

        int lastWidth = game.activeWiderBlock ? game.baseBlockWidth + 20 : game.baseBlockWidth;
        int target = game.getTargetForLevel(game.currentLevel);
        BufferedImage img = (game.blocksPlacedThisLevel == target - 1) ? balokAtap : getImageForType(nextType);

        game.hangingBlock = new Block(
                game.craneX - (lastWidth / 2),
                100,
                lastWidth,
                50,
                nextType,
                img
        );
    }


    private BufferedImage getImageForType(BlockType type) {
        return switch (type) {
            case PERUMAHAN -> balokAbu;
            case BISNIS -> balokUngu;
            case TAMAN -> balokJendela;
            default -> balokAbu;
        };
    }

    private void handleInput(KeyEvent e) {
        int k = e.getKeyCode();

        if (game.gameState == GameState.MAIN_MENU) {
            if (k == KeyEvent.VK_ENTER) {
                game.gameState = GameState.PLAYING;
                startNewTower();

                if (!"playing".equals(currentMusic)) {
                    backgroundMusic.stop();
                    backgroundMusic.playSound("/assets/music/background.wav", true);
                    currentMusic = "playing";
                }
            } else if (k == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        } else if (game.gameState == GameState.PLAYING) {
            if (k == KeyEvent.VK_U) {
                game.showingUpgrades = !game.showingUpgrades;

                if (game.showingUpgrades) {
                    game.pauseAllEffects();
                } else {
                    game.resumeAllEffects();
                }
            }else if (!game.showingUpgrades && k == KeyEvent.VK_SPACE && !game.blockIsFalling) {
                game.blockIsFalling = true;
            } else if (game.showingUpgrades && k >= KeyEvent.VK_1 && k <= KeyEvent.VK_9) {
                purchaseUpgrade(k - KeyEvent.VK_1);
            }
        } else if (k == KeyEvent.VK_ENTER) {
            switch (game.gameState) {
                case GAME_OVER -> {
                    game.initGame();
                    startNewTower();
                    game.gameState = GameState.PLAYING;
                }
                case TOWER_COMPLETE -> {
                    placeTowerInCity();
                    game.gameState = GameState.PLAYING;
                }
                case TOWER_FAILED -> {
                    game.playerLives--;
                    if (game.playerLives <= 0) {
                        addFinalScore();
                        game.gameState = GameState.GAME_OVER;
                    } else {
                        startNewTower();
                        game.gameState = GameState.PLAYING;
                    }
                }
            }
        }
    }

    private void purchaseUpgrade(int index) {
        java.util.List<UpgradeNode> list = new java.util.ArrayList<>();
        collectAvailable(game.upgradeTreeRoot, list);
        if (index < list.size()) {
            UpgradeNode up = list.get(index);
            if (game.currentScore >= up.cost) {
                game.currentScore -= up.cost;
                up.effect.run();
                prepareNextHangingBlock();
            }
        }
    }

    private void collectAvailable(UpgradeNode node, java.util.List<UpgradeNode> list) {
        if (!node.purchased) list.add(node);
        else for (UpgradeNode c : node.children) if (!c.purchased) list.add(c);
    }

    private void placeTowerInCity() {
        if (city.nextCityPlot.y >= city.CITY_HEIGHT) {
            addFinalScore();
            game.gameState = GameState.GAME_OVER;
            return;
        }

        Block top = game.towerStack.peek();
        Block roof = new Block(top.x, top.y - top.height, top.width, top.height, top.type, balokAtap);
        game.towerStack.push(roof);

        FinishedBuilding fb = new FinishedBuilding(new Point(city.nextCityPlot), game.blocksPlacedThisLevel, top.type);
        city.cityGrid.put(new Point(city.nextCityPlot), fb);

        game.currentScore += city.calculateSynergyBonus(city.nextCityPlot);

        city.nextCityPlot.x++;
        if (city.nextCityPlot.x >= city.CITY_WIDTH) {
            city.nextCityPlot.x = 0;
            city.nextCityPlot.y++;
        }

        startNewTower();
    }

    private void addFinalScore() {
        if (game.currentScore <= 0) {
            game.isNewHighScore = false;
            return;
        }

        GameScore newScore = new GameScore(game.currentScore);
        game.highScores.add(newScore);

        game.highScores.sort((a, b) -> Long.compare(b.score, a.score));
        while (game.highScores.size() > 5) {
            game.highScores.removeLast();
        }
        game.isNewHighScore = game.highScores.contains(newScore);
        System.out.println("High Scores:");
        for (GameScore gs : game.highScores) {
            System.out.println(" - " + gs.score);
        }
    }

    private volatile boolean running = true;

    public void stopGame() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (getWidth() > 0 && getHeight() > 0) {
                if (game.gameState == GameState.PLAYING && !game.showingUpgrades) {
                    updateGame();
                }
                repaint();
            }
            try {
                Thread.sleep(16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        int w = getWidth();
        if (w <= 0) return; // panel belum siap

        game.updateTemporaryEffects();

        // ✅ Tambahkan pengecekan di sini:
        if (game.forceRefreshHangingBlock) {
            game.forceRefreshHangingBlock = false;
            prepareNextHangingBlock();
            System.out.println("prepareNextHangingBlock dipanggil - activeWiderBlock: " + game.activeWiderBlock);
        }

        int leftLimit = getTowerLeftLimit();
        int rightLimit = getTowerRightLimit(w);

        if (!game.blockIsFalling) {
            int baseSpeed = game.hasSlowerCrane ? 2 : 4;
            game.craneX += (int) (baseSpeed * game.craneDirection * game.craneSpeedMultiplier);

            if (game.craneX >= rightLimit) {
                game.craneX = rightLimit;
                game.craneDirection = -1;
            } else if (game.craneX <= leftLimit) {
                game.craneX = leftLimit;
                game.craneDirection = 1;
            }

            game.hangingBlock.x = game.craneX - (game.hangingBlock.width / 2);
        } else {
            game.hangingBlock.y += 5;
            checkCollision();
        }
    }



    private void checkCollision() {
        Block top = game.towerStack.peek();
        if (game.hangingBlock.y + game.hangingBlock.height >= top.y) {
            int overlap = Math.min(game.hangingBlock.x + game.hangingBlock.width, top.x + top.width)
                    - Math.max(game.hangingBlock.x, top.x);
            if (overlap > 20) {
                game.hangingBlock.y = top.y - game.hangingBlock.height;

                game.towerStack.push(game.hangingBlock);
                game.blocksPlacedThisLevel++;
                int centerDiff = Math.abs((game.hangingBlock.x + game.hangingBlock.width / 2) - (top.x + top.width / 2));
                int bonus = Math.max(0, 100 - centerDiff * 2);
                game.currentScore += 10 + bonus;

                if (game.blocksPlacedThisLevel >= game.getTargetForLevel(game.currentLevel)) {
                    game.currentLevel++;
                    game.gameState = GameState.TOWER_COMPLETE;

                    // MODIFIED: Change background when level increases
                    if (!levelBackgrounds.isEmpty()) {
                        // Use modulo to loop through backgrounds if the level exceeds the number of images
                        int newBackgroundIndex = (game.currentLevel - 1) % levelBackgrounds.size();
                        backgroundImage = levelBackgrounds.get(newBackgroundIndex);
                    }
                } else {
                    prepareNextHangingBlock();
                }
            } else {
                game.gameState = GameState.TOWER_FAILED;
            }
        }
    }

    private void drawEffectCountdownBars(Graphics2D g) {
        int x = getWidth() - 220;
        int y = 20;

        for (TemporaryEffect effect : game.activeTemporaryEffects) {
            float progress = effect.isPaused() ? 1.0f : effect.getProgress();


            g.setColor(new Color(100, 100, 100, 180));
            g.fillRoundRect(x, y, 200, 20, 10, 10);

            g.setColor(new Color(255, 200, 0));
            g.fillRoundRect(x, y, (int) (200 * progress), 20, 10, 10);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(effect.name, x + 5, y + 15);

            y += 30;
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (game.gameState == GameState.MAIN_MENU) {
            g.drawImage(mainMenuBackground, 0, 0, getWidth(), getHeight(), this);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String title = "NUSANTARA TOWER";
            int w = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (getWidth() - w) / 2, 200);

            g.setFont(new Font("Arial", Font.BOLD, 32));
            String startText = "Tekan [ENTER] untuk Mulai";
            w = g.getFontMetrics().stringWidth(startText);
            g.drawString(startText, (getWidth() - w) / 2, 300);

            String exitText = "Tekan [ESC] untuk Keluar";
            w = g.getFontMetrics().stringWidth(exitText);
            g.drawString(exitText, (getWidth() - w) / 2, 350);
            return;
        }

        // The background image drawn here is now updated dynamically
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawCity(g2);
        drawTower(g2);
        if (game.gameState != GameState.GAME_OVER) drawCraneAndBlock(g2);
        hud.draw(g2, getWidth());

        if (!game.activeTemporaryEffects.isEmpty()) {
            drawEffectCountdownBars(g2);
        }


        if (game.showingUpgrades) {
            upgradeMenu.draw(g2, getWidth());
        } else if (game.gameState != GameState.PLAYING) {
            drawEndScreen(g2);
        }
    }

    private void drawTower(Graphics2D g) {
        for (Block b : game.towerStack.getAllBlocks()) {
            if (b.image != null)
                g.drawImage(b.image, b.x, b.y, b.width, b.height, null);
            else {
                g.setColor(b.type.color.darker());
                g.fillRect(b.x, b.y, b.width, b.height);
            }
            g.setColor(Color.DARK_GRAY);
            g.drawRect(b.x, b.y, b.width, b.height);
        }
    }

    private void drawCraneAndBlock(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 75, getWidth(), 10);
        g.setColor(Color.GRAY);
        g.fillRect(game.craneX - 25, 70, 50, 25);
        g.setColor(Color.BLACK);
        g.drawLine(game.craneX, 85, game.hangingBlock.x + game.hangingBlock.width / 2, game.hangingBlock.y);

        if (game.hangingBlock.image != null)
            g.drawImage(game.hangingBlock.image, game.hangingBlock.x, game.hangingBlock.y, game.hangingBlock.width, game.hangingBlock.height, null);
        else {
            g.setColor(game.hangingBlock.type.color);
            g.fillRect(game.hangingBlock.x, game.hangingBlock.y, game.hangingBlock.width, game.hangingBlock.height);
        }
        g.setColor(Color.DARK_GRAY);
        g.drawRect(game.hangingBlock.x, game.hangingBlock.y, game.hangingBlock.width, game.hangingBlock.height);

        if (game.activeWiderBlock) {
            g.setColor(Color.ORANGE);
            g.drawRect(game.hangingBlock.x, game.hangingBlock.y, game.hangingBlock.width, game.hangingBlock.height);
        }
    }

    private void drawCity(Graphics2D g) {
        g.setColor(new Color(100, 150, 100));
        g.fillRect(20, 20, 260, 210);
        for (int y = 0; y < city.CITY_HEIGHT; y++)
            for (int x = 0; x < city.CITY_WIDTH; x++) {
                Point p = new Point(x, y);
                FinishedBuilding b = city.cityGrid.get(p);
                if (b != null) {
                    g.setColor(b.type.color);
                    g.fillRect(25 + x * 50, 25 + y * 50, 40, 40);
                    g.setColor(Color.WHITE);
                    g.drawString("" + b.height, 35 + x * 50, 45 + y * 50);
                } else {
                    g.setColor(new Color(80, 130, 80));
                    g.drawRect(25 + x * 50, 25 + y * 50, 40, 40);
                }
            }
    }

    private void drawEndScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));

        String title = "", sub = "";
        switch (game.gameState) {
            case GAME_OVER -> {
                title = "GAME OVER";
                sub = "Tekan [ENTER] untuk Mulai Ulang";
                String currentScoreText = "Skor Kamu: " + game.currentScore;
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 26));
                int cw = g.getFontMetrics().stringWidth(currentScoreText);
                g.drawString(currentScoreText, (getWidth() - cw) / 2, 240);

                if (game.isNewHighScore) {
                    g.setColor(Color.YELLOW);
                    g.setFont(new Font("Arial", Font.BOLD, 28));
                    String notif = "New High Score!";
                    int nw = g.getFontMetrics().stringWidth(notif);
                    g.drawString(notif, (getWidth() - nw) / 2, 275);
                }
            }
            case TOWER_COMPLETE -> {
                title = "MENARA SELESAI!";
                sub = "Tekan [ENTER] untuk Lanjut";
                String currentScoreText = "Skor Kamu: " + game.currentScore;
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 26));
                int cw = g.getFontMetrics().stringWidth(currentScoreText);
                g.drawString(currentScoreText, (getWidth() - cw) / 2, 240);

            }
            case TOWER_FAILED -> {
                title = "MENARA GAGAL!";
                sub = "Tekan [ENTER] untuk Coba Lagi";
                String currentScoreText = "Skor Kamu: " + game.currentScore;
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 26));
                int cw = g.getFontMetrics().stringWidth(currentScoreText);
                g.drawString(currentScoreText, (getWidth() - cw) / 2, 240);
            }
        }

        int w = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - w) / 2, 150);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (getWidth() - sw) / 2, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("Top 5 High Scores:", 30, getHeight() - 100);

        int y = getHeight() - 80;
        int rank = 1;
        for (GameScore gs : game.highScores) {
            g.drawString(rank + ". " + gs.score, 30, y);
            y += 20;
            rank++;
            if (rank > 5) break;
        }
    }
}