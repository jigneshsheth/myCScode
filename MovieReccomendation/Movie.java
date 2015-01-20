package usflix;
import java.util.ArrayList;
public class Movie {
	
	protected final String director;
	private ArrayList<Float> ratings = new ArrayList<Float>();
	protected final String title;
	protected final int year;
	
	public Movie(String t, int y, String d){
		this.title = t;
		this.year = y;
		this.director = d;
		ratings = new ArrayList<Float>();
	}
	
	public void addRating(float r){
			ratings.add(r);
	} 
	
	public float getAverageRating(){
		float sum = 0;
		float average = 0;
		if(ratings.isEmpty()){
			ratings.add((float) 0);
		} else if(!ratings.isEmpty()){
		for(int i = 0; i< ratings.size(); i++){
				sum += ratings.get(i);
				average = sum / ratings.size();
		}
		return average;
	}
		return average;
}
	public String getTitle(){
		return title;
	}
	public void removeRating(float r){
		ratings.remove(r);
	}
	
	public String toString(){
		return "Movie " + title + "\n" + "Director: " + director + "\n" + "Year: " + year;
	}
}