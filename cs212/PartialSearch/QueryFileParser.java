/**
 * Class: Search_Word
 * Purpose: The purpose of this class is to pass query lines to 
 * inverted index. The lines will be passes as search result arrays
 * and received in the same fashion. The map will add these search
 * result objects in and print them out to a file. 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class QueryFileParser {

	private final LinkedHashMap<String, ArrayList<SearchResults>> map;
	
	public QueryFileParser() { 
		map = new LinkedHashMap<String, ArrayList<SearchResults>>();
	}

	// TODO SO MUCH EXTRA SPACE
	
	/**
	 * Normalizes a word by converting it to lower case, removing all non-word
	 * characters using the {@code "\\W"} regular expression, removing all
	 * {@code "_"} underscore characters, and removing any unnecessary extra
	 * whitespace at the start of end of the word.
	 *
	 * @param word to normalize
	 * @return normalized version of the word
	 */
	public static String normalizeWord(String word) {
		if(word != null && !word.isEmpty()){
			word = word.replaceAll("[\\W_]", "").trim().toLowerCase();
		}
		return word;
	}
	
	// TODO Usually a space before a brace {. "if () {"
	
	
	/**
	 * Search the query file to find the query words and add
	 * them in to an array list. 
	 * @param filename
	 */

	public void search(String filename, InvertedIndex index){
		
		Path file = Paths.get(filename);
		
		try (BufferedReader reader = 
				Files.newBufferedReader(file, Charset.forName("UTF-8"))
			){
			String parseWord; 
			
			while((parseWord = reader.readLine())!= null){
				ArrayList<String> query_words = new ArrayList<String>();
				
		
				for(String word : parseWord.split("\\s")) { 
					normalizeWord(word);
					query_words.add(word);
					} // TODO Indentation
				map.put(parseWord, index.partialSearch(query_words));
				
			}
			
		} catch(IOException e) {
			System.out.println("The file " + filename + " could not be read.");
		}
	}
	/**
	 * Method: Print_Results TODO Do not need to include method name, etc. in javadoc
	 * Purpose: The purpose of this method is to iterate the map of
	 * search results and print them out according to project 2 requirements. 
	 * @param filename
	 */

	public void printResults(String filename) {

		Path path = Paths.get(filename);
		

		try(BufferedWriter writer = 
				Files.newBufferedWriter(path, Charset.forName("UTF-8"))			
				){
			for(String query : map.keySet()) {
				writer.write(query);
				writer.newLine();
				for(SearchResults sr : map.get(query)) {
					// TODO If you override toString(), can do writer.write(sr)
					writer.write('"' + sr.getPath() +'"' +", ");
					writer.write(sr.getFrequency() +", " + sr.getPosition());
					writer.newLine();
				}
				writer.newLine();
			}
			writer.newLine();
		} catch(IOException e) {
			System.out.println("The file " + filename + " could not be written out to.");
		}
	}
}

	