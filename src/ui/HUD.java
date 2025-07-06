package ui;

import data.*;
import logic.GameManager;
import java.awt.*;

public class HUD {
    private GameManager gm;

    public HUD(GameManager gm) { this.gm=gm; }

    public void draw(Graphics2D g, int panelWidth) {
        int margin = 250;
        int posX = panelWidth - margin;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Skor: " + gm.currentScore, posX, 100);
        g.drawString("Nyawa: ", posX, 130);

        g.setColor(Color.RED);
        for (int i = 0; i < gm.playerLives; i++)
            g.fillOval(posX + 90 + (i * 30), 112, 20, 20);
        // 112 karena tulisan "Nyawa:" ada di y=130, tingginya kira2 24px → turun dikit.

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Berikutnya:", posX, 180);

        int i = 0;
        for (BlockType type : gm.upcomingBlocks) {
            g.setColor(type.color);
            g.fillRect(posX, 190 + i * 25, 50, 20);
            i++;
        }
    }

}
