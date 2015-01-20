package usflix;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class MovieDatabase {
	
	private ArrayList<Movie> movies = new ArrayList<Movie>();
	
	public MovieDatabase(){
		movies = new ArrayList<Movie>();
	}
	
	public MovieDatabase(String filename){
		try{
			// create scanner for file
			Scanner scan = new Scanner(new File(filename));
			// make a movie object
		    Movie m;
		    //every three lines add to movie object
			while(scan.hasNext()){
				String title = scan.nextLine();
				int year = Integer.parseInt(scan.nextLine());
				String director = scan.nextLine();
				m = new Movie(title, year, director);
				addMovie(m);
			}
		}catch (FileNotFoundException e) {
			System.out.println("No such file!");
		}
	}
	
	public boolean addMovie(Movie m){
		if(movies.add(m)){
			return true;
		}else
			return false;
	}
	
	public ArrayList<Movie> searchByTitle(String [] keywords){
		// make a temp array list and loop through the movie and keywords
		
		ArrayList<Movie> temp1 = new ArrayList<Movie>();
		ArrayList<Movie> temp2 = new ArrayList<Movie>();
		
		temp1 = movies;

		// didn't change original list, created a clone and added from it
		for(int i = 0 ; i < keywords.length; i++){
			for(int j = 0; j < temp1.size(); j++){
				if(temp1.get(j).getTitle().toLowerCase().contains(keywords[i].toLowerCase())){
					temp2.add(temp1.get(j));
				}	
		}
	}
		return temp2;
	}
	
	public Movie getMovieByTitle(String title){
		for(int i = 0; i < movies.size(); i++){
			if(movies.get(i).title.equals(title)){
				return movies.get(i);
			}
		}
		return null;
		
	}
}