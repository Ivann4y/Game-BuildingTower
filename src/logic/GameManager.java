package logic;

import data.*;
import java.util.*;
import data.Block;
import data.BlockType;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GameManager {

    public void initFirstBlock() {
        try {
            BufferedImage image = ImageIO.read(getClass().getResource("/assets/balokAbu.png"));
            hangingBlock = new Block(100, 0, 50, 50, BlockType.PERUMAHAN, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    public Stack<Block> towerStack = new Stack<>();
    public CircularBlockList towerStack = new CircularBlockList(5);
    public Queue<BlockType> upcomingBlocks = new LinkedList<>();
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
    public int craneSpeedMultiplier = 1;
    public int baseBlockWidth = 100;

    public Block hangingBlock;
    public UpgradeNode upgradeTreeRoot;

    public GameManager() {
        initGame();
    }

    public void initGame() {
        buildUpgradeTree();
        for(int i=0; i<3; i++) upcomingBlocks.offer(getRandomBlockType());
        currentScore=0; playerLives=3; craneDirection=1; craneSpeedMultiplier=1; baseBlockWidth=100;
        craneX = 400; // nilai default, kira-kira tengah
        gameState=GameState.PLAYING; showingUpgrades=false;
    }

    public BlockType getNextBlockType() {
        BlockType nextType = upcomingBlocks.poll();
        upcomingBlocks.offer(getRandomBlockType());
        return nextType;
    }

    private BlockType getRandomBlockType() {
        return BlockType.values()[new Random().nextInt(BlockType.values().length)];
    }

    private void buildUpgradeTree() {
        upgradeTreeRoot = new UpgradeNode("Root", "", 0, ()->{});
        upgradeTreeRoot.purchased = true;
        UpgradeNode fastCrane = new UpgradeNode("Crane Cepat", "Kecepatan +50%", 500, () -> craneSpeedMultiplier=2);
        UpgradeNode widerBlock = new UpgradeNode("Balok Lebih Lebar", "Lebar +20", 800, () -> baseBlockWidth=120);
        upgradeTreeRoot.addChild(fastCrane); upgradeTreeRoot.addChild(widerBlock);
        fastCrane.addChild(new UpgradeNode("Crane Super Cepat", "Kecepatan +100%", 2000, () -> craneSpeedMultiplier=3));
    }
}
