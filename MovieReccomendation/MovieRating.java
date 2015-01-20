package usflix;

public class MovieRating {

	private Movie movie;
	private float rating = 0;

	public MovieRating(Movie m, float r){
		movie = m;
		rating = r;
	}
	
	public Movie getMovie(){
		return this.movie;
	}
	
	public float getRating(){
		return this.rating;
	}
	
	public void setRating(float r){
		if(r >= 0.5 && r <= 5){
			rating = r;
		} 
	}
	public String toString(){
		String empty = "0";
		float averageRating = this.movie.getAverageRating();
		if(rating > 0){
			return "Rating is : " + rating;
		}else if(rating == 0){
			return "average: " + "(" + averageRating +")";
		}
		return empty;
	}
}