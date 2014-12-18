import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The purpose of Driver is to tie together all function calls by passing in
 * arguments. The arguments we pass get traversed by directory traverser and
 * then parsed by the file parser. Finally we create a map of the words, files,
 * and positions in inverted index. We also call argument parser class to check
 * which directory we need to access and what file we want to write out to. We
 * also check to see if a partial search needs to be done, and appropriately
 * call that method.
 * 
 * @author: Paul Hundal
 */
public class Driver {

	/**
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Error: No arguments given.");

		} else {
			ArgumentParser arguments = new ArgumentParser(args);
			InvertedIndex index = new InvertedIndex();
			int thread = 5;

			if (arguments.hasFlag("-t") && arguments.hasValidInteger("-t")) {
				thread = arguments.getInteger("-t");
			}

			WorkQueue workers = new WorkQueue(thread);
			InvertedIndexBuilder traverser = new InvertedIndexBuilder(workers);
			QueryFileParser results = new QueryFileParser(workers);

			if (arguments.hasFlag("-u") && arguments.hasURL("-u")) {
				traverser.seedIndex(arguments.getValue("-u"), index);
			}

			if (arguments.hasFlag("-d") && !arguments.hasFlag("-q")) {
				traverser.parser(arguments.getValue("-d"), ".txt", index);
				index.printMap("invertedindex.txt");
			}
			if (arguments.hasFlag("-d") && !arguments.hasFlag("-i")) {
				traverser.parser(arguments.getValue("-d"), ".txt", index);
			}
			if (arguments.hasFlag("-d") && arguments.hasFlag("-q")
					&& !arguments.hasFlag("-i") && !arguments.hasFlag("-r")) {
				System.out.println("Cannot print with these args.");
			}
			if (arguments.hasFlag("-i")) {
				String filename = "invertedindex.txt";
				if (arguments.getValue("-i") != null) {
					filename = arguments.getValue("-i");
				}
				index.printMap(filename);
			} else if (!arguments.hasFlag("-i") && arguments.hasFlag("-r")) {
				index.printMap("invertedindex.txt");
			}
			if (arguments.hasFlag("-q")) {

				if (Files.isReadable(Paths.get(arguments.getValue("-q")))) {
					if (arguments.getValue("-q").toLowerCase().endsWith(".txt")) {
						results.search((arguments.getValue("-q")), index);
					}
				}
				if (arguments.hasFlag("-r")) {
					String filename = "searchresults.txt";

					if (arguments.getValue("-r") != null) {
						filename = arguments.getValue("-r");
					}
					results.printResults(filename);
				}
			}
			traverser.shutdown();
			results.shutdown();
		}

	}
}