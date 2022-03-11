package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		if (session == user1 &&  str != null) {
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

	public void removeName(Session session,String initname) {
		if(user1 == session) {
			name1 = initname;
		}else {
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
		if(name1.startsWith("guest")) {
			sendname1 = changeturn?"guest2":"guest1";
		}
		if(name2.startsWith("guest")) {
			sendname2 = changeturn?"guest1":"guest2";
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

		String sql = "select count(*) as 'count' from result where userid = '" +userid+"'  AND result = 'win'\n"
				+ "union ALL select count(*) from result where userid = '" +userid+"'  AND result = 'lose' \n"
				+ "union ALL select count(*) from result where userid = '" +userid+"'  AND result = 'draw';";
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
		rate = (double) Math.round(rate * 10000) / 100;
		st.close();
		con.close();
		return "勝率" + rate + "% win:" + count[0] + " lose:" + count[1] + " draw:" + count[2];
	}

	public void addResult(String result, String record) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "INSERT INTO record(record,userid1,userid2) values('" + record
				+ "','"+name1+"','"+name2+"');";
		st.execute(sql);
		sql = "SELECT gameid FROM record ORDER BY gameid DESC LIMIT 1;";
		ResultSet resultSet = st.executeQuery(sql);
		int gameid = 0;
		while (resultSet.next()) {
			gameid = resultSet.getInt("gameid");
		}
		String[] ary = {"win" , "lose" , "draw"};
		String ret = result == ary[0] ? ary[1]:result == ary[1]?ary[0]:ary[2];
		sql = "insert into result (userid,result,id) value('"+name1+"','"+ret+"',"+gameid+");";
		st.execute(sql);
		sql = "insert into result (userid,result,id) value('"+name2+"','"+result+"',"+gameid+");";
		st.execute(sql);
		st.close();
		con.close();
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
