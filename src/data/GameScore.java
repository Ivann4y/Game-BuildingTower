package data;

import java.util.Date;

public class GameScore {
    public long score;
    public Date date;

    public GameScore(long score) {
        this.score = score; this.date = new Date();
    }
}
