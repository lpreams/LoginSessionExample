package clinic.json;

import clinic.db.DBUser;

public class JsonUser {

	private String username;
		
	private int level; // 1=user, 10=admin
	
	private String firstName;
	
	private String lastName;
	
	public JsonUser() {}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public JsonUser(DBUser dbUser) {
		this.username = dbUser.getUsername();
		this.firstName = dbUser.getFirstName();
		this.lastName = dbUser.getLastName();
		this.level = dbUser.getLevel();
	}
}
