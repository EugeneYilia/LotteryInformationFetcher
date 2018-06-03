package spider.lottery.football;

import spider.constant.Constant;
import spider.dao.DatabaseConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class Win_Draw_Lose_Odds {
    private ArrayList<Company_Touple> company_touples = new ArrayList<>();

    private ArrayList<Double> initial_win_odds_arrayList = new ArrayList<>();
    private ArrayList<Double> initial_draw_odds_arrayList = new ArrayList<>();
    private ArrayList<Double> initial_lose_odds_arrayList = new ArrayList<>();
    private ArrayList<Double> current_win_odds_arrayList = new ArrayList<>();
    private ArrayList<Double> current_draw_odds_arrayList = new ArrayList<>();
    private ArrayList<Double> current_lose_odds_arrayList = new ArrayList<>();

    private ArrayList<Time_Odds> win_odds_process = new ArrayList<>();
    private ArrayList<Time_Odds> draw_odds_process = new ArrayList<>();
    private ArrayList<Time_Odds> lose_odds_process = new ArrayList<>();

    private double initial_win_average = 0;
    private double initial_draw_average = 0;
    private double initial_lose_average = 0;
    private double current_win_average = 0;
    private double current_draw_average = 0;
    private double current_lose_average = 0;

    private double initial_win_sactter = 0;
    private double initial_draw_scatter = 0;
    private double initial_lose_scatter = 0;
    private double current_win_scatter = 0;
    private double current_draw_scatter = 0;
    private double current_lose_scatter = 0;

    private String content;
    private String matchName;
    private String matchTime;
    private String hometeam_cn;
    private String guestteam_cn;

    private String process_win_json_array, process_draw_json_array, process_lose_json_array = "";

    DecimalFormat decimalFormat = new DecimalFormat("0.0");//保留一位小数，四舍五入

    public void start() {
        Date startDate = new Date();
        saveInformationsToDatabase();
        Date endDate = new Date();
        System.out.println("最后一次更新完数据时间为" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        Long timeDifference = endDate.getTime() - startDate.getTime();
        System.out.println("执行一次爬取所用的时间为" + (timeDifference / (1000 * 60)) + "分" + (timeDifference / 1000) + "秒");
        //testSaveToDatabase();
    }

    public int count;

    private void saveInformationsToDatabase() {//problem url -> 1501837
        count = 1;
        for (long i = Long.parseLong(Constant.startPageNumber); i <= Long.parseLong(Constant.endPageNumber); i++) {
            String url = "http://1x2d.win007.com/" + i + ".js?visitDstTime=1";
            saveLotteryInformation(url);
            System.out.println("当前url为" + url);
            count++;
        }
        //String url = "http://1x2d.win007.com/" + "1501719" + ".js?visitDstTime=1";
        //saveLotteryInformation(url);
    }

    public static void main(String[] args) {
        Win_Draw_Lose_Odds win_draw_lose_odds = new Win_Draw_Lose_Odds();
        win_draw_lose_odds.testSaveOneInformation();
    }

    private void testSaveOneInformation() {
        String url = "http://1x2d.win007.com/1501837.js?visitDstTime=1";
        System.out.println("当前url为" + url);
        saveLotteryInformation(url);
        System.out.println("Finished!!");
    }

    public void testSaveToDatabase() {
        saveLotteryInformation("http://1x2d.win007.com/1501619.js?visitDstTime=1");
        System.out.println("测试成功");
    }

    private void saveLotteryInformation(String sourceUrl) {
        company_touples.clear();

        initial_win_odds_arrayList.clear();
        initial_draw_odds_arrayList.clear();
        initial_lose_odds_arrayList.clear();
        current_win_odds_arrayList.clear();
        current_draw_odds_arrayList.clear();
        current_lose_odds_arrayList.clear();

        win_odds_process.clear();
        draw_odds_process.clear();
        lose_odds_process.clear();

        initial_win_average = 0;
        initial_draw_average = 0;
        initial_lose_average = 0;
        current_win_average = 0;
        current_draw_average = 0;
        current_lose_average = 0;

        initial_win_sactter = 0;
        initial_draw_scatter = 0;
        initial_lose_scatter = 0;
        current_win_scatter = 0;
        current_draw_scatter = 0;
        current_lose_scatter = 0;
        try {
            URL url = new URL(sourceUrl);
            InputStream inputStream = url.openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder("");
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(readLine);
            }
            content = stringBuilder.toString();
            //System.out.println(content);
            content = getTargetString(content, Constant.MATCH_NAME_CN);
            matchName = content.substring(content.indexOf("\"") + 1, content.indexOf(";") - 1);
            //System.out.println(matchName);

            content = getTargetString(content, Constant.MATCH_TIME);
            matchTime = content.substring(content.indexOf("\"") + 1, content.indexOf(";") - 1);
            //System.out.println(matchTime);

            content = getTargetString(content, Constant.HOME_TEAM_CN);
            hometeam_cn = content.substring(content.indexOf("\"") + 1, content.indexOf(";") - 1);
            //System.out.println(hometeam_cn);

            content = getTargetString(content, Constant.GUEST_TEAM_CN);
            guestteam_cn = content.substring(content.indexOf("\"") + 1, content.indexOf(";") - 1);
            //System.out.println(guestteam_cn);

            content = getTargetString(content, "var game=Array");
            content = content.substring(15);
            //System.out.println(content);

            //System.out.println("开始解析比赛");

            String tempContent = content.substring(0, content.indexOf("var gameDetail"));
            StringTokenizer stringTokenizer = new StringTokenizer(tempContent, "\"");
            //System.out.println(tempContent);
            while (true) {
                String toupleId = "";
                String companyName = "";
                String win_odds = "";
                String draw_odds = "";
                String lose_odds = "";

                String nextToken = stringTokenizer.nextToken();
                //System.out.println("nextToken->" + nextToken);
                if (nextToken.equals(");")) {
                    break;
                }
                String[] strings = nextToken.split("[|]");
                toupleId = strings[1];
                companyName = strings[2];
                win_odds = strings[3];
                draw_odds = strings[4];
                lose_odds = strings[5];
                //System.out.println("initialCompanyName->" + companyName);
                if (companyNameIsExist(companyName)) {
                    //System.out.println("remainingCompanyName->" + companyName);
                    Company_Touple company_touple = new Company_Touple();
                    company_touple.setToupleId(toupleId);
                    company_touple.setCompanyName(companyName);
                    company_touple.setMatchName(matchName);
                    company_touple.setMatchTime(matchTime);
                    company_touple.setHostTeam(hometeam_cn);
                    company_touple.setGuestTeam(guestteam_cn);

                    company_touple.setWin_odds(win_odds);
                    company_touple.setDraw_odds(draw_odds);
                    company_touple.setLose_odds(lose_odds);

                    company_touples.add(company_touple);
                }
                String line = stringTokenizer.nextToken();
                //System.out.println("line->" + line);
                if (line.contains(");")) {
                    break;
                }
            }
            //showCompanyTuples();
            if (company_touples.size() == 0) {
                return;
            }
            content = getTargetString(content, "var gameDetail=Array");
            content = content.substring(21);
            StringTokenizer stringTokenizer2 = new StringTokenizer(content, "\"");

            //System.out.println("开始解析比赛详情");

            while (true) {
                String toupleId;
                String nextToken = stringTokenizer2.nextToken();
                //System.out.println(nextToken);
                if (nextToken.contains(")")) {
                    break;
                }
                String[] strings = nextToken.split("[|;]");
                toupleId = strings[0].substring(0, strings[0].indexOf("^"));
                strings[0] = strings[0].substring(strings[0].indexOf("^") + 1);
                int i = 0;
                if (toupleIsExist(toupleId)) {
                    String jsonArray = "[";
                    while (true) {
                        if (i == 0) {

                            current_win_average += Double.parseDouble(strings[i]);
                            current_win_odds_arrayList.add(Double.parseDouble(strings[i]));

                            current_draw_average += Double.parseDouble(strings[i + 1]);
                            current_draw_odds_arrayList.add(Double.parseDouble(strings[i + 1]));

                            current_lose_average += Double.parseDouble(strings[i + 2]);
                            current_lose_odds_arrayList.add(Double.parseDouble(strings[i + 2]));

                        }
                        jsonArray += "{\"win_odds\":\"" + strings[i] + "\",";
                        jsonArray += "\"draw_odds\":\"" + strings[i + 1] + "\",";
                        jsonArray += "\"lose_odds\":\"" + strings[i + 2] + "\",";
                        jsonArray += "\"match_time\":\"" + strings[i + 3] + "\"}";
                        i = i + 7;
                        if (i > strings.length - 1) {
                            i = i - 7;
                            initial_win_average += Double.parseDouble(strings[i]);
                            initial_win_odds_arrayList.add(Double.parseDouble(strings[i]));

                            initial_draw_average += Double.parseDouble(strings[i + 1]);
                            initial_draw_odds_arrayList.add(Double.parseDouble(strings[i + 1]));

                            initial_lose_average += Double.parseDouble(strings[i + 2]);
                            initial_lose_odds_arrayList.add(Double.parseDouble(strings[i + 2]));
                            break;
                        } else {
                            jsonArray += ",";
                        }
                    }
                    jsonArray += "]";
                    //System.out.println(jsonArray);
                    for (int j = 0; j < company_touples.size(); j++) {
                        if (company_touples.get(j).getToupleId().equals(toupleId)) {
                            company_touples.get(j).setProcessOddsJson(jsonArray);
                            break;
                        }
                    }
                }
                String line = stringTokenizer2.nextToken();
                if (line.contains(")")) {
                    break;
                }
            }
            //printAll();

            //System.out.println("即将准备计算离散值");

            initial_win_average = initial_win_average / company_touples.size();
            //System.out.println("initial_win_average->" + initial_win_average);
            for (int i = 0; i < initial_win_odds_arrayList.size(); i++) {
                initial_win_sactter += Math.abs((double) initial_win_odds_arrayList.get(i) - (double) initial_win_average);
            }
            //System.out.println("initial_win_sactter->" + initial_win_sactter);

            initial_draw_average = initial_draw_average / company_touples.size();
            //System.out.println("initial_draw_average->" + initial_draw_average);
            for (int i = 0; i < initial_draw_odds_arrayList.size(); i++) {
                initial_draw_scatter += Math.abs((double) initial_draw_odds_arrayList.get(i) - (double) initial_draw_average);
            }
            //System.out.println("initial_draw_scatter->" + initial_draw_scatter);

            initial_lose_average = initial_lose_average / company_touples.size();
            //System.out.println("initial_lose_average->" + initial_lose_average);
            for (int i = 0; i < initial_lose_odds_arrayList.size(); i++) {
                initial_lose_scatter += Math.abs((double) initial_lose_odds_arrayList.get(i) - (double) initial_lose_average);
            }
            //System.out.println("initial_lose_scatter->" + initial_lose_scatter);


            current_win_average = current_win_average / company_touples.size();
            //System.out.println("current_win_average->" + current_win_average);
            for (int i = 0; i < current_win_odds_arrayList.size(); i++) {
                current_win_scatter += Math.abs((double) current_win_odds_arrayList.get(i) - (double) current_win_average);
            }
            //System.out.println("current_win_scatter->" + current_win_scatter);

            current_draw_average = current_draw_average / company_touples.size();
            //System.out.println("current_draw_average->" + current_draw_average);
            for (int i = 0; i < current_draw_odds_arrayList.size(); i++) {
                current_draw_scatter += Math.abs((double) current_draw_odds_arrayList.get(i) - (double) current_draw_average);
            }
            //System.out.println("current_draw_scatter->" + current_draw_scatter);

            current_lose_average = current_lose_average / company_touples.size();
            //System.out.println("current_lose_scatter->" + current_lose_average);
            for (int i = 0; i < current_lose_odds_arrayList.size(); i++) {
                current_lose_scatter += Math.abs((double) current_lose_odds_arrayList.get(i) - (double) current_lose_average);
            }
            //System.out.println("current_lose_scatter->" + current_lose_scatter);

            //System.out.println("即将初始化time_odds部分");
            ArrayList timePointArrayList = getTimePoint();
           /*
           for (int i = 0; i < timePointArrayList.size(); i++) {
                System.out.println(timePointArrayList.get(i));
            }
            */

            //System.out.println(getEarliestTime());
            //System.out.println("mostProcessOdds->" + getMostProcessOdds());
            setTimeOddsArrayList();
            //printTimeOddsArrayList();
            //System.out.println("即将构建json_array部分");
            process_win_json_array = "[";
            for (int i = 0; i < win_odds_process.size(); i++) {
                process_win_json_array += "{\"win_odds\":\"";
                process_win_json_array += decimalFormat.format(win_odds_process.get(i).getOdds());
                process_win_json_array += "\",\"time\":\"";
                process_win_json_array += win_odds_process.get(i).getTime();
                process_win_json_array += "\"}";
                if (i != win_odds_process.size() - 1) {
                    process_win_json_array += ",";
                }
            }
            process_win_json_array += "]";

            process_draw_json_array = "[";
            for (int i = 0; i < draw_odds_process.size(); i++) {
                process_draw_json_array += "{\"draw_odds\":\"";
                process_draw_json_array += decimalFormat.format(draw_odds_process.get(i).getOdds());
                process_draw_json_array += "\",\"time\":\"";
                process_draw_json_array += draw_odds_process.get(i).getTime();
                process_draw_json_array += "\"}";
                if (i != draw_odds_process.size() - 1) {
                    process_draw_json_array += ",";
                }
            }
            process_draw_json_array += "]";

            process_lose_json_array = "[";
            for (int i = 0; i < lose_odds_process.size(); i++) {
                process_lose_json_array += "{\"lose_odds\":\"";
                process_lose_json_array += decimalFormat.format(lose_odds_process.get(i).getOdds());
                process_lose_json_array += "\",\"time\":\"";
                process_lose_json_array += lose_odds_process.get(i).getTime();
                process_lose_json_array += "\"}";
                if (i != lose_odds_process.size() - 1) {
                    process_lose_json_array += ",";
                }
            }
            process_lose_json_array += "]";

            //System.out.println(company_touples.get(0).getProcessOddsJson());

            //System.out.println("即将持久化到数据库");
            try {
                //当要保存到数据库的成分为新数据的时候
                //保存进数据库   START
                saveToDatabase();
                System.out.println("第" + count + "条数据成功插入数据库");
                //保存进数据库   END
            } catch (SQLException e) {
                //当要保存到数据库的成分为已经存在的数据的时候
                //更新数据库里面的现有的数据  START
                updateToDatabase();
                System.out.println("第" + count + "条数据成功更新进数据库");
                //更新数据库里面的现有的数据  END
            }
            //printAllArrayListElements();
        } catch (FileNotFoundException e) {
            //访问页面没有数据
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("该网页没有内容");
        }
    }

    public void showCompanyTuples() {
        for (int i = 0; i < company_touples.size(); i++) {
            System.out.println(company_touples.get(i).getCompanyName());
        }
    }

    public void updateToDatabase() {
        Connection connection = DatabaseConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("update company_prediction set " +
                    "company_name = ?," +//1
                    "tuple_id = ?," +//2
                    "league_name = ?," +//3
                    "host_team_name = ?," +//4
                    "guest_team_name = ?," +//5
                    "match_time = ?," +//6
                    "initial_win_odds = ?," +//7
                    "initial_draw_odds = ?," +//8
                    "initial_lose_odds = ?," +//9
                    "current_win_odds = ?," +//10
                    "current_draw_odds = ?," +//11
                    "current_lose_odds = ? " +//12
                    "where tuple_id = ?");//13
            for (int i = 0; i < company_touples.size(); i++) {
                //update part
                preparedStatement.setString(1, company_touples.get(i).getCompanyName());
                preparedStatement.setString(2, company_touples.get(i).getToupleId());
                preparedStatement.setString(3, company_touples.get(i).getMatchName());
                preparedStatement.setString(4, company_touples.get(i).getHostTeam());
                preparedStatement.setString(5, company_touples.get(i).getGuestTeam());
                preparedStatement.setString(6, company_touples.get(i).getMatchTime());
                preparedStatement.setString(7, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialWinOdds(company_touples.get(i).getProcessOddsJson(), i)))));
                preparedStatement.setString(8, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialDrawOdds(company_touples.get(i).getProcessOddsJson(), i)))));
                preparedStatement.setString(9, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialLoseOdds(company_touples.get(i).getProcessOddsJson(), i)))));
                preparedStatement.setString(10, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentWinOdds(company_touples.get(i).getProcessOddsJson(), i)))));
                preparedStatement.setString(11, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentDrawOdds(company_touples.get(i).getProcessOddsJson(), i)))));
                preparedStatement.setString(12, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentLoseOdds(company_touples.get(i).getProcessOddsJson(), i)))));

                //where part
                preparedStatement.setString(13, company_touples.get(i).getToupleId());
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();

            PreparedStatement preparedStatement2 = connection.prepareStatement(
                    "update " +
                            "sports_match set " +
                            "league_name = ?," +//1
                            "host_team_name = ?," +//2
                            "guest_team_name = ?," +//3
                            "match_time = ?," +//4
                            "initial_win_odds = ?," +//5
                            "initial_draw_odds = ?," +//6
                            "initial_lose_odds = ?," +//7
                            "current_win_odds = ?," +//8
                            "current_draw_odds = ?," +//9
                            "current_lose_odds = ?," +//10
                            "initial_win_scatter = ?," +//11
                            "initial_draw_scatter = ?," +//12
                            "initial_lose_scatter = ?," +//13
                            "current_win_scatter = ?," +//14
                            "current_draw_scatter = ?," +//15
                            "current_lose_scatter  = ?," +//16
                            "process_win_json_array = ?," +//17
                            "process_draw_json_array = ?," +//18
                            "process_lose_json_array = ? " +//19
                            "where " +
                            "host_team_name = ? and " +//20
                            "guest_team_name = ? and " +//21
                            "match_time = ?");//22
            //update part
            preparedStatement2.setString(1, company_touples.get(0).getMatchName());
            preparedStatement2.setString(2, company_touples.get(0).getHostTeam());
            preparedStatement2.setString(3, company_touples.get(0).getGuestTeam());
            preparedStatement2.setString(4, company_touples.get(0).getMatchTime());
            preparedStatement2.setString(5, String.valueOf(decimalFormat.format(initial_win_average)));
            preparedStatement2.setString(6, String.valueOf(decimalFormat.format(initial_draw_average)));
            preparedStatement2.setString(7, String.valueOf(decimalFormat.format(initial_lose_average)));
            preparedStatement2.setString(8, String.valueOf(decimalFormat.format(current_win_average)));
            preparedStatement2.setString(9, String.valueOf(decimalFormat.format(current_draw_average)));
            preparedStatement2.setString(10, String.valueOf(decimalFormat.format(current_lose_average)));
            preparedStatement2.setString(11, String.valueOf(decimalFormat.format(initial_win_sactter)));
            preparedStatement2.setString(12, String.valueOf(decimalFormat.format(initial_draw_scatter)));
            preparedStatement2.setString(13, String.valueOf(decimalFormat.format(initial_lose_scatter)));
            preparedStatement2.setString(14, String.valueOf(decimalFormat.format(current_win_scatter)));
            preparedStatement2.setString(15, String.valueOf(decimalFormat.format(current_draw_scatter)));
            preparedStatement2.setString(16, String.valueOf(decimalFormat.format(current_lose_scatter)));
            preparedStatement2.setString(17, process_win_json_array);
            preparedStatement2.setString(18, process_draw_json_array);
            preparedStatement2.setString(19, process_lose_json_array);

            //where part
            preparedStatement2.setString(20, company_touples.get(0).getHostTeam());
            preparedStatement2.setString(21, company_touples.get(0).getGuestTeam());
            preparedStatement2.setString(22, company_touples.get(0).getMatchName());
            preparedStatement2.executeUpdate();
            preparedStatement2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveToDatabase() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("insert into company_prediction(" +
                "company_name," +//1
                "tuple_id," +//2
                "league_name," +//3
                "host_team_name," +//4
                "guest_team_name," +//5
                "match_time," +//6
                "initial_win_odds," +//7
                "initial_draw_odds," +//8
                "initial_lose_odds," +//9
                "current_win_odds," +//10
                "current_draw_odds," +//11
                "current_lose_odds" +//12
                ") values" +
                "(?,?,?," +
                "?,?,?," +
                "?,?,?," +
                "?,?,?)");
        for (int i = 0; i < company_touples.size(); i++) {
            preparedStatement.setString(1, company_touples.get(i).getCompanyName());
            preparedStatement.setString(2, company_touples.get(i).getToupleId());
            preparedStatement.setString(3, company_touples.get(i).getMatchName());
            preparedStatement.setString(4, company_touples.get(i).getHostTeam());
            preparedStatement.setString(5, company_touples.get(i).getGuestTeam());
            preparedStatement.setString(6, company_touples.get(i).getMatchTime());
            preparedStatement.setString(7, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialWinOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.setString(8, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialDrawOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.setString(9, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyInitialLoseOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.setString(10, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentWinOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.setString(11, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentDrawOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.setString(12, String.valueOf(decimalFormat.format(Double.parseDouble(getCompanyCurrentLoseOdds(company_touples.get(i).getProcessOddsJson(), i)))));
            preparedStatement.executeUpdate();
        }
        preparedStatement.close();

        PreparedStatement preparedStatement2 = connection.prepareStatement("insert into " +
                "sports_match(" +
                "league_name," +
                "host_team_name," +
                "guest_team_name," +
                "match_time," +
                "initial_win_odds," +
                "initial_draw_odds," +
                "initial_lose_odds," +
                "current_win_odds," +
                "current_draw_odds," +
                "current_lose_odds," +
                "initial_win_scatter," +
                "initial_draw_scatter," +
                "initial_lose_scatter," +
                "current_win_scatter," +
                "current_draw_scatter," +
                "current_lose_scatter," +
                "process_win_json_array," +
                "process_draw_json_array," +
                "process_lose_json_array) values" +
                "(?,?,?,?,?,?," +
                "?,?,?,?,?,?," +
                "?,?,?,?,?,?," +
                "?)");
        preparedStatement2.setString(1, company_touples.get(0).getMatchName());
        preparedStatement2.setString(2, company_touples.get(0).getHostTeam());
        preparedStatement2.setString(3, company_touples.get(0).getGuestTeam());
        preparedStatement2.setString(4, company_touples.get(0).getMatchTime());
        preparedStatement2.setString(5, String.valueOf(decimalFormat.format(initial_win_average)));
        preparedStatement2.setString(6, String.valueOf(decimalFormat.format(initial_draw_average)));
        preparedStatement2.setString(7, String.valueOf(decimalFormat.format(initial_lose_average)));
        preparedStatement2.setString(8, String.valueOf(decimalFormat.format(current_win_average)));
        preparedStatement2.setString(9, String.valueOf(decimalFormat.format(current_draw_average)));
        preparedStatement2.setString(10, String.valueOf(decimalFormat.format(current_lose_average)));
        preparedStatement2.setString(11, String.valueOf(decimalFormat.format(initial_win_sactter)));
        preparedStatement2.setString(12, String.valueOf(decimalFormat.format(initial_draw_scatter)));
        preparedStatement2.setString(13, String.valueOf(decimalFormat.format(initial_lose_scatter)));
        preparedStatement2.setString(14, String.valueOf(decimalFormat.format(current_win_scatter)));
        preparedStatement2.setString(15, String.valueOf(decimalFormat.format(current_draw_scatter)));
        preparedStatement2.setString(16, String.valueOf(decimalFormat.format(current_lose_scatter)));
        preparedStatement2.setString(17, process_win_json_array);
        preparedStatement2.setString(18, process_draw_json_array);
        preparedStatement2.setString(19, process_lose_json_array);
        preparedStatement2.executeUpdate();
        preparedStatement2.close();
    }

    public String getCompanyInitialWinOdds(String processJsonArray, int i) {
        //System.out.println("processJsonArray->"+processJsonArray);
        String initialWinOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size() - 1);
            //JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            initialWinOdds = initialJsonObject.getString("win_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            initialWinOdds = company_touples.get(i).getWin_odds();
        }
        return initialWinOdds;
    }

    public String getCompanyInitialDrawOdds(String processJsonArray, int i) {
        String initialDrawOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size() - 1);
            //JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            initialDrawOdds = initialJsonObject.getString("draw_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            initialDrawOdds = company_touples.get(i).getDraw_odds();
        }
        return initialDrawOdds;
    }

    public String getCompanyInitialLoseOdds(String processJsonArray, int i) {
        String initialLoseOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size() - 1);
            //JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            initialLoseOdds = initialJsonObject.getString("lose_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            initialLoseOdds = company_touples.get(i).getLose_odds();
        }
        return initialLoseOdds;
    }

    public String getCompanyCurrentWinOdds(String processJsonArray, int i) {
        String currentWinOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            //JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size()-1);
            JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            currentWinOdds = currentJsonObeject.getString("win_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            currentWinOdds = company_touples.get(i).getWin_odds();
        }
        return currentWinOdds;
    }

    public String getCompanyCurrentDrawOdds(String processJsonArray, int i) {
        String currentDrawOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            //JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size()-1);
            JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            currentDrawOdds = currentJsonObeject.getString("draw_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            currentDrawOdds = company_touples.get(i).getDraw_odds();
        }
        return currentDrawOdds;
    }

    public String getCompanyCurrentLoseOdds(String processJsonArray, int i) {
        String currentLoseOdds;
        if (processJsonArray != null && processJsonArray.length() != 0) {
            JSONArray jsonArray = JSON.parseArray(processJsonArray);
            //JSONObject initialJsonObject = jsonArray.getJSONObject(jsonArray.size()-1);
            JSONObject currentJsonObeject = jsonArray.getJSONObject(0);
            currentLoseOdds = currentJsonObeject.getString("lose_odds");
            //当只有一组数据的时候，初始jsonObject将和当前jsonObject为同一个jsonObject
        } else {
            currentLoseOdds = company_touples.get(i).getLose_odds();
        }
        return currentLoseOdds;
    }

    public void printTimeOddsArrayList() {
        System.out.println("-----------------WIN_ODDS_PROCESS START------------------");
        for (int i = 0; i < win_odds_process.size(); i++) {
            System.out.println(win_odds_process.get(i).getOdds() + "  " + win_odds_process.get(i).getTime());
        }
        System.out.println("-----------------WIN_ODDS_PROCESS END--------------------");

        System.out.println("-----------------DRAW_ODDS_PROCESS START------------------");
        for (int i = 0; i < draw_odds_process.size(); i++) {
            System.out.println(draw_odds_process.get(i).getOdds() + "  " + draw_odds_process.get(i).getTime());
        }
        System.out.println("-----------------DRAW_ODDS_PROCESS END--------------------");

        System.out.println("-----------------LOSE_ODDS_PROCESS START------------------");
        for (int i = 0; i < lose_odds_process.size(); i++) {
            System.out.println(lose_odds_process.get(i).getOdds() + "  " + lose_odds_process.get(i).getTime());
        }
        System.out.println("-----------------LOSE_ODDS_PROCESS END--------------------");

    }

    public void setTimeOddsArrayList() {
        Time_Odds first_time_win_odds = new Time_Odds();
        first_time_win_odds.setTime(getEarliestTime());
        first_time_win_odds.setOdds(initial_win_sactter);
        win_odds_process.add(first_time_win_odds);//离线值折线图所需要的数据存在这个数组里面

        Time_Odds first_time_draw_odds = new Time_Odds();
        first_time_draw_odds.setTime(getEarliestTime());
        first_time_draw_odds.setOdds(initial_draw_scatter);
        draw_odds_process.add(first_time_draw_odds);

        Time_Odds first_time_lose_odds = new Time_Odds();
        first_time_lose_odds.setTime(getEarliestTime());
        first_time_lose_odds.setOdds(initial_lose_scatter);
        lose_odds_process.add(first_time_lose_odds);

        //System.out.println("正在准备对不同时间点下的数据进行加载");
        for (int i = 1; i < getTimePoint().size(); i++) {//从第二个时间点开始对数据进行加载
            //System.out.println("目前是第" + i + "个时间点");

            String time = getTimePoint().get(i);
            ArrayList winValues = getWinValues(time);
            ArrayList drawValues = getDrawValues(time);
            ArrayList loseValues = getLoseValues(time);
            Double winScatter = getScatter(winValues);
            Double drawScatter = getScatter(drawValues);
            Double loseScatter = getScatter(loseValues);

            Time_Odds win_time_odds = new Time_Odds();
            win_time_odds.setTime(time);
            win_time_odds.setOdds(winScatter);
            win_odds_process.add(win_time_odds);

            Time_Odds draw_time_odds = new Time_Odds();
            draw_time_odds.setTime(time);
            draw_time_odds.setOdds(drawScatter);
            draw_odds_process.add(draw_time_odds);

            Time_Odds lose_time_odds = new Time_Odds();
            lose_time_odds.setTime(time);
            lose_time_odds.setOdds(loseScatter);
            lose_odds_process.add(lose_time_odds);
        }
    }

    public Double getScatter(ArrayList<Double> values) {
        double average = 0;
        double sum = 0;
        for (int i = 0; i < values.size(); i++) {
            sum += values.get(i);
        }
        average = sum / values.size();
        double scatter = 0;
        for (int i = 0; i < values.size(); i++) {
            scatter += Math.abs(values.get(i) - average);
        }
        return scatter;
    }

    public void printOne() {
        System.out.println(company_touples.get(0).getCompanyName());
        System.out.println(company_touples.get(0).getMatchName());
        System.out.println(company_touples.get(0).getMatchTime());
        System.out.println(company_touples.get(0).getHostTeam());
        System.out.println(company_touples.get(0).getGuestTeam());
        System.out.println(company_touples.get(0).getToupleId());
        System.out.println(company_touples.get(0).getProcessOddsJson());
    }

    public void printAllArrayListElements() {
        System.out.println("initial_win_odds_arraylist");
        printinitial_win_odds_arrayList();
        System.out.println("initial_draw_odds_arraylist");
        printinitial_draw_odds_arrayList();
        System.out.println("initial_lose_odds_arraylist");
        printinitial_lose_odds_arrayList();

        System.out.println("current_win_odds_arraylist");
        printcurrent_win_odds_arrayList();
        System.out.println("current_draw_odds_arraylist");
        printcurrent_draw_odds_arrayList();
        System.out.println("current_lose_odds_arraylist");
        printcurrent_lose_odds_arrayList();
    }

    public void printinitial_win_odds_arrayList() {
        for (int i = 0; i < initial_win_odds_arrayList.size(); i++) {
            System.out.println(initial_win_odds_arrayList.get(i));
        }
    }

    public void printinitial_draw_odds_arrayList() {
        for (int i = 0; i < initial_draw_odds_arrayList.size(); i++) {
            System.out.println(initial_draw_odds_arrayList.get(i));
        }
    }

    public void printinitial_lose_odds_arrayList() {
        for (int i = 0; i < initial_lose_odds_arrayList.size(); i++) {
            System.out.println(initial_lose_odds_arrayList.get(i));
        }
    }

    public void printcurrent_win_odds_arrayList() {
        for (int i = 0; i < current_win_odds_arrayList.size(); i++) {
            System.out.println(current_win_odds_arrayList.get(i));
        }
    }


    public void printcurrent_draw_odds_arrayList() {
        for (int i = 0; i < current_draw_odds_arrayList.size(); i++) {
            System.out.println(current_draw_odds_arrayList.get(i));
        }
    }


    public void printcurrent_lose_odds_arrayList() {
        for (int i = 0; i < current_lose_odds_arrayList.size(); i++) {
            System.out.println(current_lose_odds_arrayList.get(i));
        }
    }

    public void printAll() {

        for (int i = 0; i < company_touples.size(); i++) {
            System.out.println(company_touples.get(i).getCompanyName());
            System.out.println(company_touples.get(i).getMatchName());
            System.out.println(company_touples.get(i).getMatchTime());
            System.out.println(company_touples.get(i).getHostTeam());
            System.out.println(company_touples.get(i).getGuestTeam());
            System.out.println(company_touples.get(i).getToupleId());
            System.out.println(company_touples.get(i).getProcessOddsJson());
        }
    }

    public String getTargetString(String sourceString, String pattern) {
        sourceString = sourceString.substring(sourceString.indexOf(pattern));
        return sourceString;
    }

    public boolean companyNameIsExist(String sourceCompanyName) {
        boolean exist = false;
        for (int i = 0; i < Constant.COMPANY_NAME_ARRAYLIST.size(); i++) {
            if (Constant.COMPANY_NAME_ARRAYLIST.get(i).equalsIgnoreCase(sourceCompanyName)) {
                exist = true;
            }
        }
        return exist;
    }

    public boolean toupleIsExist(String sourceTupleId) {
        boolean exist = false;
        for (int i = 0; i < company_touples.size(); i++) {
            if (company_touples.get(i).getToupleId().equalsIgnoreCase(sourceTupleId)) {
                exist = true;
            }
        }
        return exist;
    }

    public ArrayList<Double> getWinValues(String time) {
        ArrayList<Double> winValues = new ArrayList();
        for (int i = 0; i < company_touples.size(); i++) {
            //System.out.println(company_touples.get(i).getProcessOddsJson());
            JSONArray jsonArray = JSON.parseArray(company_touples.get(i).getProcessOddsJson());
            int timeIndex = 0;//默认将返回第一个值
            if (jsonArray != null) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    if (jsonObject.getString("match_time").compareTo(time) <= 0) {
                        timeIndex = j;
                    }
                }

                //System.out.println(company_touples.get(i).getProcessOddsJson()+"  "+timeIndex+"  "+company_touples.get(i).getCompanyName()+"  "+company_touples.get(i).getMatchName());
                Double value = jsonArray.getJSONObject(timeIndex).getDouble("win_odds");
                winValues.add(value);
            } else {
                winValues.add(Double.parseDouble(company_touples.get(i).getWin_odds()));
            }
        }
        return winValues;
    }

    public ArrayList<Double> getDrawValues(String time) {
        ArrayList<Double> drawValues = new ArrayList();
        for (int i = 0; i < company_touples.size(); i++) {
            JSONArray jsonArray = JSON.parseArray(company_touples.get(i).getProcessOddsJson());
            int timeIndex = 0;
            if (jsonArray != null) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    if (jsonObject.getString("match_time").compareTo(time) <= 0) {
                        timeIndex = j;
                    }
                }

                Double value = jsonArray.getJSONObject(timeIndex).getDouble("draw_odds");
                drawValues.add(value);
            } else {
                drawValues.add(Double.parseDouble(company_touples.get(i).getDraw_odds()));
            }
        }
        return drawValues;
    }

    public ArrayList<Double> getLoseValues(String time) {
        ArrayList<Double> loseValues = new ArrayList();
        for (int i = 0; i < company_touples.size(); i++) {
            JSONArray jsonArray = JSON.parseArray(company_touples.get(i).getProcessOddsJson());
            int timeIndex = 0;
            if (jsonArray != null) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    if (jsonObject.getString("match_time").compareTo(time) <= 0) {
                        timeIndex = j;
                    }
                }

                Double value = jsonArray.getJSONObject(timeIndex).getDouble("lose_odds");
                loseValues.add(value);
            } else {
                loseValues.add(Double.parseDouble(company_touples.get(i).getLose_odds()));
            }
        }
        return loseValues;
    }

    public String getEarliestTime() {
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        String earliestTime = "";
        if (company_touples.size() > 0) {
            jsonArray = JSON.parseArray(company_touples.get(0).getProcessOddsJson());
            jsonObject = jsonArray.getJSONObject(0);
            earliestTime = jsonObject.getString("match_time");
            for (int i = 1; i < company_touples.size(); i++) {
                jsonArray = JSON.parseArray(company_touples.get(i).getProcessOddsJson());
                String currentMatchTime = "";
                if (jsonArray != null && jsonArray.size() > 0) {//只有一个值的公司会==null，此时该公司的预测时间点不会作为可能成为最早时间点的候选值
                    //System.out.println(jsonArray);
                    //时间点降序排序，最后一个jsonObject对象为最早的一个开奖时间点
                    jsonObject = jsonArray.getJSONObject(jsonArray.size() - 1);
                    currentMatchTime = jsonObject.getString("match_time");
                    if ((currentMatchTime.compareTo(earliestTime)) == -1) {
                        earliestTime = currentMatchTime;
                    }
                }
            }
        }
        return earliestTime;
    }

    public ArrayList<String> getTimePoint() {
        ArrayList<String> arrayList = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(getMostProcessOdds());
        //System.out.println(jsonArray);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            //System.out.println(jsonObject);
            //System.out.println(jsonObject.getString("match_time"));
            arrayList.add(jsonObject.getString("match_time"));
        }
        return arrayList;
    }

    public String getMostProcessOdds() {
        int maxIndex = 0;
        int maxNumber = 0;
        for (int i = 0; i < company_touples.size(); i++) {
            int currentCount = 0;
            if (i == 0) {
                for (int j = 0; j < company_touples.get(i).getProcessOddsJson().length(); j++) {
                    if (company_touples.get(i).getProcessOddsJson().charAt(j) == '{') {
                        currentCount++;
                    }
                }
                maxIndex = i;
                maxNumber = currentCount;
            } else {
                for (int j = 0; j < company_touples.get(i).getProcessOddsJson().length(); j++) {
                    if (company_touples.get(i).getProcessOddsJson().charAt(j) == '{') {
                        currentCount++;
                    }
                }
                if (currentCount > maxNumber) {
                    maxIndex = i;
                    maxNumber = currentCount;
                }
            }
        }
        return company_touples.get(maxIndex).getProcessOddsJson();
    }
}
