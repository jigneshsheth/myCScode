import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The purpose of this class is to parse through the files our directory
 * traversal class passes to it. We parse each file by removing all the white
 * space, special characters, and make it case-sensitive. The file reader keeps
 * track of all positions and files passed from our directory traversal and
 * sends it to our inverted index.
 * 
 * @author Paul Hundal
 * 
 */
public class InvertedIndexBuilder {


	/**
	 * This method takes in file input from Directory Traverse class and tries
	 * to read it in through a buffer. The buffer reads in the file line by
	 * line, cleans up the words by trimming off white space and taking away the
	 * special characters. After that, the buffer passes the word to the
	 * inverted index data structure.
	 * 
	 * @param fileToParse
	 */


	public static void parseFiles(String fileToParse, InvertedIndex index) {

		/* Storage unit for the words we parse */
		String[] words;

		/* Keep track of each position as we read it in */
		int position = 1;

		/*
		 * Buffered reader will parse through our file passed from directory
		 * traversal
		 */
		try (
			// TODO Eclipse doesn't know yet how to format try-with-resources, make it:
			BufferedReader fileReader = Files.newBufferedReader(Paths.get(fileToParse), Charset.forName("UTF-8"));
		){

			while (fileReader.ready()) {
				words = fileReader.readLine().split(" ");
				// Normalize each word as it is passed to the inverted index.
				for (String word : words) {
					word = word.replaceAll("\\W", "").replaceAll("_", "").trim().toLowerCase();
					if (word != null && !word.isEmpty()) {
						index.addWord(word, fileToParse, position);
						position++;
					}
				}
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("The file" + fileToParse + "you tried to read does not exist! ");
		} 
		catch (IOException e) {
			System.out.println("Cannot read the given file.");

		}
	}
}