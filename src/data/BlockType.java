package data;

import java.awt.Color;

public enum BlockType {
    PERUMAHAN(new Color(220, 100, 100)),
    BISNIS(new Color(100, 100, 220)),
    TAMAN(new Color(100, 220, 100));

    public final Color color;

    BlockType(Color color) { this.color = color; }
}