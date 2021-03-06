import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * The purpose of our inverted index class is to create a data structure where
 * the key is our word parsed from our file parser and the value is a pair of
 * file names and the positions of the word in that file. We take our parameter
 * input from the file parser and store these keys and values in a tree map
 * called invertedIndex. Once the map is full, we print the map out to a file.
 * 
 * @author Paul Hundal
 * 
 */
public class InvertedIndex {

	private final TreeMap<String, TreeMap<String, ArrayList<Integer>>> invertedIndex; 
	private static Logger log = LogManager.getLogger();
	private Lock lock;


	public InvertedIndex() {
		invertedIndex = new TreeMap<>();
		lock = new Lock();
		log.info("Building my inverted index.");
	}

	/**
	 * The purpose of this method is to take in the input from file parser for 3
	 * different things. We want the actual word, the file that word is located
	 * in, and its position in that file. We want to fill that map based on if
	 * that word is already there or not. If it is not, we add the word and the
	 * file it is in. If the word is already there we update the file as
	 * necessary. The position is always incremented because we must keep track
	 * of each new location.
	 */
	public void addWord(String word, String file, int position) {
		lock.acquireWriteLock();
		if (!invertedIndex.containsKey(word)) {
			invertedIndex.put(word, new TreeMap<String, ArrayList<Integer>>());
			invertedIndex.get(word).put(file, new ArrayList<Integer>());

		} else if (invertedIndex.containsKey(word)
				&& !invertedIndex.get(word).containsKey(file)) {
			invertedIndex.get(word).put(file, new ArrayList<Integer>());
		}
		invertedIndex.get(word).get(file).add(position);
		lock.releaseWriteLock();
	}

	/**
	 * The add all method takes in a global index as input from the 
	 * inverted index builder class to add all to the local index rather
	 * than one at a time. This method is for efficiency purposes.
	 * @param globalIndex
	 */
	public void addAll(InvertedIndex globalIndex) {
		lock.acquireWriteLock();
		for(String word : globalIndex.invertedIndex.keySet()) {
			if(!invertedIndex.containsKey(word)) {
				invertedIndex.put(word, globalIndex.invertedIndex.get(word));
			} else {
				for(String allpaths : globalIndex.invertedIndex.get(word).keySet()) {
					TreeMap<String, ArrayList<Integer>> indexmap = invertedIndex.get(word);
					if(!indexmap.containsKey(allpaths)) {
						indexmap.put(allpaths, globalIndex.invertedIndex.get(word).get(allpaths));
					} else {
						ArrayList<Integer> position = indexmap.get(allpaths);
						position.addAll(globalIndex.invertedIndex.get(word).get(allpaths));
					}
				}

			}
		}
		lock.releaseWriteLock();
	}

	/**
	 * The partial search method that takes in the line of partial search results and 
	 * returns the list of sorted results. 
	 * @param querywords
	 * @param threads
	 * @return
	 */
	public ArrayList<SearchResults> partialSearch(ArrayList<String> querywords) {
		ArrayList<SearchResults> finalResults = new ArrayList<SearchResults>();
		HashMap<String, SearchResults> tempResults = new HashMap<String, SearchResults>();

		lock.acquireReadLock(); 
		for(String word : querywords) {
			for(String searchword : invertedIndex.tailMap(word).keySet()) {
				if(!searchword.startsWith(word)) {
					break;
				} else {
					for(String paths : invertedIndex.get(searchword).keySet()) {
						if(tempResults.containsKey(paths)){
							int freqsize = invertedIndex.get(searchword).get(paths).size();
							tempResults.get(paths).addFrequency(freqsize);
							tempResults.get(paths).setPosition(invertedIndex.get(searchword).get(paths).get(0));

						} else {

							int freqsize = invertedIndex.get(searchword).get(paths).size();
							tempResults.put(paths, new SearchResults(paths, freqsize, invertedIndex.get(searchword).get(paths).get(0)));
						}

					} 
				}

			}
		}
		lock.releaseReadLock(); // TODO
		finalResults.addAll(tempResults.values());
		Collections.sort(finalResults);
		return finalResults;
	}		

	/**
	 * The purpose of this method is to create a print out to a file of our map.
	 * We create a buffer to write out each, and pass the file we want to write
	 * out to our buffered writer. We then iterate each word, file, and position
	 * and print out each of these three elements to our file. Note: We must
	 * produce spaces and correct formatted lines in order to get exact output
	 * we desire. It is necessary to close and flush the map.
	 */
	public void printMap(String filename) {
		lock.acquireReadLock();
		Path path = Paths.get(filename);

		try (
				BufferedWriter outputMap = Files.newBufferedWriter(path, Charset.forName("UTF-8"));
				){
			for (String word : invertedIndex.keySet()) {
				outputMap.write(word);
				outputMap.newLine();

				for (String files : invertedIndex.get(word).keySet()) {
					outputMap.write("\"" + files + "\"");

					for (Integer position : invertedIndex.get(word).get(files)) {
						outputMap.write(", ");
						outputMap.write(position.toString());
					}
					outputMap.newLine();
				}
				outputMap.newLine();
			}
			outputMap.newLine();
		} catch (IOException e) {
			System.out.println("Your text file " + filename + " cannot be accessed.");
		}
		lock.releaseReadLock();
	}
}