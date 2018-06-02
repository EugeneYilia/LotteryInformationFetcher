package spider.lottery.football;

public class Company_Touple {
    private String toupleId = "";
    private String matchName = "";//联盟名字
    private String matchTime = "";
    private String hostTeam = "";//主队名字
    private String guestTeam = "";//客队名字
    private String processOddsJson = "";
    private String companyName = "";
    private String win_odds = "";//当公司只有一个预测数据的时候
    private String draw_odds = "";
    private String lose_odds = "";


    public Company_Touple(){}

    public String getWin_odds() {
        return win_odds;
    }

    public void setWin_odds(String win_odds) {
        this.win_odds = win_odds;
    }

    public String getDraw_odds() {
        return draw_odds;
    }

    public void setDraw_odds(String draw_odds) {
        this.draw_odds = draw_odds;
    }

    public String getLose_odds() {
        return lose_odds;
    }

    public void setLose_odds(String lose_odds) {
        this.lose_odds = lose_odds;
    }

    public void setGuestTeam(String guestTeam) {
        this.guestTeam = guestTeam;
    }

    public void setHostTeam(String hostTeam) {
        this.hostTeam = hostTeam;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public void setProcessOddsJson(String processOddsJson) {
        this.processOddsJson = processOddsJson;
    }

    public void setToupleId(String toupleId) {
        this.toupleId = toupleId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGuestTeam() {
        return guestTeam;
    }

    public String getHostTeam() {
        return hostTeam;
    }

    public String getMatchName() {
        return matchName;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public String getProcessOddsJson() {
        return processOddsJson;
    }

    public String getToupleId() {
        return toupleId;
    }

    public String getCompanyName() {
        return companyName;
    }
}
