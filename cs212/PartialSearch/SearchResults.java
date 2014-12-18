/**
 * Class: SearchResults
 * Purpose: Create a search result object which stores the
 * path, frequency, and position of each search result.
 * This class also uses a compare to method to check which search result
 * object should be placed ahead than another based off off the objects
 * frequency count. 
 * @author p_hundal
 *
 */

public class SearchResults implements Comparable<SearchResults> {
	
	private final String path;
	private int frequency;
	private int position;
	
	/**
	 * Constructor : Constructs a SearchResults object. 
	 * @param route - TODO
	 * @param freq - TODO
	 * @param pos - earliest position a query word appears in the path
	 */
	public SearchResults(String route, int freq, int pos) {
		path = route;
		frequency = freq;
		position = pos;
		
	}

	/**
	 * Comparator method that compares search result objects
	 * based on their frequency, then position, and then path. 
	 */
	@Override
	public int compareTo(SearchResults other) {
		
		if(this.frequency != other.frequency){
			return Integer.compare(other.frequency, this.frequency);
		}
		
		if(this.position != other.position){
			return Integer.compare(this.position, other.position);
		}
		
		return String.CASE_INSENSITIVE_ORDER.compare(this.path, other.path);
	}
		
	/**
	 * Returns the frequency of this search result.
	 * @return
	 */
	public int getFrequency() {
		return this.frequency;
	}
	
	/**
	 * Returns the position of this search result. 
	 * @return
	 */
	public int getPosition() {
		return this.position;
	}
	
	/**
	 * Returns the path of this search result. 
	 * @return
	 */
	public String getPath() {
		return this.path;
	}
	
	/** 
	 * Updates the position of the current position is less
	 * than the previous one. 
	 * @param newpos
	 */
	public void setPosition(int newpos) { 
		if(newpos < position){
			position = newpos;
		}
	}
	
	/**
	 * Updates frequency by adding the count each time. 
	 * @param freq
	 */
	public void addFrequency(int freq) { 
		frequency += freq;
	}
}