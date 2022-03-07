package websocket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import model.Othello;
import model.Room;

@ServerEndpoint("/othello")
public class WebSocket {
	private static List<Room> roomlist = new ArrayList<>();
	private static Map<Session,String> userlist = new LinkedHashMap<>();

	@OnOpen
	public void connect(Session session) throws Exception {
		Room room = new Room();
		room.setUser(session);
		roomlist.add(room);
	}

	@OnClose
	public void remove(Session session) {
		Room getroom = getRoom(session);
		getroom.removeSession(session);
		getroom.setAI(true);
		if (getroom.removeRoom()) {
			roomlist.remove(getroom);
		}
		userlist.remove(session);
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
			room.sendMessage("start");
			Thread.sleep(100);
			room.sendMessage("stone," + stone);
			room.timer();
			if (room.isAI()) {
				room.setAI(true);
				Thread.sleep(100);
				room.sendMessage("matching,"+ room.getName1()+","+ room.getName2());
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
			stone = game.othello();
			room.sendMessage("stone," + stone);
			if (!ret) {
				break;
			}
			if (room.isAI()) {
				Thread.sleep(300);
				game.othelloAI();
				stone = game.othello();
				room.sendMessage("stone," + stone);
			}
			if (!game.isGame()) {
				addResult(game.judge(), game.getRecord());
				Thread.sleep(100);
				room.sendMessage("end");
				if (room.isAI()) {
					Thread.sleep(100);
					room.sendMessage(getResult());
				}
			}
			break;
		case "online":
			boolean getRoom = setRoom(session);
			if (getRoom) {
				roomlist.remove(room);
			} else {
				room.setAI(false);
			}
			String userid = userlist.get(session);
			if(userid != null) {
				getRoom(session).setName(session, userid);
			}
			break;
		case "offline":
			room.setAI(true);
			break;
		case "login":
			ret = userLogin(str[1],str[2]);
			if(ret) {
				room.sendMessage("login,success");
				room.setName(session, str[1]);
				userlist.put(session,str[1]);
			}else {
				room.sendMessage("login,failure");
			}
			break;
		case "register":
			if(!userLogin(str[1],str[2])) {
				if(userRegister(str[1],str[2])) {
					room.sendMessage("register,success");
					break;
				}
			}
			room.sendMessage("register,failure");
			break;
		case "delete":
			if(userDelete(str[1],str[2])) {
				room.sendMessage("delete,success");
			}else {
				room.sendMessage("delete,failure");
			}
			break;
		}

	}

	public boolean setRoom(Session session) throws Exception {
		boolean setroom = false;
		for (Room r : roomlist) {
			if (r.isEmpty()) {
				r.setUser(session);
				setroom = true;
				Thread.sleep(100);
				r.sendMessage("matching,"+ r.getName1()+","+ r.getName2());
				break;
			}
		}
		return setroom;
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

		String sql = "select count(*) as \"count\" from result where result = 'win' union ALL select count(*) from result where result = 'lose' union ALL select count(*) from result where result = 'draw';";
		ResultSet resultSet = st.executeQuery(sql);
		int playcount = 0;
		int[] count = new int[3];
		int index = 0;
		while (resultSet.next()) {
			count[index] = resultSet.getInt("count");
			playcount += count[index];
			index++;
		}
		double rate = (double) count[0] /(double)  playcount;
		rate = (double)Math.round(rate * 10000) / 100;
		st.close();
		con.close();
		return "rate,勝率" + rate + "% win:" + count[0] + " lose:" + count[1] + " draw:" + count[2];
	}

	public void addResult(String result, String record) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		LocalDateTime datetime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String time = datetime.format(f);
		String sql = "INSERT INTO result(playdate,result,record) values('" + time + "','" + result + "','" + record
				+ "');";
		st.execute(sql);
		st.close();
		con.close();
	}
	
	public boolean userRegister(String id,String pass) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid='" +id+ "';";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		if(!ret) {
			sql = "INSERT INTO account (password,userid,rate) values ('" +pass+ "','" +id+ "',0.00);";
			st.execute(sql);
		}
		st.close();
		con.close();
		return !ret;
	}
	
	public boolean userDelete(String id,String pass) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "delete from account where userid = '" +id+ "' AND password ='"+pass+"';";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		st.close();
		con.close();
		return ret;
	}
	
	public boolean userLogin(String id,String pass) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid ='" +id+ "' AND password = '" +pass+ "'";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		st.close();
		con.close();
		return ret;
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