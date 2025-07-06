package data;

import java.util.*;

public class UpgradeNode {
    public String name, description;
    public int cost;
    public boolean purchased = false;
    public Runnable effect;
    public List<UpgradeNode> children = new ArrayList<>();

    public UpgradeNode(String name, String desc, int cost, Runnable effect) {
        this.name = name; this.description = desc; this.cost = cost; this.effect = effect;
    }

    public void addChild(UpgradeNode child) { this.children.add(child); }
}