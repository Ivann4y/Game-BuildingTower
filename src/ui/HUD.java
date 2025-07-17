package ui;

import data.*;
import logic.GameManager;

import java.awt.*;

public class HUD {
    private GameManager gm;

    public HUD(GameManager gm) {
        this.gm = gm;
    }

    public void draw(Graphics2D g, int panelWidth, int panelHeight) {
        int margin = 230;
        int posX = panelWidth - margin;

        // === Bagian Skor dan Nyawa ===
        g.setColor(Color.WHITE);
        g.setFont(new Font("Algerian", Font.BOLD, 24));
        g.drawString("Skor: " + gm.currentScore, posX, 100);
        g.drawString("Nyawa: ", posX, 130);

        g.setColor(Color.RED);
        for (int i = 0; i < gm.playerLives; i++) {
            g.fillOval(posX + 90 + (i * 30), 112, 20, 20);
        }

        // === Bagian Legend (Kontrol) ===
        int marginX = 30;
        int marginY = 30;
        int boxWidth = 280;
        int boxHeight = 50;


        int legendX = panelWidth - boxWidth - marginX; // pojok kanan
        int legendY = panelHeight - boxHeight - marginY; // pojok bawah

        g.setColor(new Color(255, 255, 255, 180)); // putih transparan
        g.fillRoundRect(legendX - 10, legendY - 20, boxWidth, boxHeight + 30, 15, 15);

        g.setFont(new Font("Verdana", Font.BOLD, 16)); // font yang lebih elegan
        g.setColor(Color.BLACK);

        int textY = legendY;
        g.drawString("[SPACE]   Jatuhkan Balok", legendX, textY);
        textY += 24;
        g.drawString("[U]           Tampilkan Upgrade", legendX, textY);
        textY += 24;;
        g.drawString("[P]            Pause", legendX, textY);
    }
}
