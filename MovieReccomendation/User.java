package usflix;
import java.util.ArrayList;
public class User {

	private String firstName;
	private String lastName;
	private String password;
	private String username;
	private float minRating = (float) 0.5;
	private int maxRating = 5;
	
	private ArrayList<MovieRating> seenMovies = new ArrayList<MovieRating>();

	
	public User(String f, String l, String u, String p){
		firstName = f;
		lastName = l;
		password = p;
		username = u;
	}
	
	public void addRating(Movie m, float r){
		// need a new movie rating object
		MovieRating seen = new MovieRating(m, r);
		if(seenMovies.contains(seen)){
			if(r >= minRating && r <= maxRating){
				// if its between the min and max then it updates the movie rating and movie object
				seen.setRating(r);
				m.removeRating(r);
			} else 
				System.out.println("Please enter a valid rating: ");
	}
		if(r >= minRating && r <= maxRating){
			m.addRating(r);
			seenMovies.add(seen);
		}
}
	public String getUsername(){
		return username;
	}
	public String getFirstName(){
		return firstName;
	}
	public String getLastName(){
		return lastName;
	}
	
	public String getRating(Movie m){	
		for(MovieRating movieRating:seenMovies){
			if(movieRating.getMovie() == m){
				// condition is if the movie rated is in movie rating then return its rating
				return Float.toString(movieRating.getRating());
			} else
				return Float.toString(m.getAverageRating());
		}
		return null;
	}
	
	public ArrayList<Movie> getSeenMovies(){
		// an array list created to add seenMovies from
		ArrayList<Movie> movObj = new ArrayList<Movie>();
		for(int i = 0; i < seenMovies.size(); i++){
			movObj.add(seenMovies.get(i).getMovie());
		}
		return movObj;
	}
	
	// checks if string p is equal to the password entered
	public boolean login(String p){
		if(p.equals(password)){
			return true;
		}
		return false;
	}
	
}