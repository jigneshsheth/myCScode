
/**
 * The purpose of Driver is to tie together all function calls by passing in
 * arguments. The arguments we pass get traversed by directory traverser and
 * then parsed by the file parser. Finally we create a map of the words,
 * files, and positions in inverted index. We also call argument parser
 * class to check which directory we need to access and what file we want to
 * write out to.
 * 
 * @author: Paul Hundal
 */
public class Driver {

	public static void main(String[] args) {

		InvertedIndex index = new InvertedIndex();
		ArgumentParser arguments = new ArgumentParser(args);

		if (arguments.hasValue("-d")) {

			DirectoryTraverser.traverseDirectory(arguments.getValue("-d"), index);
			if (arguments.hasValue("-i")) {
				index.printMap(arguments.getValue("-i"));
			} else {
				index.printMap("invertedindex.txt");
			}
		}
	}
}