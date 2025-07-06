package ui;

import data.*;
import logic.GameManager;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UpgradeMenu {
    private GameManager gm;

    public UpgradeMenu(GameManager gm) { this.gm=gm; }

    public void draw(Graphics2D g, int width) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(150,100,500,400);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD,24));
        g.drawString("Menu Upgrade",320,140);
        g.setFont(new Font("Arial", Font.PLAIN,16));
        g.drawString("Skor Anda: "+gm.currentScore,330,170);

        List<UpgradeNode> list=new ArrayList<>();
        collectAvailable(gm.upgradeTreeRoot,list);
        int y=220,index=1;
        for(UpgradeNode node:list){
            String status=node.purchased?"[SUDAH DIBELI]":"["+node.cost+" Skor]";
            g.setColor(node.purchased||gm.currentScore<node.cost?Color.GRAY:Color.GREEN);
            if(node.purchased)
                g.drawString(node.name,180,y);
            else
                g.drawString("["+index+"] "+node.name+" "+status,180,y);
            y+=30;
            if(!node.purchased) index++;
        }

        g.setFont(new Font("Arial", Font.BOLD,18));
        g.setColor(Color.YELLOW);
        String back="Tekan [U] untuk Kembali";
        g.drawString(back,(width-g.getFontMetrics().stringWidth(back))/2,480);
    }

    private void collectAvailable(UpgradeNode node, List<UpgradeNode> list) {
        if(!node.purchased) list.add(node);
        else for(UpgradeNode child:node.children) if(!child.purchased) list.add(child);
    }
}
