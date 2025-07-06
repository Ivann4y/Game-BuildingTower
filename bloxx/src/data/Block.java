package data;

import java.awt.image.BufferedImage;

public class Block {
    public int x, y, width, height;
    public BlockType type;          // cukup pakai BlockType, karena sudah diimport
    public BufferedImage image;

    public Block(int x, int y, int width, int height, BlockType type, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.image = image;
    }
}