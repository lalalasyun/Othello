package sample.websocket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/sample")
public class SampleWebSocket {
	private static List<Room> roomlist = new ArrayList<>();

	@OnOpen
	public void connect(Session session) throws Exception {
		boolean setroom = false;
		for (Room r : roomlist) {
			if (r.isEmpty()) {
				r.setUser(session);
				setroom = true;
				Thread.sleep(100);
				r.sendMessage("matching");
				break;
			}
		}
		if (!setroom) {
			Room room = new Room();
			room.setUser(session);
			roomlist.add(room);
		}

	}

	@OnClose
	public void remove(Session session) {
		Room getroom = getRoom(session);
		getroom.removeSession(session);
		getroom.setAI(false);
		if (getroom.removeRoom()) {
			roomlist.remove(getroom);
		}
	}

	@OnMessage
	public void broadcast(String message, Session session) throws Exception {
		String str[] = message.split(",");
		String stone;
		Room room = getRoom(session);
		Othello game = room.getGame();
		switch (str[0]) {
		case "start":
			game.initialize();
			stone = game.othello();
			room.sendMessage("stone," + stone);
			room.timer();
			if (room.isEmpty()) {
				room.setAI(true);
				Thread.sleep(100);
				room.sendMessage(getResult());
			}
			break;
		case "coord":
			if (!room.isTurn(session)) {
				break;
			}
			int x = Integer.parseInt(str[1]);
			int y = Integer.parseInt(str[2]);

			boolean ret = game.place(x, y);
			if (!ret) {
				break;
			}
			stone = game.othello();
			room.sendMessage("stone," + stone);
			if (room.isAI()) {
				Thread.sleep(300);
				game.othelloAI();
				stone = game.othello();
				room.sendMessage("stone," + stone);
			}
			if (!game.isGame()) {
				addResult(game.judge(),game.getRecord());
				Thread.sleep(100);
				room.sendMessage("end");
				if(room.isAI()) {
					Thread.sleep(100);
					room.sendMessage(getResult());
				}
			}
			break;
		}

	}

	public Room getRoom(Session session) {
		for (Room r : roomlist) {
			if (r.searchSession(session)) {
				return r;
			}
		}
		return null;
	}
	
	public String getResult() throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		
		String sql ="select count(*) as \"count\" from result where result = 'win' union ALL select count(*) from result where result = 'lose' union ALL select count(*) from result where result = 'draw';";
		ResultSet resultSet = st.executeQuery(sql);
		int playcount = 0;
		int[] count = new int[3];
		int index = 0;
		while (resultSet.next()) {
		    count[index] = resultSet.getInt("count");
		    playcount += count[index];
		    index++;
		}
		double rate = (double)count[0] / playcount;
		rate = ((double)Math.round(rate * 100));
		st.close();
	    con.close();
	    return "rate,AI勝率"  + rate + "% win:" + count[0] + " lose:" + count[1] + " draw:" + count[2];
	}

	public void addResult(String result,String record) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		LocalDateTime datetime = LocalDateTime.now();
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String time = datetime.format(f);
		String sql = "INSERT INTO result(playdate,result,record) values('" + time + "','" + result + "','" +  record  + "');";
		System.out.println(sql);
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