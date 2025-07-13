package ui;

import data.*;
import logic.GameManager;

import java.awt.*;

public class HUD {
    private GameManager gm;

    public HUD(GameManager gm) {
        this.gm = gm;
    }

    public void draw(Graphics2D g, int panelWidth) {
        int margin = 230;
        int posX = panelWidth - margin;

        // === Bagian Skor dan Nyawa ===

        g.setColor(Color.WHITE); // Ganti ke Color.WHITE kalau background kamu gelap
        g.setFont(new Font("Algerian", Font.BOLD, 24));
        g.drawString("Skor: " + gm.currentScore, posX, 100);
        g.drawString("Nyawa:   ", posX, 130);

        g.setColor(Color.RED);
        for (int i = 0; i < gm.playerLives; i++)
            g.fillOval(posX + 90 + (i * 30), 112, 20, 20);

        // === Bagian Legend (Kontrol) ===
        int legendX = posX;
        int legendY = 750;

        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.setColor(new Color(255, 255, 255, 180)); // transparan halus

        g.drawString("[SPACE]   Jatuhkan Balok", legendX, legendY);
        legendY += 24;
        g.drawString("[U]           Tampilkan Upgrade", legendX, legendY);
        legendY += 24;
        g.drawString("[ENTER]   Ulang", legendX, legendY);

    }
}
