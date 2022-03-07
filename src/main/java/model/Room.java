package model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

public class Room {
	private Session user1 = null;
	private Session user2 = null;
	
	private String name1 = null;
	private String name2 = "AI";
	
	private Othello game = new Othello();

	private boolean AI = true;
	
	
	
	public Session getUser1() {
		return user1;
	}

	public Session getUser2() {
		return user2;
	}
	
	public String getName1() {
		return name1;
	}

	public String getName2() {
		return name2;
	}

	public boolean isAI() {
		return AI;
	}

	public boolean isTurn(Session session) { 
		if(user1 == session) {
			return getGame().getColor() ? true:false;
		}else {
			return getGame().getColor() ? false:true;
		}
		
	}
	
	public void setName(Session session,String str) {
		if(session == user1) {
			name1 = str;
		}else {
			name2 = str;
		}
	}

	public void setUser(Session session) {
		if(user1 == null) {
			user1 = session;
			name1 = "Anonymous1";
			user1.getAsyncRemote().sendText("turn,black");
		}else if(user2 == null){
			user2 = session;
			name2 = "Anonymous2";
			user2.getAsyncRemote().sendText("turn,white");
		}
	}
	
	
	public void setAI(boolean ret) {
		AI = ret;
	}

	public void removeSession(Session session) {
		if(user1 == session) {
			user1 = null;
			name1 = null;
		}else {
			user2 = null;
			name2 = null;
		}
	}
	
	public boolean removeRoom() {
		return user1 == null && user2 == null;
	}

	public boolean isEmpty() {
		return !AI && (user1 == null || user2 == null);
	}
	
	public boolean searchSession(Session session) {
		return user1 == session || user2 == session;
	}
	
	public void sendMessage(String mess) {
		if(user1 != null) {
			user1.getAsyncRemote().sendText(mess);
		}
		if(user2 != null) {
			user2.getAsyncRemote().sendText(mess);
		}
	}

	public Othello getGame() {
		return game;
	}
	
	public void timer() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
        final Runnable runnable = new Runnable() {
        	int countdownStarter = 20;
            public void run() {

            	sendMessage(Integer.valueOf(countdownStarter).toString());
                countdownStarter--;

                if (countdownStarter < 0) {
                    scheduler.shutdown();
                }
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 30, 30, TimeUnit.SECONDS);
	}
	
}
