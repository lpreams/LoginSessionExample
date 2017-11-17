package clinic.json;

public class LoginEvent {
	private String token;
	private JsonUser user;
	public LoginEvent() {}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public JsonUser getUser() {
		return user;
	}
	public void setUser(JsonUser user) {
		this.user = user;
	}

}
