// File: TemporaryEffect.java
package logic;

public class TemporaryEffect {
    public final String name;
    public final long startTime;
    public final long durationMillis;
    public final long endTime;

    public final Runnable onApply;
    public final Runnable undo;
    public final Runnable onUpdate;

    public TemporaryEffect(String name, Runnable onApply, long durationMillis, Runnable undo, Runnable onUpdate) {
        this.name = name;
        this.onApply = onApply;
        this.undo = undo;
        this.onUpdate = onUpdate;
        this.startTime = System.currentTimeMillis();
        this.durationMillis = durationMillis;
        this.endTime = this.startTime + this.durationMillis;

        if (onApply != null) onApply.run();
    }

    public float getProgress() {
        long now = System.currentTimeMillis();
        float progress = (float)(endTime - now) / durationMillis;
        return Math.max(0, Math.min(1, progress));
    }


    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    public void applyUpdate() {
        if (onUpdate != null) onUpdate.run();
    }

    public void expire() {
        if (undo != null) undo.run();
    }
}
