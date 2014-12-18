import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
	 * The input is the filename or directory name we want to traverse. The
	 * traverser will check to see if the file is a file type or directory type
	 * and recursively parse all files that end in .txt to the file parser.
	 * 
	 * @param filename
	 */
	
	public static void traverseDirectory(String filename, InvertedIndex index) {

		/* This path object will generalize the file input from args */
		Path files = Paths.get(filename);
		
		/*
		 * Check both conditions, either the input is a file, which we send to
		 * our file parser or the file is a directory and we store its contents
		 * in a file list and recursively process.
		 */
		
		if (files.toString().toLowerCase().endsWith(".txt")) {
			String filePath = files.toAbsolutePath().toString();
			try {
				InvertedIndexBuilder.parseFiles(filePath,index); 

			} catch (Exception e) {
				System.out.println("Could not find this path.");
			}

		} else if (Files.isDirectory(files)) {
			
			/* Efficiently iterate through files and sub-directories. */
			try(DirectoryStream<Path> list = Files.newDirectoryStream(files)) {
				for(Path file : list){
					traverseDirectory(file.toAbsolutePath().normalize().toString(), index);
				}

			} catch (IOException e) {
				System.out.println("Could not traverse the path " + filename );
			}
		}
	}


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
			// TODO Formatting
			BufferedReader fileReader = Files.newBufferedReader(Paths.get(fileToParse), 
					Charset.forName("UTF-8"));	
		) {

			while (fileReader.ready()) {
				words = fileReader.readLine().split("\\s");
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
			System.out.println("The file " + fileToParse + " you tried to read does not exist! ");
		} 
		catch (IOException e) {
			System.out.println("Cannot read the given file.");

		}
	}
}