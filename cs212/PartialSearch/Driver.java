
/**
 * The purpose of Driver is to tie together all function calls by passing in
 * arguments. The arguments we pass get traversed by directory traverser and
 * then parsed by the file parser. Finally we create a map of the words,
 * files, and positions in inverted index. We also call argument parser
 * class to check which directory we need to access and what file we want to
 * write out to. We also check to see if a partial search needs to be done,
 * and appropriately call that method.
 * 
 * @author: Paul Hundal
 */


/** Main driver class that handles the running of the program. // TODO Delete this
 */

public class Driver {	

	public static void main(String[] args) {
		
		InvertedIndex index = new InvertedIndex();
		QueryFileParser word = new QueryFileParser();
		ArgumentParser arg = new ArgumentParser(args);
		
		
		if(args == null || args.length == 0) {
			System.out.println("Please enter arguments.");
		}
		
		else if(args.length > 0) {
			
			if(arg.hasFlag("-d")) {
				InvertedIndexBuilder.traverseDirectory(arg.getValue("-d"), index);
			}
			if(arg.hasFlag("-d") && !arg.hasFlag("-q")) {
				index.printMap("invertedindex.txt");
			}
			
			if(arg.hasFlag("-d") && arg.hasFlag("-q") && !arg.hasFlag("-i") && !arg.hasFlag("-r")) {
				System.out.println("No output files to create.");
			}
			
			if(arg.hasFlag("-i")) {
				String value = arg.getValue("-i");
				if(value != null) {
					index.printMap(arg.getValue("-i"));
				} else if(value == null) {
					index.printMap("invertedindex.txt");
				}
			}
			
			if(arg.hasFlag("-q")) {
				String queries = arg.getValue("-q");
				
				if(queries != null) {
					word.search(queries, index);
					
					if(arg.hasFlag("-r")) {
						String results = arg.getValue("-r");
						
						if(results != null) {
							word.printResults(results);
						} else if(results == null){
							word.printResults("searchresults.txt");
						}
					}
				}
			}
		}
		else {
			System.out.println("You have bad arguments.");
			System.out.println("Please remember -d, -q, -i, -r arguments.");
		}
	}
}