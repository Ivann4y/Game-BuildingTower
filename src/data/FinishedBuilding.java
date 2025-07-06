package data;

import java.awt.Point;

public class FinishedBuilding {
    public Point position;
    public int height;
    public BlockType type;

    public FinishedBuilding(Point p, int height, BlockType type) {
        this.position = p; this.height = height; this.type = type;
    }
}