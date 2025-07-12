package logic;

public class TemporaryEffect {
    public final String name;
    public final long startTime;
    public final long durationMillis;

    public final Runnable onApply;
    public final Runnable undo;
    public final Runnable onUpdate;

    private long pausedAt = -1;
    private long totalPausedTime = 0;

    public TemporaryEffect(String name, Runnable onApply, long durationMillis, Runnable undo, Runnable onUpdate) {
        this.name = name;
        this.onApply = onApply;
        this.undo = undo;
        this.onUpdate = onUpdate;
        this.startTime = System.currentTimeMillis();
        this.durationMillis = durationMillis;

        if (onApply != null) onApply.run();
    }

    public float getProgress() {
        long now = System.currentTimeMillis();
        long effectiveNow = now - totalPausedTime;

        if (pausedAt != -1) {
            effectiveNow -= (now - pausedAt);
        }

        float progress = (float) (durationMillis - (effectiveNow - startTime)) / durationMillis;
        return Math.max(0, Math.min(1, progress));
    }

    public void pause() {
        if (pausedAt == -1) {
            pausedAt = System.currentTimeMillis();
        }
    }

    public void resume() {
        if (pausedAt != -1) {
            long now = System.currentTimeMillis();
            totalPausedTime += (now - pausedAt);
            pausedAt = -1;
        }
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis();
        long effectiveNow = now - totalPausedTime;

        if (pausedAt != -1) {
            effectiveNow -= (now - pausedAt);
        }

        return effectiveNow >= (startTime + durationMillis);
    }

    public boolean isPaused() {
        return pausedAt != -1;
    }


    public void applyUpdate() {
        if (onUpdate != null) onUpdate.run();
    }

    public void expire() {
        if (undo != null) undo.run();
    }
}
