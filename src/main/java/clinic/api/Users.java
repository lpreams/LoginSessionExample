package clinic.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import clinic.db.DB;
import clinic.db.DB.DBException;
import clinic.db.DBUser;
import clinic.json.JsonUser;
import clinic.json.LoginEvent;

@Path("user")
public class Users {
	
	private static HashMap<String,String> sessions = new HashMap<>();
	
	@GET
	@Path("login")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
		System.out.printf("Login request %s <%s>%n", username, password);
		try {
			DBUser user = DB.checkPassword(username, password);
			String token = genToken(sessions, username);
			LoginEvent login = new LoginEvent();
			login.setUser(new JsonUser(user));
			login.setToken(token);
			System.out.println("Added login token " + token + " - " + username);
			return Response.ok(new Gson().toJson(login)).status(200).build();
		} catch (DBException e) {
			return Response.ok("Username " + username + " not found, or incorrect password").status(401).build();
		}
	}
	
	@GET
	@Path("logout")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response logout(@QueryParam("token") String token) {
		System.out.printf("Logout request %s%n", token);
		if (sessions.containsKey(token)) {
			sessions.remove(token);
			return Response.ok("Logout successful").status(200).build(); 
		} else return Response.ok("Login session not found").status(401).build();
	}
	
	@GET
	@Path("changename")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response changename(@QueryParam("token") String token, @QueryParam("firstname") String firstName, @QueryParam("lastname") String lastName) {
		System.out.printf("Change name request %s%n", token);
		String username = sessions.get(token);
		if (username != null) {
			System.out.printf("Change name request %s <%s %s>%n", username, firstName, lastName);
			try {
				DB.changeNames(username, firstName, lastName);
			} catch (DBException e) {
				System.out.println("Username " + username + " not in db");
				return Response.ok("Login session not found").status(401).build();
			}
			
			sessions.remove(token);
			return Response.ok("Logout successful").status(200).build(); 
		} else {
			System.out.println("Login session not found");
			return Response.ok("Login session not found").status(401).build();
		}
	}
	
	@GET
	@Path("createuser")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response createuser(@QueryParam("token") String token, @QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("firstname") String firstName, @QueryParam("lastname") String lastName) {
		String userUsername = sessions.get(token);
		if (userUsername == null) return Response.ok("You must be logged in to do that").status(401).build();
		DBUser user;
		try {
			user = DB.getUserByUsername(userUsername);
		} catch (DBException e1) {
			return Response.ok("User not found (this should never happen)").status(401).build();
		}
		
		if (user.getLevel() < 10) return Response.ok("You must be an admin to do that").status(401).build();
		
		try {
			DB.createUser(username, password, 1, firstName, lastName);
		} catch (DBException e) {
			return Response.ok("Username " + username + " already exists").status(401).build();
		}
		
		return Response.ok("Created new user " + username).status(200).build();
	}
	
	private static String genToken(HashMap<String,String> map, String value) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<32; ++i) sb.append(charList.get(r.nextInt(charList.size())));
		String token = sb.toString();
		if (map.containsKey(token)) return genToken(map, value);
		else {
			map.put(token, value);
			return token;
		}
	}
	private static Random r = new Random();
	private static ArrayList<Character> charList = new ArrayList<>(36);
	static {
		for (char c='0'; c<='9'; ++c) charList.add(c);
		for (char c='a'; c<='z'; ++c) charList.add(c);
		for (char c='A'; c<='Z'; ++c) charList.add(c);
	}
}
