package sample.websocket;

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
	public void connect(Session session) {
		boolean setroom = false;
		for (Room r : roomlist) {
			if (r.isEmpty()) {
				r.setUser(session);
				setroom = true;
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
		if(getroom.removeRoom()) {
			roomlist.remove(getroom);
		}
	}

	@OnMessage
	public void broadcast(String message, Session session) throws Exception{
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
			if(room.isEmpty()) {
				room.setAI(true);
			}
			break;
		case "coord":
			if(!room.isTurn(session)) {
				break;
			}
			int x = Integer.parseInt(str[1]);
			int y = Integer.parseInt(str[2]);
			
			boolean ret = game.place(x, y);
			if(!ret) {
				break;
			}
			stone = game.othello();
			room.sendMessage("stone," + stone);
			if(room.isAI()) {
				Thread.sleep(500);
				game.othelloAI();
				stone = game.othello();
				room.sendMessage("stone," + stone);
			}
			if(!game.isGame()) {
				Thread.sleep(100);
				room.sendMessage("end");
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
	
	
	

}