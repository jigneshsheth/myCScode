package usflix;

import java.util.HashMap;

public class UserDatabase {
	
	public static int minPwdLength = 6;
	private HashMap<String, User> userDB = new HashMap<String, User>();
	
	public UserDatabase(){
		userDB = new HashMap<String, User>();
	}

	public User createAccount(String f, String l, String u, String p){
		User newUser = new User(f, l, u, p);
		// new user creates a new account
		if(p.contains(u)){
			return null;
		} else if(p.length() < minPwdLength){
			return null;
		} else if(isAvailable(u) == false){
			return null;
		} else 
			userDB.put(u, newUser);
			return newUser;
	}
	
	public User login(String u, String p){
		// the hash map checks if the username and password are in the database 
		if(userDB.containsKey(u) && userDB.get(u).login(p)){
			return userDB.get(u);
		}
		return null;
	}

	
	public boolean isAvailable(String u){
		// checking if username is in the database already
		if(userDB.containsKey(u) == false){
			return true;
		}else
			return false;
	}
}