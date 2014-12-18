import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Purpose: This class handles the traversal of directories and files. Each
 * input is check for two conditions, either it is a file or a directory. A file
 * must end with the suffix ".txt", and a directory must be traversed until a
 * list of ".txt" files is ready to traverse. This class processes this
 * information recursively.
 * 
 * @author Paul Hundal
 * 
 */
public class DirectoryTraverser {

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
		
		// TODO: Might want to check if directory first, and then check if txt file.
		
		if (files.toString().toLowerCase().endsWith(".txt")){
			String filePath = files.toAbsolutePath().toString();
			try {
				InvertedIndexBuilder.parseFiles(filePath,index); 

			} catch (Exception e) {
				System.out.println("Could not find this path.");
			}

		} else if (Files.isDirectory(files)) {
			
			// Efficiently iterate through files and sub-directories. 
			try(DirectoryStream<Path> list = Files.newDirectoryStream(files)) {
				for(Path file : list){
					traverseDirectory(file.toAbsolutePath().normalize().toString(), index);
				}

			} catch (IOException e) {
				System.out.println("Could not traverse the path " + filename );
			}
		}
	}
}