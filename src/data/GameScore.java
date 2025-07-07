package data;

public class GameScore {
    public long score;

    public GameScore(long score) {
        this.score = score;
    }



    // Supaya bisa dibandingkan pakai indexOf() saat ngecek high score
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameScore that = (GameScore) obj;
        return score == that.score;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(score);
    }
}