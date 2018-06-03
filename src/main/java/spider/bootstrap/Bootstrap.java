package spider.bootstrap;


import spider.lottery.football.Win_Draw_Lose_Odds;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bootstrap {
    public static void main(String[] args) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            public void run() {
                new Win_Draw_Lose_Odds().start();
            }
        }, 0, 20, TimeUnit.MINUTES);
    }
}
