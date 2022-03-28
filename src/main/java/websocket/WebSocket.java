package websocket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

	public WebSocket() throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "delete from account where userid Like 'guest%';";
		st.executeUpdate(sql);
		sql = "delete from result where userid Like 'guest%';";
		st.executeUpdate(sql);
		sql = "update account set status = false where status = true;";
		st.executeUpdate(sql);
		st.close();
		con.close();
	}

	@OnOpen
	public void connect(Session session) throws Exception {
		Room room = new Room();
		soloroomlist.add(room);
		room.setUser(session);
		room.sendMessage("matching,AI");
		room.sendTurn();
		room.sendName();
		room.sendResult();
		userRegister(room.getName1(), "lalalasyun.com");
	}

	@OnClose
	public void remove(Session session) throws Exception {
		Room room = getMultiRoom(session);

		if (room != null) {
			room.removeSession(session);
			if (!room.getGame().isGame()) {
				room.sendMessage("matching,wait");
			}
			if (room.removeRoom()) {
				multiroomlist.remove(room);
			}
		}
		room = getSoloRoom(session);
		userDelete(room.getInitName(), "lalalasyun.com");
		soloroomlist.remove(room);
		boolean ret = userLogout(room.getName(session));
		if (ret) {
			userAddStatus(room.getName(session), false);
		}
	}

	@OnMessage
	public void broadcast(String message, Session session) throws Exception {
		String str[] = message.split(",");
		String stone;
		String mess;
		Room soloroom = getSoloRoom(session);
		Room multiroom = getMultiRoom(session);
		Room room = multiroom != null ? multiroom : soloroom;
		Othello game = room.getGame();
		switch (str[0]) {
		case "start":
			game.initialize();
			stone = game.getStone();
			room.sendTurn();
			room.sendMessage("start");
			room.sendMessage("stone," + stone);
			if (room.isAI() && room.isAiturn()) {
				int[] coord = game.othelloAIPut(room.isAiturn());
				stone = game.getStone();
				room.sendMessage("stone," + stone);
				if (coord != null) {
					room.sendMessage("coord," + coord[0] + "," + coord[1]);
				}
			}
			mess = game.getAIEvaluation(!game.getColor(),false);
			if (mess != null) {
				room.sendMessage(mess);
			}
			room.timer();
			break;
		case "reset":
			game.setGame(false);
			room.sendMessage("reset");
			break;
		case "coord":
			if (!room.isTurn(session) || !game.isGame()) {
				break;
			}
			int x = Integer.parseInt(str[1]);
			int y = Integer.parseInt(str[2]);

			boolean ret = game.place(x, y);

			if (!ret) {
				room.sendMessage("miss");
				break;
			}
			stone = game.getStone();
			room.sendMessage("stone," + stone);
			room.sendMessage("coord," + x + "," + y);
			if (room.isAI()) {
				Thread.sleep(300);
				int[] coord = game.othelloAIPut(room.isAiturn());
				stone = game.getStone();
				room.sendMessage("stone," + stone);
				if (coord != null) {
					room.sendMessage("coord," + coord[0] + "," + coord[1]);
				}
			}
			if (!game.isGame()) {
				room.addResult(game.judge(), game.getRecord());
				room.sendMessage("end");
				room.sendResult();
			}
			mess = game.getAIEvaluation(game.getColor(),false);
			if (mess != null) {
				room.sendMessage(mess);
			}
			break;
		case "ainavi":
			stone = game.getStone();
			room.sendMessage("stone," + stone);
			mess = game.getAIEvaluation(game.getColor(),false);
			if (mess != null) {
				room.sendMessage(mess);
			}
			break;
		case "changeturn":
			room.changeTurn();
			room.sendTurn();
			room.sendName();
			break;
		case "online":
			online(soloroom, session);
			break;
		case "offline":
			multiroom.removeSession(session);
			if (!room.getGame().isGame()) {
				multiroom.sendMessage("matching,wait");
			}
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
				soloroom.setName(session, str[1]);
				room.setName(session, str[1]);
				soloroom.sendMessage("login,0,success");
				room.sendName();
				userAddStatus(str[1], true);
			} else {
				soloroom.sendMessage("login,0,failure");
			}

			break;
		case "logout":
			boolean login = userLogout(room.getName(session));
			if (login) {
				String name = room.getName(session);
				String initname = soloroom.getInitName();
				soloroom.removeName(session, initname);
				room.removeName(session, initname);
				soloroom.sendMessage("login,1,success");
				userAddStatus(name, false);
			} else {
				soloroom.sendMessage("login,1,failure");
			}
			room.sendName();
			break;
		case "register":
			if (!userLogin(str[1], str[2])) {
				if (userRegister(str[1], str[2])) {
					soloroom.sendMessage("login,2,success");
					break;
				}
			}
			room.sendName();
			soloroom.sendMessage("login,2,failure");
			break;
		case "delete":
			soloroom.setName(session, null);
			room.setName(session, null);
			if (userDelete(str[1], str[2])) {
				soloroom.sendMessage("login,3,success");
			} else {
				soloroom.sendMessage("login,3,failure");
			}
			room.sendName();
			break;
		case "kihu":
			switch (str[1]) {
			case "get":
				soloroom.getKihu(session);
				break;
			case "index":
				int index = Integer.parseInt(str[2]);
				soloroom.getKihuStone(index);
				break;
			case "next":
				soloroom.playKihu(0);
				break;
			case "back":
				soloroom.playKihu(1);
				break;
			case "start":
				soloroom.playKihu(2);
				break;
			case "end":
				soloroom.playKihu(3);
				break;
			}
			break;
		}
	}

	public void online(Room soloroom, Session session) throws Exception {
		Room multiroom = getMultiRoom(soloroom.getName(session));
		boolean matching = multiroom == null;
		if (matching) {
			multiroom = new Room(soloroom.getName(session));
			multiroomlist.add(multiroom);
		}
		multiroom.setUser(session);
		if (!matching) {
			multiroom.setName(session, soloroom.getName(session));
			multiroom.sendTurn();
			multiroom.sendMessage("matching,player");
			multiroom.sendName();
			Othello game = multiroom.getGame();
			if (game.isGame()) {
				multiroom.sendMessage("start");
				multiroom.sendMessage("stone," + game.getStone());
			}
			multiroom.sendResult();
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

		return getEmptyMultiRoom();
	}

	public Room getMultiRoom(Session session) {
		for (Room r : multiroomlist) {
			if (r.searchSession(session)) {
				return r;
			}
		}
		return null;
	}

	public Room getEmptyMultiRoom() {
		for (Room r : multiroomlist) {
			if (r.isEmpty() && !r.getGame().isGame()) {
				return r;
			}
		}
		return null;
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
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid ='" + id + "' AND password = '" + pass
				+ "' AND status = false";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		st.close();
		con.close();
		return ret;
	}

	public boolean userLogout(String id) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		String sql = "select * from account where userid ='" + id + "' AND status = true";
		ResultSet resultSet = st.executeQuery(sql);
		boolean ret = false;
		while (resultSet.next()) {
			ret = true;
		}
		st.close();
		con.close();
		return ret;
	}

	public void userAddStatus(String id, boolean ret) throws SQLException {
		Connection con = getConnection();
		Statement st = con.createStatement();
		int param = ret ? 1 : 0;
		String sql = "update account set status = " + param + " where userid='" + id + "';";
		st.executeUpdate(sql);
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