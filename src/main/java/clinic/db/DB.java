package clinic.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;

public class DB {
	
	public static void main(String[] args) throws Exception {
		try {
			DB.createUser("lpreams", "testpass", 10, "Lee", "Reams");
			DB.createUser("normie", "testpass", 1, "Normal", "User");
		} finally {
		session.close();
		sessionFactory.close();
		System.exit(0);
		}
	}
	
	static final SessionFactory sessionFactory;
	static final Session session;
	static Object dbLock = new Object();
	static {
		synchronized (dbLock) {
			SessionFactory tempSessionFactory = null;
			Session tempSession = null;
			
			if (tempSessionFactory == null) {
				Configuration configuration = new Configuration().configure();
				configuration.addAnnotatedClass(DBUser.class);

				StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
						.applySettings(configuration.getProperties());
				tempSessionFactory = configuration.buildSessionFactory(builder.build());
			}
			if (tempSession == null) tempSession = tempSessionFactory.openSession();
			
			sessionFactory = tempSessionFactory;
			session = tempSession;
		}
	}
	
	public static class DBException extends Exception {
		/***/
		private static final long serialVersionUID = 4406905803438820527L;
		public DBException(String message) {
			super(message);
		}
	}
	
	public static void createUser(String username, String plainPassword, int level, String firstName, String lastName) throws DBException {
		synchronized (dbLock) {
			if (session.get(DBUser.class, username) != null) throw new DBException("Username " + username + " is already in use");
			DBUser user = new DBUser();
			user.setUsername(username);
			user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
			user.setLevel(level);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			session.beginTransaction();
			System.out.println("Created user " + session.save(user));
			session.getTransaction().commit();
		}
	}
	
	public static DBUser checkPassword(String username, String plainPassword) throws DBException {
		synchronized (dbLock) {
			DBUser user = session.get(DBUser.class, username);
			if (user == null) throw new DBException("Username " + username + " does not exist");
			if (BCrypt.checkpw(plainPassword, user.getPassword())) return user;
			else throw new DBException("Incorrect password");
		}
	}
	
	public static DBUser getUserByUsername(String username) throws DBException {
		synchronized (dbLock) {	
			DBUser user = session.get(DBUser.class, username);
			if (user == null) throw new DBException("Username " + username + " does not exist");
			return user;
		}
	}
	
	public static DBUser changeNames(String username, String newFirstName, String newLastName) throws DBException {
		synchronized (dbLock) {	
			DBUser user = session.get(DBUser.class, username);
			if (user == null) throw new DBException("Username " + username + " does not exist");
			user.setFirstName(newFirstName);
			user.setLastName(newLastName);
			session.beginTransaction();
			session.merge(user);
			session.getTransaction().commit();
			return user;
		}
	}
}
