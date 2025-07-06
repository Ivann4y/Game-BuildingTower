import data.*;
import logic.*;
import ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class NusantaraTower extends JPanel implements Runnable {
    private GameManager game;
    private CityManager city;
    private HUD hud;
    private UpgradeMenu upgradeMenu;
    private Thread gameThread;

    private Image backgroundImage;
    private BufferedImage balokAbu, balokUngu, balokJendela, balokAtap;

    public NusantaraTower() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        // Load background & block images
        backgroundImage = new ImageIcon(getClass().getResource("/assets/ville.png")).getImage();
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

        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        new Timer(10, e -> {
            if (getWidth() > 0 && getHeight() > 0 && game.towerStack.isEmpty()) {
                game.initGame();
                startNewTower();
                game.gameState = GameState.PLAYING;

                gameThread = new Thread(this);
                gameThread.start();
                ((Timer) e.getSource()).stop(); // hentikan timer
            }
        }).start();
    }

    private void startNewTower() {
        int w = getWidth();
        if (w <= 0) w = 800;
        int h = getHeight();
        if (h <= 0) h = 600;
        game.craneX = w / 2;
        game.craneDirection = 1;
        game.craneSpeedMultiplier = 1;
        game.blockIsFalling = false;
        game.gameState = GameState.PLAYING;
        game.blocksPlacedThisLevel = 0;

        resetTower(w, h); // kirim width & height ke resetTower
        prepareNextHangingBlock();
    }

    private void resetTower(int w, int h) {
        game.towerStack.clear();
        int baseX = (w / 2) - (game.baseBlockWidth / 2);
        int baseY = h - 50;
        if (baseY <= 0) baseY = 550;
        Block baseBlock = new Block(baseX, baseY, game.baseBlockWidth, 50, BlockType.PERUMAHAN, balokAbu);
        game.towerStack.push(baseBlock);
    }

    private void prepareNextHangingBlock() {
        game.blockIsFalling = false;
        BlockType nextType = game.getNextBlockType();
        int lastWidth = game.towerStack.peek().width;
        BufferedImage img = (game.blocksPlacedThisLevel == 14) ? balokAtap : getImageForType(nextType);

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
        if (game.gameState == GameState.PLAYING) {
            if (k == KeyEvent.VK_U) {
                game.showingUpgrades = !game.showingUpgrades;
            } else if (!game.showingUpgrades && k == KeyEvent.VK_SPACE && !game.blockIsFalling) {
                game.blockIsFalling = true;
            } else if (game.showingUpgrades && k >= KeyEvent.VK_1 && k <= KeyEvent.VK_9) {
                purchaseUpgrade(k - KeyEvent.VK_1);
            }
        } else if (k == KeyEvent.VK_ENTER) {
            switch (game.gameState) {
                case GAME_OVER -> {
                    game.initGame();
                    startNewTower();
                }
                case TOWER_COMPLETE -> placeTowerInCity();
                case TOWER_FAILED -> {
                    game.playerLives--;
                    if (game.playerLives <= 0) {
                        addFinalScore();
                        game.gameState = GameState.GAME_OVER;
                    } else {
                        startNewTower();
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
            if (!up.purchased && game.currentScore >= up.cost) {
                game.currentScore -= up.cost;
                up.purchased = true;
                up.effect.run();
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

        // Tambahkan balok atap tambahan di puncak (supaya tinggi tower pas)
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
        game.highScores.addFirst(new GameScore(game.currentScore));
        game.highScores.sort((a, b) -> Long.compare(b.score, a.score));
        while (game.highScores.size() > 5) game.highScores.removeLast();
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
            if (game.gameState == GameState.PLAYING && !game.showingUpgrades) {
                updateGame();
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void updateGame() {
        if (!game.blockIsFalling) {
            game.craneX += 3 * game.craneDirection * game.craneSpeedMultiplier;
            if (game.craneX > getWidth() - 150 || game.craneX < 150)
                game.craneDirection *= -1;

            game.hangingBlock.x = game.craneX - game.hangingBlock.width / 2;
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

                if (game.blocksPlacedThisLevel >= 15) {
                    game.gameState = GameState.TOWER_COMPLETE;
                } else {
                    prepareNextHangingBlock();
                }
            } else {
                game.gameState = GameState.TOWER_FAILED;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawCity(g2);
        drawTower(g2);
        if (game.gameState != GameState.GAME_OVER) drawCraneAndBlock(g2);
        hud.draw(g2, getWidth());

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
            }
            case TOWER_COMPLETE -> {
                title = "MENARA SELESAI!";
                sub = "Tekan [ENTER] untuk Lanjut";
            }
            case TOWER_FAILED -> {
                title = "MENARA GAGAL!";
                sub = "Tekan [ENTER] untuk Coba Lagi";
            }
        }
        int w = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - w) / 2, 150);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (getWidth() - sw) / 2, 200);
    }
}
