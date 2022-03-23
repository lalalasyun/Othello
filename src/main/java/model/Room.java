package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

public class Room {
	private Session user1 = null;
	private Session user2 = null;

	private String name1 = null;
	private String name2 = null;

	private Othello game = new Othello();

	private boolean AI = false;
	private boolean aiturn = false; // black:true white

	private String initname = "guest1";

	private boolean changeturn = false;

	private static int guestNumber = 0;
	
	private List<String> record = new ArrayList<>();
	private List<String> stonedata = new ArrayList<>();
	private List<String> evadata = new ArrayList<>();
	private List<int[]> coorddata = new ArrayList<>();
	private int stonedataindex = 0;

	public Room() {
		AI = true;
		name1 = "guest" + guestNumber++;
		name2 = "AI";
		initname = name1;
	}

	public Room(String name) {
		AI = false;
		name1 = name;
		name2 = "guest";
	}

	public String getName1() {
		return name1;
	}

	public String getName2() {
		return name2;
	}

	public String getInitName() {
		return initname;
	}

	public String getName(Session session) {
		if (session == user1) {
			return name1;
		} else if (session == user2) {
			return name2;
		}
		return null;
	}

	public boolean isAI() {
		return AI;
	}

	public boolean isAiturn() {
		return aiturn;
	}

	public boolean isTurn(Session session) {
		if (user1 == session) {
			return getGame().getColor() ? true : false;
		} else if (user2 == session) {
			return getGame().getColor() ? false : true;
		}
		return false;
	}

	public void setName(Session session, String str) throws Exception {
		if (session == user1 && str != null) {
			name1 = str;
		} else if (session == user2 && str != null) {
			name2 = str;
		}
	}

	public void setUser(Session session) throws Exception {
		if (user1 == null) {
			user1 = session;
		} else if (user2 == null) {
			user2 = session;
		}
	}

	public void setAiturn() {
		if (!AI) {
			return;
		}
		if (name1.equals("AI")) {
			aiturn = true;
		} else if (name2.equals("AI")) {
			aiturn = false;
		}
	}

	public void changeTurn() {
		Session copyuser = user2;
		String copyname = name2;
		user2 = user1;
		name2 = name1;
		user1 = copyuser;
		name1 = copyname;
		setAiturn();
		changeturn = !changeturn;
	}

	public void removeSession(Session session) {
		user1 = user1 == session ? null : user1;
		user2 = user2 == session ? null : user2;
	}

	public void removeName(Session session, String initname) {
		if (user1 == session) {
			name1 = initname;
		} else {
			name2 = initname;
		}
	}

	public boolean removeRoom() {
		return (user1 == null && user2 == null);
	}

	public boolean isEmpty() {
		return !AI && (user1 == null || user2 != null) || (user1 != null || user2 == null);
	}

	public boolean searchSession(Session session) {
		return user1 == session || user2 == session;
	}

	public boolean searchUser(String name) {
		if (name.equals(name1) || name.equals(name2)) {
			return true;
		}
		return false;
	}

	public void sendMessage(String mess) throws Exception {
		if (user1 != null) {
			Thread.sleep(10);
			user1.getAsyncRemote().sendText(mess);
		}
		if (user2 != null) {
			Thread.sleep(10);
			user2.getAsyncRemote().sendText(mess);
		}
	}

	public void sendTurn() throws Exception {
		if (user1 != null) {
			Thread.sleep(10);
			user1.getAsyncRemote().sendText("turn,black");
		}
		if (user2 != null) {
			Thread.sleep(10);
			user2.getAsyncRemote().sendText("turn,white");
		}
	}

	public void sendName() throws Exception {
		String sendname1 = name1;
		String sendname2 = name2;
		if (name1.startsWith("guest")) {
			sendname1 = changeturn ? "guest2" : "guest1";
		}
		if (name2.startsWith("guest")) {
			sendname2 = changeturn ? "guest1" : "guest2";
		}
		sendMessage("name," + sendname1 + "," + sendname2);
	}

	public void sendResult() throws Exception {
		sendMessage("rate," + getResult(name1) + ",");
		sendMessage("rate,," + getResult(name2));
	}

	public Othello getGame() {
		return game;
	}

	public void timer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		final Runnable runnable = new Runnable() {
			int countdownStarter = 20;

			public void run() {

				try {
					sendMessage(Integer.valueOf(countdownStarter).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				countdownStarter--;

				if (countdownStarter < 0) {
					scheduler.shutdown();
				}
			}
		};
		scheduler.scheduleAtFixedRate(runnable, 30, 30, TimeUnit.SECONDS);
	}

	public String getResult(String userid) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();

		String sql = "select count(*) as 'count' from result where userid = '" + userid + "'  AND result = 'win'\n"
				+ "union ALL select count(*) from result where userid = '" + userid + "'  AND result = 'lose' \n"
				+ "union ALL select count(*) from result where userid = '" + userid + "'  AND result = 'draw';";
		ResultSet resultSet = st.executeQuery(sql);
		int playcount = 0;
		int[] count = new int[3];
		int index = 0;
		while (resultSet.next()) {
			count[index] = resultSet.getInt("count");
			playcount += count[index];
			index++;
		}
		double rate = (double) count[0] / (double) playcount;
		int resultrate = (int) Math.round(rate * 10000) / 100;
		st.close();
		con.close();
		return resultrate + "% win:" + count[0] + " lose:" + count[1] + " draw:" + count[2];
	}

