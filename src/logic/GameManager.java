package logic;

import data.*;

import java.util.*;

import data.Block;
import data.BlockType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// ✅ Tambahkan import AudioPlayer
import audio.AudioPlayer;

public class GameManager {

    // ✅ Tambahkan deklarasi untuk efek suara
    private AudioPlayer successSound;
    private AudioPlayer failSound;

    public void initFirstBlock() {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/assets/balokAbu.png"));
            hangingBlock = new Block(100, 0, 50, 50, BlockType.PERUMAHAN, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CircularBlockList towerStack = new CircularBlockList(5);
    public Queue<BlockType> upcomingBlocks = new LinkedList<>();
    public List<TemporaryEffect> activeTemporaryEffects = new ArrayList<>();
    public List<GameScore> highScores = new LinkedList<>();
    public GameState gameState = GameState.PLAYING;
    public boolean isNewHighScore = false;

    public int blocksPlacedThisLevel = 0;
    public long currentScore = 0;
    public int playerLives = 3;
    public boolean blockIsFalling = false;
    public boolean showingUpgrades = false;
    public int craneX = 200;
    public int craneDirection = 1;
    public double craneSpeedMultiplier = 1.0;
    public int baseBlockWidth = 100;

    public Block hangingBlock;
    public UpgradeNode upgradeTreeRoot;

    public int currentLevel = 1;

    public int getTargetForLevel(int level) {
        return 15 + (level - 1) * 5;
    }

    public GameManager() {
        initGame();

        // ✅ Inisialisasi suara
        successSound = new AudioPlayer();
        failSound = new AudioPlayer();
        gameState = GameState.MAIN_MENU;
    }

    public void initGame() {
        buildUpgradeTree();
        for (int i = 0; i < 3; i++) upcomingBlocks.offer(getRandomBlockType());
        currentScore = 0;
        playerLives = 3;
        craneDirection = 1;
        craneSpeedMultiplier = 1;
        baseBlockWidth = 100;
        craneX = 400;
        showingUpgrades = false;
    }

    public BlockType getNextBlockType() {
        BlockType nextType = upcomingBlocks.poll();
        upcomingBlocks.offer(getRandomBlockType());
        return nextType;
    }

    private BlockType getRandomBlockType() {
        return BlockType.values()[new Random().nextInt(BlockType.values().length)];
    }

    public void updateTemporaryEffects() {
        if (showingUpgrades) return;

        long now = System.currentTimeMillis();
        Iterator<TemporaryEffect> iter = activeTemporaryEffects.iterator();
        while (iter.hasNext()) {
            TemporaryEffect temp = iter.next();
            if (temp.isExpired()) {
                if (temp.undo != null) temp.undo.run();
                iter.remove();
            } else if (temp.onUpdate != null) {
                temp.onUpdate.run();
            }
        }
    }

    private void buildUpgradeTree() {
        upgradeTreeRoot = new UpgradeNode("Root", "", 0, () -> {
        });
        upgradeTreeRoot.purchased = true;

        UpgradeNode slowCrane = new UpgradeNode("Crane Lambat", "Kecepatan -50% sementara", 500, () -> {
            TemporaryEffect slowEffect = new TemporaryEffect(
                    "Crane Lambat",
                    () -> craneSpeedMultiplier = 0.5,
                    10000,
                    () -> craneSpeedMultiplier = 1,
                    null
            );
            activeTemporaryEffects.add(slowEffect);
        });

        UpgradeNode widerBlock = new UpgradeNode("Balok Lebar", "Lebar +20", 800, () -> baseBlockWidth = 120);

        upgradeTreeRoot.addChild(slowCrane);
        upgradeTreeRoot.addChild(widerBlock);
    }

    // ✅ Tambahkan method baru untuk dipanggil ketika balok berhasil ditumpuk
    public void playSuccessSound() {
        successSound.playSound("/assets/sfx/stack_success.wav", false);
    }

    // ✅ Tambahkan method baru untuk dipanggil ketika balok gagal ditumpuk
    public void playFailSound() {
        failSound.playSound("/assets/sfx/stack_fail.wav", false);
    }
}
