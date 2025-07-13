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
    private float pausedProgressSnapshot = -1;


    public TemporaryEffect(String name, Runnable onApply, long durationMillis, Runnable undo, Runnable onUpdate, boolean startPaused) {
        this.name = name;
        this.onApply = onApply;
        this.undo = undo;
        this.onUpdate = onUpdate;
        this.startTime = System.currentTimeMillis();
        this.durationMillis = durationMillis;

        if (onApply != null) onApply.run();
        if (startPaused) {
            this.pausedAt = System.currentTimeMillis();
        }
    }

    public float getProgress() {
        long now = System.currentTimeMillis();

        // Hitung waktu efektif sekarang, dikurangi total pause
        long effectiveNow = now - totalPausedTime;
        if (pausedAt != -1) {
            effectiveNow -= (now - pausedAt);
        }

        long elapsed = effectiveNow - startTime;

        float progress = 1.0f - (float) elapsed / durationMillis; // ✅ progress berkurang

        return Math.max(0, Math.min(1, progress)); // clamp antara 0 dan 1
    }


    private float getProgressInternal() {
        long now = System.currentTimeMillis();
        long effectivePausedTime = totalPausedTime;
        if (pausedAt != -1) {
            effectivePausedTime += now - pausedAt;
        }

        long effectiveElapsed = now - effectivePausedTime - startTime;
        float progress = (float) effectiveElapsed / durationMillis;
        return Math.max(0, Math.min(1, progress));
    }





    public boolean isExpired() {
        long now = System.currentTimeMillis();
        long effectiveNow = now - totalPausedTime;

        if (pausedAt != -1) {
            effectiveNow -= (now - pausedAt);
        }

        return effectiveNow >= startTime + durationMillis;
    }

    public boolean isPaused() {
        return pausedAt != -1;
    }

    public void pause() {
        if (pausedAt == -1) {
            pausedAt = System.currentTimeMillis();
            pausedProgressSnapshot = getProgressInternal(); // snapshot saat ini
        }
    }


    public void resume() {
        if (pausedAt != -1) {
            long now = System.currentTimeMillis();
            totalPausedTime += (now - pausedAt);
            pausedAt = -1;
            pausedProgressSnapshot = -1; // hapus snapshot karena lanjut lagi
        }
    }



    public void applyUpdate() {
        if (onUpdate != null) onUpdate.run();
    }

    public void expire() {
        if (undo != null) undo.run();
    }
}
