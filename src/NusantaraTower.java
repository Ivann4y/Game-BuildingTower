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
import java.util.List;

import audio.AudioPlayer;

public class NusantaraTower extends JPanel implements Runnable {
    private GameManager game;
    private CityManager city;
    private HUD hud;
    private UpgradeMenu upgradeMenu;
    private Thread gameThread;

    private Image backgroundImage;
    private List<Image> levelBackgrounds;

    private BufferedImage balokAbu, balokUngu, balokJendela, balokAtap;
    private AudioPlayer backgroundMusic;
    private Image mainMenuBackground;
    private String currentMusic = "";
    private volatile boolean running = true;

    public NusantaraTower() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        backgroundMusic = new AudioPlayer();
        backgroundMusic.playSound("/assets/music/hadroh.wav", true);
        currentMusic = "hadroh";

        mainMenuBackground = new ImageIcon(getClass().getResource("/assets/mainMenu.gif")).getImage();

        loadLevelBackgrounds();
        if (!levelBackgrounds.isEmpty()) {
            backgroundImage = levelBackgrounds.get(0);
        } else {
            backgroundImage = new ImageIcon(getClass().getResource("/assets/ville.png")).getImage();
        }

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
    }

    private void loadLevelBackgrounds() {
        levelBackgrounds = new ArrayList<>();
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
        return 350;
    }

    private int getTowerRightLimit(int w) {
        return w - 300;
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
        game.hangingBlock = new Block(game.craneX - (lastWidth / 2), 100, lastWidth, 50, nextType, img);
    }

    private BufferedImage getImageForType(BlockType type) {
        return switch (type) {
            case PERUMAHAN -> balokAbu;
            case BISNIS -> balokUngu;
            case TAMAN -> balokJendela;
            default -> balokAbu;
        };
    }

    // =========================================================================
    //                            KODE YANG DIPERBARUI
    // =========================================================================
    private void handleInput(KeyEvent e) {
        int k = e.getKeyCode();

        // MAIN MENU
        if (game.gameState == GameState.MAIN_MENU) {
            if (k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN) {
                game.mainMenuSelection = 1 - game.mainMenuSelection;
            } else if (k == KeyEvent.VK_ENTER) {
                if (game.mainMenuSelection == 0) { // Mulai
                    game.gameState = GameState.ENTER_USERNAME;
                    game.playerUsername = "";
                } else if (game.mainMenuSelection == 1) { // Keluar
                    System.exit(0);
                }
            } else if (k == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }

            // ENTER USERNAME
        } else if (game.gameState == GameState.ENTER_USERNAME) {
            char c = e.getKeyChar();
            if (k == KeyEvent.VK_ENTER) {
                if (!game.playerUsername.isEmpty()) {
                    game.gameState = GameState.PLAYING;
                    startNewTower();
                    if (!"playing".equals(currentMusic)) {
                        backgroundMusic.stop();
                        backgroundMusic.playSound("/assets/music/gamer-music-140-bpm-355954.wav", true);
                        currentMusic = "playing";
                    }
                }
            } else if (k == KeyEvent.VK_ESCAPE) {
                game.gameState = GameState.MAIN_MENU;
            } else if ((Character.isLetterOrDigit(c) || c == ' ') && game.playerUsername.length() < 12) {
                game.playerUsername += c;
            } else if (k == KeyEvent.VK_BACK_SPACE && !game.playerUsername.isEmpty()) {
                game.playerUsername = game.playerUsername.substring(0, game.playerUsername.length() - 1);
            }

            // PAUSED
        } else if (game.gameState == GameState.PAUSED) {
            if (k == KeyEvent.VK_ENTER) {
                game.isPausedManually = false;
                game.gameState = GameState.PLAYING;
                game.resumeAllEffects();
            } else if (k == KeyEvent.VK_ESCAPE) {
                game.initGame();
                game.gameState = GameState.MAIN_MENU;
                if (!"hadroh".equals(currentMusic)) {
                    backgroundMusic.stop();
                    backgroundMusic.playSound("/assets/music/hadroh.wav", true);
                    currentMusic = "hadroh";
                }
            }

            // PLAYING
        } else if (game.gameState == GameState.PLAYING) {
            if (k == KeyEvent.VK_P && !game.showingUpgrades) {
                game.isPausedManually = !game.isPausedManually;
                game.gameState = game.isPausedManually ? GameState.PAUSED : GameState.PLAYING;
                if (game.isPausedManually) game.pauseAllEffects();
                else game.resumeAllEffects();
            } else if (k == KeyEvent.VK_U) {
                game.showingUpgrades = !game.showingUpgrades;
                if (game.showingUpgrades) game.pauseAllEffects();
                else game.resumeAllEffects();
            } else if (!game.showingUpgrades && k == KeyEvent.VK_SPACE && !game.blockIsFalling) {
                game.blockIsFalling = true;
            } else if (game.showingUpgrades && k >= KeyEvent.VK_1 && k <= KeyEvent.VK_9) {
                purchaseUpgrade(k - KeyEvent.VK_1);
            }

            // OTHER STATES (Menunggu tombol ENTER)
        } else if (k == KeyEvent.VK_ENTER) {
            switch (game.gameState) {
                case GAME_COMPLETE: {
                    game.initGame();
                    game.gameState = GameState.MAIN_MENU;
                    if (!"hadroh".equals(currentMusic)) {
                        backgroundMusic.stop();
                        backgroundMusic.playSound("/assets/music/hadroh.wav", true);
                        currentMusic = "hadroh";
                    }
                    break;
                }
                case GAME_OVER: {
                    game.initGame();
                    startNewTower();
                    game.gameState = GameState.PLAYING;
                    break;
                }
                case TOWER_COMPLETE: {
                    placeTowerInCity();
                    if (game.gameState != GameState.GAME_COMPLETE) {
                        game.gameState = GameState.PLAYING;
                    }
                    break;
                }
                case TOWER_FAILED: {
                    game.playerLives--;
                    if (game.playerLives <= 0) {
                        addFinalScore();
                        game.gameState = GameState.GAME_OVER;
                    } else {
                        startNewTower();
                        game.gameState = GameState.PLAYING;
                    }
                    break;
                }
            }
        }
    }

    private void purchaseUpgrade(int index) {
        List<UpgradeNode> list = new ArrayList<>();
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

    private void collectAvailable(UpgradeNode node, List<UpgradeNode> list) {
        if (!node.purchased) list.add(node);
        else for (UpgradeNode c : node.children) if (!c.purchased) list.add(c);
    }

    private void placeTowerInCity() {
        Block top = game.towerStack.peek();
        FinishedBuilding fb = new FinishedBuilding(new Point(city.nextCityPlot), game.blocksPlacedThisLevel, top.type);
        city.cityGrid.put(new Point(city.nextCityPlot), fb);
        game.currentScore += city.calculateSynergyBonus(city.nextCityPlot);

        city.nextCityPlot.x++;
        if (city.nextCityPlot.x >= city.CITY_WIDTH) {
            city.nextCityPlot.x = 0;
            city.nextCityPlot.y++;
        }

        // Cek kondisi tamat SETELAH menempatkan menara
        if (city.nextCityPlot.y >= city.CITY_HEIGHT) {
            addFinalScore();
            game.gameState = GameState.GAME_COMPLETE;
        } else {
            startNewTower();
        }
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
    }

    public void stopGame() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double amountOfTicks = 60.0;
        final double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                if (getWidth() > 0 && getHeight() > 0 && game.gameState == GameState.PLAYING && !game.showingUpgrades) {
                    updateGame();
                }
                delta--;
            }
            if (running) {
                repaint();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        int w = getWidth();
        if (w <= 0) return;

        game.updateTemporaryEffects();

        if (game.forceRefreshHangingBlock) {
            game.forceRefreshHangingBlock = false;
            prepareNextHangingBlock();
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

            // Menggunakan nilai overlap yang lebih toleran
            if (overlap > 19) {
                game.hangingBlock.y = top.y - game.hangingBlock.height;
                game.towerStack.push(game.hangingBlock);
                game.blocksPlacedThisLevel++;
                int centerDiff = Math.abs((game.hangingBlock.x + game.hangingBlock.width / 2) - (top.x + top.width / 2));
                int bonus = Math.max(0, 100 - centerDiff * 2);
                game.currentScore += 10 + bonus;

                if (game.blocksPlacedThisLevel >= game.getTargetForLevel(game.currentLevel)) {
                    int totalCityPlots = city.CITY_WIDTH * city.CITY_HEIGHT;
                    int towersAlreadyBuilt = city.cityGrid.size();

                    if (towersAlreadyBuilt + 1 >= totalCityPlots) {
                        placeTowerInCity();
                    } else {
                        game.currentLevel++;
                        game.gameState = GameState.TOWER_COMPLETE;
                        if (!levelBackgrounds.isEmpty()) {
                            int newBackgroundIndex = (game.currentLevel - 1) % levelBackgrounds.size();
                            backgroundImage = levelBackgrounds.get(newBackgroundIndex);
                        }
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
            if (effect.name.equals("Balok Lebar")) g.setColor(new Color(255, 200, 0));
            else if (effect.name.equals("Crane Lambat")) g.setColor(new Color(100, 200, 255));
            else g.setColor(Color.GRAY);
            g.fillRoundRect(x, y, (int) (200 * progress), 20, 10, 10);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(effect.name, x + 5, y + 15);
            y += 30;
        }
    }

    private void drawPauseMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "PAUSE";
        int w = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - w) / 2, 150);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String resume = "[ENTER] Lanjut Main";
        String back = "[ESC] Kembali ke Menu";
        int rw = g.getFontMetrics().stringWidth(resume);
        int bw = g.getFontMetrics().stringWidth(back);
        g.drawString(resume, (getWidth() - rw) / 2, 250);
        g.drawString(back, (getWidth() - bw) / 2, 300);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (game.gameState == GameState.MAIN_MENU) {
            g.drawImage(mainMenuBackground, 0, 0, getWidth(), getHeight(), this);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Serif", Font.BOLD, 48));
            String title = "NUSANTARA TOWER";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (getWidth() - titleWidth) / 2, 200);

            String[] menuOptions = {"MULAI", "KELUAR"};
            Font menuFont = new Font("Serif", Font.BOLD, 36);
            g.setFont(menuFont);
            FontMetrics fm = g.getFontMetrics();
            int startY = 300;
            int menuSpacing = 60;

            for (int i = 0; i < menuOptions.length; i++) {
                String text = menuOptions[i];
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = startY + i * menuSpacing;
                if (i == game.mainMenuSelection) {
                    g.setColor(Color.YELLOW);
                    g.drawString(text, x, y);
                    int linePadding = 10;
                    int lineWidth = textWidth + (linePadding * 2);
                    int lineX = x - linePadding;
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(lineX, y - fm.getAscent() + 5, lineX + lineWidth, y - fm.getAscent() + 5);
                    g2d.drawLine(lineX, y + fm.getDescent() + 2, lineX + lineWidth, y + fm.getDescent() + 2);
                    drawSelectionIcon(g2d, x + textWidth + 15, y - (fm.getHeight() / 2) + fm.getDescent() + 5);
                } else {
                    g.setColor(Color.WHITE);
                    g.drawString(text, x, y);
                }
            }
            return;
        }

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        drawCity(g2d);
        drawTower(g2d);
        if (game.gameState != GameState.GAME_OVER && game.gameState != GameState.GAME_COMPLETE) {
            drawCraneAndBlock(g2d);
        }
        hud.draw(g2d, getWidth(), getHeight());
        if (!game.activeTemporaryEffects.isEmpty()) {
            drawEffectCountdownBars(g2d);
        }
        if (game.showingUpgrades) {
            upgradeMenu.draw(g2d, getWidth());
        } else if (game.gameState != GameState.PLAYING) {
            drawEndScreen(g2d);
        }
        if (game.gameState == GameState.PAUSED) {
            drawPauseMenu(g2d);
        }
        if (game.gameState == GameState.ENTER_USERNAME) {
            g.drawImage(mainMenuBackground, 0, 0, getWidth(), getHeight(), this);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String prompt = "Masukkan Nama Pemain";
            int pw = g.getFontMetrics().stringWidth(prompt);
            g.drawString(prompt, (getWidth() - pw) / 2, 200);
            g.setFont(new Font("Monospaced", Font.BOLD, 48));
            String usernameDisplay = game.playerUsername + "_";
            int uw = g.getFontMetrics().stringWidth(usernameDisplay);
            g.drawString(usernameDisplay, (getWidth() - uw) / 2, 270);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String info = "[ENTER] konfirmasi   [ESC] batal   [Bckspc] hapus";
            int iw = g.getFontMetrics().stringWidth(info);
            g.drawString(info, (getWidth() - iw) / 2, 340);
        }
    }

    private void drawSelectionIcon(Graphics2D g, int x, int y) {
        int diameter = 20;
        int radius = diameter / 2;
        Stroke oldStroke = g.getStroke();
        g.setColor(new Color(0, 150, 255));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x - radius, y - radius, diameter, diameter);
        int crossPadding = 5;
        g.drawLine(x - crossPadding, y - crossPadding, x + crossPadding, y + crossPadding);
        g.drawLine(x - crossPadding, y + crossPadding, x + crossPadding, y - crossPadding);
        g.setStroke(oldStroke);
    }

    private void drawTower(Graphics2D g) {
        for (Block b : game.towerStack.getAllBlocks()) {
            if (b.image != null) g.drawImage(b.image, b.x, b.y, b.width, b.height, null);
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
        if (game.hangingBlock.image != null) {
            g.drawImage(game.hangingBlock.image, game.hangingBlock.x, game.hangingBlock.y, game.hangingBlock.width, game.hangingBlock.height, null);
        } else {
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
        for (int y = 0; y < city.CITY_HEIGHT; y++) {
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
    }

    private void drawEndScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);

        String title = "", sub = "";

        switch (game.gameState) {
            case GAME_COMPLETE: {
                g.setFont(new Font("Serif", Font.BOLD, 96));
                g.setColor(Color.CYAN);
                title = "TAMAT";
                FontMetrics fmTitle = g.getFontMetrics();
                int titleWidth = fmTitle.stringWidth(title);
                g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 50);

                g.setFont(new Font("Arial", Font.BOLD, 28));
                g.setColor(Color.WHITE);
                String scoreText = "Skor Akhir: " + game.currentScore;
                FontMetrics fmScore = g.getFontMetrics();
                int scoreWidth = fmScore.stringWidth(scoreText);
                g.drawString(scoreText, (getWidth() - scoreWidth) / 2, getHeight() / 2 + 30);

                g.setFont(new Font("Arial", Font.BOLD, 24));
                String buttonText = "Tekan [ENTER] untuk Kembali ke Menu";
                FontMetrics fmButton = g.getFontMetrics();
                int buttonWidth = fmButton.stringWidth(buttonText);
                int buttonHeight = 40;
                int buttonPadding = 20;
                int rectWidth = buttonWidth + (buttonPadding * 2);
                int rectHeight = buttonHeight + (buttonPadding);
                int rectX = (getWidth() - rectWidth) / 2;
                int rectY = getHeight() - rectHeight - 60;

                g.setColor(new Color(255, 190, 0));
                g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 30, 30);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(rectX, rectY, rectWidth, rectHeight, 30, 30);
                g.setColor(Color.BLACK);
                g.drawString(buttonText, rectX + buttonPadding, rectY + buttonHeight);
                return;
            }
            case GAME_OVER: {
                title = "GAME OVER";
                sub = "Tekan [ENTER] untuk Mulai Ulang";
                // ... (sisanya sama dengan kode lama)
                break;
            }
            case TOWER_COMPLETE: {
                title = "MENARA SELESAI!";
                sub = "Tekan [ENTER] untuk Lanjut";
                // ...
                break;
            }
            case TOWER_FAILED: {
                title = "MENARA GAGAL!";
                sub = "Tekan [ENTER] untuk Coba Lagi";
                // ...
                break;
            }
        }

        // Default rendering untuk state lain
        g.setFont(new Font("Arial", Font.BOLD, 48));
        int w = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - w) / 2, 150);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (getWidth() - sw) / 2, 200);

        if (game.gameState == GameState.GAME_OVER || game.gameState == GameState.TOWER_FAILED || game.gameState == GameState.TOWER_COMPLETE) {
            String currentScoreText = "Skor Kamu: " + game.currentScore;
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 26));
            int cw = g.getFontMetrics().stringWidth(currentScoreText);
            g.drawString(currentScoreText, (getWidth() - cw) / 2, 240);

            if (game.isNewHighScore && game.gameState == GameState.GAME_OVER) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 28));
                String notif = "New High Score!";
                int nw = g.getFontMetrics().stringWidth(notif);
                g.drawString(notif, (getWidth() - nw) / 2, 275);
            }
        }

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