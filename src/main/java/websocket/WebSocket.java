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
import java.util.Map.Entry;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import model.Othello;
import model.Room;

@ServerEndpoint("/othello")
public class WebSocket {
	private static List<Room> multiroomlist = new ArrayList<>();
	private static List<Room> soloroomlist = new ArrayList<>();
	private static Map<Session, String> userlist = new LinkedHashMap<>();
	
	public WebSocket() throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "delete from account where userid Like 'guest%';";
		st.executeUpdate(sql);
		sql = "delete from result where userid Like 'guest%';";
		st.executeUpdate(sql);
		st.close();
		con.close();
	}
	
	@OnOpen
	public void connect(Session session) throws Exception {
		Room room = new Room();
		soloroomlist.add(room);
		room.setUser(session);
		room.sendTurn();
		room.sendMessage("matching,AI");
		room.sendName();
		room.sendResult();
		userRegister(room.getName1(),"lalalasyun.com");
	}

	@OnClose
	public void remove(Session session) throws SQLException {
		Room room = getMultiRoom(session);
		if (room != null) {
			room.removeSession(session);
		}
		if (room.removeRoom()) {
			multiroomlist.remove(room);
		}
		room = getSoloRoom(session);
		if(userlist.remove(session) == null) {
			userDelete(room.getName1(),"lalalasyun.com");
			userDelete(room.getName2(),"lalalasyun.com");
		}
		soloroomlist.remove(room);
	}

	@OnMessage
	public void broadcast(String message, Session session) throws Exception {
		String str[] = message.split(",");
		String stone;
		Room soloroom = getSoloRoom(session);
		Room multiroom = getMultiRoom(session);
		Room room =  multiroom != null ? multiroom:soloroom;
		Othello game = room.getGame();
		switch (str[0]) {
		case "start":
			game.initialize();
			stone = game.othello();
			room.sendMessage("start");
			room.sendMessage("stone," + stone);
			if (room.isAI() && room.isAiturn()) {
				Thread.sleep(300);
				game.othelloAI(room.isAiturn());
				stone = game.othello();
				room.sendMessage("stone," + stone);
			}
			room.timer();
			break;
		case "reset":
			game.initialize();
			room.sendMessage("reset");
			break;
		case "coord":
			if (!room.isTurn(session) || !game.isGame()) {
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
				game.othelloAI(room.isAiturn());
				stone = game.othello();
				room.sendMessage("stone," + stone);
			}
			if (!game.isGame()) {
				addResult(game.judge(), game.getRecord(),room);
				room.sendMessage("end");
				room.sendResult();
			}
			break;
		case "changeturn":
			room.changeTurn();
			room.sendTurn();
			room.sendName();
			break;
		case "online":
			multiroom = getMultiRoom();
			boolean matching = multiroom == null;
			if (matching) {
				multiroom = new Room(soloroom.getName(session));
				multiroomlist.add(multiroom);
			} 
			multiroom.setUser(session);
			multiroom.setName(session, soloroom.getName(session));
			multiroom.sendTurn();
			if (!matching) {
				multiroom.sendMessage("matching,player");
				multiroom.sendName();
				multiroom.sendResult();
			}
			break;
		case "offline":
			multiroom.removeSession(session);
			multiroom.sendMessage("matching,wait");
			if (multiroom.removeRoom()) {
				multiroomlist.remove(multiroom);
			}
			soloroom.sendTurn();
			soloroom.sendMessage("matching,AI");
			soloroom.sendName();
			soloroom.sendResult();
			break;
		case "login":
			ret = userLogin(str[1], str[2]);
			if (ret) {
				Room findRoom = getMultiRoom(str[1]);
				if (findRoom != null) {
					room = findRoom;
					room.setUser(session);
				}
				soloroom.setName(session, str[1]);
				room.setName(session, str[1]);
				room.sendMessage("login,0,success");
				userlist.put(session, str[1]);
			} else {
				room.sendMessage("login,0,failure");
			}
			room.sendName();
			break;
		case "logout":
			String removeret = userlist.remove(session);
			if (removeret != null) {
				room.removeName(session);
				room.sendMessage("login,1,success");
			} else {
				room.sendMessage("login,1,failure");
			}
			room.sendName();
			break;
		case "register":
			if (!userLogin(str[1], str[2])) {
				if (userRegister(str[1], str[2])) {
					room.sendMessage("login,2,success");
					break;
				}
			}
			room.sendName();
			room.sendMessage("login,2,failure");
			break;
		case "delete":
			soloroom.setName(session, null);
			room.setName(session, null);
			if (userDelete(str[1], str[2])) {
				room.sendMessage("login,3,success");
			} else {
				room.sendMessage("login,3,failure");
			}
			room.sendName();
			userlist.remove(session);
			break;
		}

	}

	public Room getSoloRoom(Session session) {
		for (Room r : soloroomlist) {
			if (r.searchSession(session)) {
				return r;
			}
		}
		return null;
	}

	public Room getMultiRoom(String userid) {
		for (Room r : multiroomlist) {
			if (r.searchUser(userid)) {
				return r;
			}
		}
		return null;
	}

	public Room getMultiRoom(Session session) {
		for (Room r : multiroomlist) {
			if (r.searchSession(session)) {
				return r;
			}
		}
		return null;
	}

	public Room getMultiRoom() {
		for (Room r : multiroomlist) {
			if (r.isEmpty()) {
				return r;
			}
		}
		return null;
	}


	public void addResult(String result, String record ,Room room) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		LocalDateTime datetime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String time = datetime.format(f);
		String sql = "INSERT INTO record(playdate,record,userid1,userid2) values('" + time + "','" + record
				+ "','"+room.getName1()+"','"+room.getName2()+"');";
		st.execute(sql);
		sql = "SELECT id FROM record ORDER BY id DESC LIMIT 1;";
		ResultSet resultSet = st.executeQuery(sql);
		int gameid = 0;
		while (resultSet.next()) {
			gameid = resultSet.getInt("id");
		}
		String[] ary = {"win" , "lose" , "draw"};
		String ret = result == ary[0] ? ary[1]:result == ary[1]?ary[0]:ary[2];
		sql = "insert into result (userid,result,id) value('"+room.getName1()+"','"+ret+"',"+gameid+");";
		st.execute(sql);
		sql = "insert into result (userid,result,id) value('"+room.getName2()+"','"+result+"',"+gameid+");";
		st.execute(sql);
		st.close();
		con.close();
	}

	public boolean userRegister(String id, String pass) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid='" + id + "';";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		if (!ret) {
			sql = "INSERT INTO account (password,userid) values ('" + pass + "','" + id + "');";
			st.execute(sql);
		}
		st.close();
		con.close();
		return !ret;
	}

	public boolean userDelete(String id, String pass) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "delete from account where userid = '" + id + "' AND password ='" + pass + "';";
		int ret = st.executeUpdate(sql);
		st.close();
		con.close();
		return ret != 0 ? true : false;
	}

	public boolean userLogin(String id, String pass) throws SQLException {
		for(Entry<Session, String> user:userlist.entrySet()) {
			if(user.getValue().equals(id)) {
				return false;
			}
		}
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid ='" + id + "' AND password = '" + pass + "'";
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