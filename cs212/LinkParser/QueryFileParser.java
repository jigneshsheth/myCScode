/**
 * Class: Search_Word
 * Purpose: The purpose of this class is to pass query lines to 
 * inverted index. The lines will be passes as search result arrays
 * and received in the same fashion. The map will add these search
 * result objects in and print them out to a file. 
 * @author Paul Hundal
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryFileParser {
	private final LinkedHashMap<String, ArrayList<SearchResults>> map;
	private final WorkQueue workers;
	private static Logger log = LogManager.getLogger();
	private Lock lock;
	private int pending;

	public QueryFileParser(WorkQueue minions) {
		map = new LinkedHashMap<String, ArrayList<SearchResults>>();
		workers = minions;
		pending = 0;
		lock = new Lock();
		log.info("Building my search results.");
	}

	/**
	 * Increments the count of the pending variable.
	 */
	private synchronized void incrementPending() {
		pending++;
		log.debug("Pending is now {}", pending);
	}

	/**
	 * Decreases the count of the pending variable.
	 */
	private synchronized void decrementPending() {
		pending--;
		log.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}

	/**
	 * Waits until all threads have finished.
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
				log.debug("Waiting until finished");
				this.wait();
			}
		} catch (InterruptedException e) {
			log.debug("Finish interrupted", e);
		}
	}

	/**
	 * Shuts down
	 */
	public void shutdown() {
		log.debug("Shutting down");
		finish();
		workers.shutdown();
	}

	/**
	 * Normalizes a word by converting it to lower case, removing all non-word
	 * characters using the {@code "\\W"} regular expression, removing all
	 * {@code "_"} underscore characters, and removing any unnecessary extra
	 * whitespace at the start of end of the word.
	 * 
	 * @param word
	 *            to normalize
	 * @return normalized version of the word
	 */
	public static String normalizeWord(String word) {
		if (word != null && !word.isEmpty()) {
			word = word.replaceAll("[\\W_]", "").trim().toLowerCase();
		}
		return word;
	}

	/**
	 * Search the query file to find the query words and add them in to an array
	 * list.
	 * 
	 * @param filename
	 *            : file of queries
	 */
	public void search(String filename, InvertedIndex index) {
		Path file = Paths.get(filename);

		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String parseWord;
			while ((parseWord = reader.readLine()) != null) {
				lock.acquireWriteLock();
				map.put(parseWord, null);
				lock.releaseWriteLock();
				workers.execute(new Searcher(parseWord, index));
			}
			finish();
		} catch (IOException e) {
			System.out.println("The file " + filename + " could not be read.");
		}
	}

	private class Searcher implements Runnable {
		private final String inputline;
		private InvertedIndex index;

		public Searcher(String inputline, InvertedIndex index) {
			this.inputline = inputline;
			this.index = index;
			incrementPending();
			log.debug("Starting searcher.");
		}

		@Override
		public void run() {
			ArrayList<String> querylist = new ArrayList<String>();
			if (inputline != null && !inputline.isEmpty()) {
				String[] wordarray = inputline.split("\\s");
				for (String words : wordarray) {
					normalizeWord(words);
					querylist.add(words);
				}

				ArrayList<SearchResults> results = index
						.partialSearch(querylist);
				lock.acquireWriteLock();
				map.put(inputline, results);
				lock.releaseWriteLock();
			}
			decrementPending();
		}
	}

	/**
	 * Method: Print_Results Purpose: The purpose of this method is to iterate
	 * the map of search results and print them out according to project 2
	 * requirements.
	 * 
	 * @param filename
	 *            : the file you would like search results to print to.
	 */
	public void printResults(String filename) {
		lock.acquireReadLock();
		Path path = Paths.get(filename);

		try (BufferedWriter writer = Files.newBufferedWriter(path,
				Charset.forName("UTF-8"))) {
			for (String query : map.keySet()) {
				writer.write(query);
				writer.newLine();
				for (SearchResults sr : map.get(query)) {
					writer.write('"' + sr.getPath() + '"' + ", ");
					writer.write(sr.getFrequency() + ", " + sr.getPosition());
					writer.newLine();
				}
				writer.newLine();
			}
			writer.newLine();
		} catch (IOException e) {
			System.out.println("The file " + filename
					+ " could not be written out to.");
		}
		lock.releaseReadLock();
	}
}