	public void addResult(String result, String record) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "INSERT INTO record(record,userid1,userid2) values('" + record + "','" + name1 + "','" + name2
				+ "');";
		st.execute(sql);
		sql = "SELECT gameid FROM record ORDER BY gameid DESC LIMIT 1;";
		ResultSet resultSet = st.executeQuery(sql);
		int gameid = 0;
		while (resultSet.next()) {
			gameid = resultSet.getInt("gameid");
		}
		String[] ary = { "win", "lose", "draw" };
		String ret = result == ary[0] ? ary[1] : result == ary[1] ? ary[0] : ary[2];
		sql = "insert into result (userid,result,id,playdate) value('" + name1 + "','" + ret + "'," + gameid + ",DATE_ADD(current_timestamp, INTERVAL 9 HOUR));";
		st.execute(sql);
		sql = "insert into result (userid,result,id,playdate) value('" + name2 + "','" + result + "'," + gameid + ",DATE_ADD(current_timestamp, INTERVAL 9 HOUR));";
		st.execute(sql);
		st.close();
		con.close();
	}

	public void getKihu(Session session) throws Exception {
		this.record = new ArrayList<>();
		String searchname = null;
		if (session == user1) {
			searchname = name1;
		} else if (session == user2) {
			searchname = name2;
		}
		if (searchname == null) {
			return;
		}
		Map<Integer, String> record = new LinkedHashMap<>();
		String result;
		int gameid = 0;
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select gameid,record from record where userid1 = '" + searchname + "' OR userid2 = '" + searchname
				+ "';";
		ResultSet resultSet = st.executeQuery(sql);
		while (resultSet.next()) {
			record.put(resultSet.getInt("gameid"), resultSet.getString("record"));
		}
		sql = "select id,result,playdate from result where userid = '" + searchname + "';";
		resultSet = st.executeQuery(sql);
		while (resultSet.next()) {
			gameid = resultSet.getInt("id");
			String getrecord = record.get(gameid);
			result = resultSet.getString("result");
			Timestamp date = resultSet.getTimestamp("playdate");
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			dateFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
			String str = dateFormatter.format(date);
			
			if (getrecord != null) {
				this.record.add(getrecord);
				sendMessage("kihu," + str + "," + result);
			}
		}
		if(this.record.size() == 0) {
			sendMessage("kihu,failure");
		}
		st.close();
		con.close();
	}
	
	public void getKihuStone(int index) throws Exception {
		this.stonedata = new ArrayList<>();
		this.evadata = new ArrayList<>();
		stonedataindex = 0;
		String record = this.record.get(index);
		Othello game = new Othello();
		game.initialize();
		String stone = game.getStone();
		String mess = game.getAIEvaluation(!game.getColor());
		int[] coord = {9,9};
		stonedata.add(stone);
		evadata.add(mess);
		coorddata.add(coord);
		sendMessage("kihustone," + stone);
		for(int i = 0; i < record.length()+2;i+=2) {
			try {
				int x = Character.getNumericValue(record.charAt(i));
				int y = Character.getNumericValue(record.charAt(i+1));
				if(game.getPass()) {
					stonedata.add(game.getStone());
					evadata.add(game.getAIEvaluation(game.getColor()));
					coorddata.add(coord);
				}
				game.place(x,y);
				int[] addcoord = {x,y};
				stonedata.add(game.getStone());
				evadata.add(game.getAIEvaluation(game.getColor()));
				coorddata.add(addcoord);
			}catch (java.lang.StringIndexOutOfBoundsException e) {
				break;
			}
		}
		sendMessage("kihu,play");
	}
	
	public void playKihu(int cmd) throws Exception {
		String stone = null;
		String eva = null;
		int[] coord = null;
		int maxindex = stonedata.size()-1;
		switch(cmd) {
		case 0:
			if(stonedataindex != maxindex) {
				stonedataindex++;
			}
			break;
		case 1:
			if(stonedataindex != 0) {
				stonedataindex--;
			}
			break;
		case 2:
			stonedataindex = 0;
			break;
		case 3:
			stonedataindex = maxindex;
			break;
		}
		stone = stonedata.get(stonedataindex);
		eva = evadata.get(stonedataindex);
		coord = coorddata.get(stonedataindex);
		sendMessage("kihustone,"+stone);
		sendMessage(eva);
		sendMessage("coord," + coord[0] + "," + coord[1]);
	}

	public Connection getConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection(
					"jdbc:mysql://us-cdbr-east-05.cleardb.net/heroku_dc2e2b62c172679", "bfa96589068399", "f5a637d7");

			return con;

		} catch (ClassNotFoundException e) {
			System.out.println("ドライバを読み込めませんでした " + e);
		} catch (SQLException e) {
			System.out.println("データベース接続エラー" + e);
		}
		return null;
	}

}
