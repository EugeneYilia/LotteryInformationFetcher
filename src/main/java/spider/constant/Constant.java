package spider.constant;

import java.util.ArrayList;

public class Constant {
    private Constant(){};
    public static final String USERNAME = "root";
    public static final String PASSWORD = "liuyichen";
    public static final String DATABASE_URL = "jdbc:mysql://localhost:3306/sports_lotteries?useUnicode=true&characterEncoding=utf-8";
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final ArrayList<String> COMPANY_NAME_ARRAYLIST = new ArrayList<String>();
    public static final String startPageNumber = "1501619";
    public static final String endPageNumber = "1501973";//http://1x2d.win007.com/1501973.js?visitDstTime=1

    public static final String MATCH_NAME_CN = "matchname_cn";
    public static final String MATCH_TIME = "MatchTime";
    public static final String HOME_TEAM_CN  = "hometeam_cn";
    public static final String GUEST_TEAM_CN = "guestteam_cn";

    static {
        COMPANY_NAME_ARRAYLIST.add("ManbetX");
        COMPANY_NAME_ARRAYLIST.add("澳门");
        COMPANY_NAME_ARRAYLIST.add("Crown");
        COMPANY_NAME_ARRAYLIST.add("Bet 365");
        COMPANY_NAME_ARRAYLIST.add("易胜博");
        COMPANY_NAME_ARRAYLIST.add("韦德");
        COMPANY_NAME_ARRAYLIST.add("明陞");
        COMPANY_NAME_ARRAYLIST.add("10BET");
        COMPANY_NAME_ARRAYLIST.add("金宝博");
        COMPANY_NAME_ARRAYLIST.add("12BET");
        COMPANY_NAME_ARRAYLIST.add("利记sbobet");
        COMPANY_NAME_ARRAYLIST.add("盈禾");
        COMPANY_NAME_ARRAYLIST.add("18Bet");
        COMPANY_NAME_ARRAYLIST.add("立博");
    }
}
