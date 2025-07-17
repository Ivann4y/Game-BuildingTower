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
    public String playerUsername = "";
    public int usernameCharIndex = 0; // buat scroll huruf
    public char currentChar = 'A';


    public int blocksPlacedThisLevel = 0;
    public long currentScore = 0;
    public int playerLives = 3;
    public boolean blockIsFalling = false;
    public boolean showingUpgrades = false;
    public int craneX = 200;
    public int craneDirection = 1;
    public double craneSpeedMultiplier = 1.0;
    public int baseBlockWidth = 100;
    public long upgradeMenuOpenedAt = -1;
    public long totalUpgradePauseTime = 0;
    public boolean hasSlowerCrane = false;
    public boolean activeWiderBlock = false;
    public boolean forceRefreshHangingBlock = false;
    public boolean activeSlowCraneEffect = false;
    public boolean isPausedManually = false;


    public Block hangingBlock;
    public UpgradeNode upgradeTreeRoot;

    public int currentLevel = 1;

    public int getTargetForLevel(int level) {
        return 15 + (level - 1) * 1;
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

    private BlockType getRandomBlockType() {
        return BlockType.values()[new Random().nextInt(BlockType.values().length)];
    }


    public BlockType getNextBlockType() {
        // Cegah null: jika queue kosong, tambahkan dulu
        if (upcomingBlocks.isEmpty()) {
            upcomingBlocks.offer(getRandomBlockType());
        }

        BlockType nextType = upcomingBlocks.poll();
        upcomingBlocks.offer(getRandomBlockType());

        int width = activeWiderBlock ? baseBlockWidth + 20 : baseBlockWidth;

        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/assets/balokAbu.png"));
            hangingBlock = new Block(craneX - width / 2, 0, width, 50, nextType, image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nextType;
    }


    public void updateTemporaryEffects() {
        Iterator<TemporaryEffect> iter = activeTemporaryEffects.iterator();
        while (iter.hasNext()) {
            TemporaryEffect temp = iter.next();
            if (temp.isExpired()) {
                temp.expire();
                iter.remove();

                if (temp.name.equals("Balok Lebar")) {
                    forceRefreshHangingBlock = true;
                }
            } else if (!temp.isPaused()) {
                temp.applyUpdate();
            }
        }
    }



    private void buildUpgradeTree() {
        upgradeTreeRoot = new UpgradeNode("Root", "", 0, () -> {});
        upgradeTreeRoot.purchased = true;

        UpgradeNode slowCrane = new UpgradeNode("Crane Lambat", "Kecepatan -50% sementara", 500, () -> {
            boolean startPaused = showingUpgrades; // ✅ Pindah ke dalam sini

            TemporaryEffect slowEffect = new TemporaryEffect(
                    "Crane Lambat",
                    () -> {
                        craneSpeedMultiplier = 0.5;
                        activeSlowCraneEffect = true;
                    },
                    10000,
                    () -> {
                        craneSpeedMultiplier = 1;
                        activeSlowCraneEffect = false;
                    },
                    null,
                    startPaused
            );


            activeTemporaryEffects.add(slowEffect);
        });

        UpgradeNode widerBlock = new UpgradeNode("Balok Lebar", "Lebar +20 sementara", 800, () -> {
            boolean startPaused = showingUpgrades;

            TemporaryEffect wideEffect = new TemporaryEffect(
                    "Balok Lebar",
                    () -> {
                        this.activeWiderBlock = true;
                        refreshHangingBlock(); // <<< Tambahan: langsung perbarui ukuran
                    },
                    10000,
                    () -> {
                        this.activeWiderBlock = false;
                        refreshHangingBlock(); // <<< Tambahan: kembalikan ke normal saat efek habis
                    },
                    null,
                    startPaused
            );

            activeTemporaryEffects.add(wideEffect);
        });






        upgradeTreeRoot.addChild(slowCrane);
        upgradeTreeRoot.addChild(widerBlock);
    }


    public void pauseAllEffects() {
        for (TemporaryEffect effect : activeTemporaryEffects) {
            effect.pause();
        }
    }

    public void resumeAllEffects() {
        for (TemporaryEffect effect : activeTemporaryEffects) {
            effect.resume();
        }
    }

    public void refreshHangingBlock() {
        if (hangingBlock != null) {
            int width = activeWiderBlock ? baseBlockWidth + 20 : baseBlockWidth;
            hangingBlock.width = width;
            hangingBlock.x = craneX - (width / 2);
        }
    }




    // ✅ Tambahkan method baru untuk dipanggil ketika balok berhasil ditumpuk
    public void playSuccessSound() {
        successSound.playSound("/assets/sfx/stone-effect-254998.wav", false);
    }

    // ✅ Tambahkan method baru untuk dipanggil ketika balok gagal ditumpuk
    public void playFailSound() {
        failSound.playSound("/assets/sfx/game-fx-9-40197.wav", false);
    }
}
