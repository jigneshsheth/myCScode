import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InvertedIndexBuilder {

	private static final Logger logger = LogManager.getLogger();
	private static final String pregex = "^([a-zA-Z][a-zA-Z0-9+.-]*)://";
	private static final String dregex = "([^/?#]+)";
	private static final String rregex = "(/[^?#]*)?";
	private static final String qregex = "(?:\\?([^#]*))?";
	private static final String fregex = "(?:#(.*))?$";
	private static final String URL = pregex + dregex + rregex + qregex
			+ fregex;
	private static final String REGEX = "(?i)a\\s*?href\\s*?=\\s*?\"\\s*?(("
			+ URL + ")|(.*?))\\s*?\"\\s*?";
	private static final String hash = "[?#].*";
	private final HashSet<URI> links;
	private final WorkQueue workers;
	private final TreeSet<Path> paths;
	private final Lock lock;
	private final int MAX = 50;
	private final int GROUP = 1;
	private int pending;

	/**
	 * The constructor, which takes in number of threads to run.
	 * 
	 * @param threads
	 */
	public InvertedIndexBuilder(WorkQueue minions) {
		pending = 0;
		workers = minions;
		lock = new Lock();
		paths = new TreeSet<Path>();
		links = new HashSet<>();
	}

	/**
	 * Gets all of the paths from a set.
	 * 
	 * @return: unmodfilePaths
	 */
	public Set<Path> getPaths() {

		finish();
		lock.acquireReadLock();
		logger.debug("Getting paths.");
		Set<Path> unmodfilePaths = Collections.unmodifiableSet(paths);
		lock.releaseReadLock();
		return unmodfilePaths;

	}

	/**
	 * Adds paths in to a set.
	 * 
	 * @param paths
	 */
	public void addPaths(TreeSet<Path> paths) {
		lock.acquireWriteLock();
		this.paths.addAll(paths);
		lock.releaseWriteLock();
	}

	/**
	 * Waits until all threads have finished.
	 */
	public synchronized void finish() {
		try {

			while (pending > 0) {
				logger.debug("Waiting until finished.");
				this.wait();
			}
		} catch (InterruptedException e) {
			System.out.println("Interuppted!");
		}

	}

	/**
	 * Resets the counters.
	 */
	public void reset() {
		finish();
		lock.acquireWriteLock();
		paths.clear();
		logger.debug("Counters reset.");
		lock.releaseWriteLock();

	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		logger.debug("Shutting down.");
		finish();
		workers.shutdown();

	}

	/**
	 * Increments the pending count.
	 */
	private synchronized void incrementPending() {
		pending++;
		logger.debug("Pending is now {}", pending);
	}

	/**
	 * Decrements the pending count.
	 */
	private synchronized void decrementPending() {
		pending--;
		logger.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}

	/**
	 * Calls the directory traverser and sends the files to file parser.
	 * 
	 * @param dir
	 * @param ext
	 * @param index
	 */
	public void parser(String dir, String ext, InvertedIndex index) {
		directoryTraverser(dir, ext);

		for (Path file : getPaths()) {
			if (file != null) {
				workers.execute(new FileWorker(file.toString(), index));
			} else {
				System.out.println("Canot read files.");
			}
		}
		finish();
	}

	/**
	 * Traverses the directory in a multi-threaded manner.
	 * 
	 * @param directory
	 * @param ext
	 */
	public void directoryTraverser(String directory, String ext) {
		Path dir = Paths.get(directory);
		logger.debug("Starting traverser now");

		if (Files.isDirectory(dir)) {
			workers.execute(new DirectoryWorker(dir, ext));
		} else if (Files.isReadable(dir)
				&& dir.toString().toLowerCase().endsWith(ext)) {
			lock.acquireWriteLock();
			paths.add(dir);
			lock.releaseWriteLock();
		}
	}

	/**
	 * Sub class to work on traversing the directory.
	 * 
	 * @author p_hundal
	 * 
	 */
	private class DirectoryWorker implements Runnable {

		private final Path directory;
		private String extension;

		/**
		 * Constructor for the worker class.
		 * 
		 * @param directory
		 * @param extension
		 */
		public DirectoryWorker(Path directory, String extension) {
			logger.debug("Worker created for {}", directory);
			this.directory = directory;
			this.extension = extension;
			incrementPending();
		}

		@Override
		public void run() {
			TreeSet<Path> temp = new TreeSet<Path>();

			try (DirectoryStream<Path> stream = Files
					.newDirectoryStream(directory)) {

				for (Path path : stream) {
					if (Files.isDirectory(path)) {
						workers.execute(new DirectoryWorker(path, extension));
					} else if (Files.isReadable(path)
							&& path.toString().toLowerCase()
									.endsWith(extension)) {
						temp.add(path);
					}
				}
				addPaths(temp);
			} catch (IOException e) {
				logger.warn("Unable to parse {}", directory);
				logger.catching(Level.DEBUG, e);
			}
			decrementPending();

			logger.debug("Worker finished {}", directory);
		}
	}

	/**
	 * Sub class to parse files.
	 * 
	 * @author Paul Hundal
	 * 
	 */
	private class FileWorker implements Runnable {
		private final String path;
		private InvertedIndex index;
		private InvertedIndex localIndex;

		/**
		 * Constructor for the worker class of file parser.
		 * 
		 * @param path
		 * @param index
		 */
		public FileWorker(String path, InvertedIndex index) {
			logger.debug("Starting to parse files");
			this.path = path;
			this.index = index;
			localIndex = new InvertedIndex();

			incrementPending();
		}

		@Override
		public void run() {
			String[] words;
			int position = 1;

			try (BufferedReader fileReader = Files.newBufferedReader(
					Paths.get(path), Charset.forName("UTF-8"));

			) {
				while (fileReader.ready()) {
					words = fileReader.readLine().split("\\s");
					for (String word : words) {
						word = word.replaceAll("\\W", "").replaceAll("_", "")
								.trim().toLowerCase();
						if (word != null && !word.isEmpty()) {
							localIndex.addWord(word, path, position);
							position++;
						}
					}
				}
				index.addAll(localIndex);
			} catch (FileNotFoundException e) {
				System.out.println("The file " + path
						+ " you tried to read does not exist! ");
			} catch (IOException e) {
				System.out.println("Cannot read the given file.");

			}
			decrementPending();
		}
	}

	/**
	 * This builds the index crawling the web.
	 * 
	 * @param seed
	 * @param index
	 */
	public void seedIndex(String seed, InvertedIndex index) {
		try {
			URL seedURL = new URL(seed);
			lock.acquireWriteLock();
			links.add(seedURL.toURI());
			lock.releaseWriteLock();

			workers.execute(new WebCrawlWorker(seedURL.toURI(), index));

		} catch (MalformedURLException | URISyntaxException e) {
			System.out.println("Could not fetch the seed url.");
		}
		finish();
	}

	/**
	 * The web crawl worker class is a runnable method that takes in the uri and
	 * index, gets the headers, and then checks if the link is valid. After the
	 * check it iterates the temp set of links and adds them in to a master set.
	 * 
	 * @author Paul Hundal
	 * 
	 */
	private class WebCrawlWorker implements Runnable {
		private final URI uri;
		private final InvertedIndex index;

		public WebCrawlWorker(URI uri, InvertedIndex index) {
			this.uri = uri;
			this.index = index;

			incrementPending();
		}

		@Override
		public void run() {
			try {

				HTTPFetcher fetcher = new HTTPFetcher(uri.toString());
				fetcher.fetchHeader();
				String link = fetcher.getHTML();
				Pattern pattern = Pattern.compile(REGEX);
				Matcher matcher = pattern.matcher(link);
				String text = "text/html";
				String head = fetcher.getHeader();

				URL completePath = null;
				lock.acquireWriteLock();
				while (matcher.find() && links.size() < MAX) {
					completePath = new URL(uri.toURL(), matcher.group(GROUP)
							.replaceAll(hash, " "));
					if (head.contains(text)) {
						if (!links.contains(completePath.toURI())) {
							links.add(completePath.toURI());
							workers.execute(new WebCrawlWorker(completePath
									.toURI(), this.index));
						}
					}
				}
				htmlParser(index, link, uri);
				lock.releaseWriteLock();
			} catch (MalformedURLException | URISyntaxException e) {
				System.out.println("Cannot fetch the given URL");
			}
			decrementPending();
		}

		/**
		 * Parses the html in to the index.
		 * 
		 * @param index
		 * @param link
		 * @param url
		 */
		private void htmlParser(InvertedIndex index, String html, URI url) {
			InvertedIndex localIndex = new InvertedIndex();
			ArrayList<String> x = fetchWords(html);
			int count = 1;
			for (String word : x) {
				localIndex.addWord(word, url.toString(), count++);
			}
			index.addAll(localIndex);
		}
	}

	/**
	 * Fetches the webpage at the provided URL, cleans up the HTML tags, and
	 * parses the resulting plain text into words.
	 * 
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 * 
	 * @param url
	 *            - webpage to download
	 * @return list of parsed words
	 */
	public static ArrayList<String> fetchWords(String html) {
		// String html = fetchHTML(url);
		String text = cleanHTML(html);
		return parseWords(text);
	}

	/**
	 * Parses the provided plain text (already cleaned of HTML tags) into
	 * individual words.
	 * 
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 * 
	 * @param text
	 *            - plain text without html tags
	 * @return list of parsed words
	 */
	public static ArrayList<String> parseWords(String text) {
		ArrayList<String> words = new ArrayList<String>();

		for (String word : text.split("\\s+")) {
			word = word.toLowerCase().replaceAll("[\\W_]+", "").trim();

			if (!word.isEmpty()) {
				words.add(word);
			}
		}

		return words;
	}

	/**
	 * Removes all style and script tags (and any text in between those tags),
	 * all HTML tags, and all special characters/entities.
	 * 
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 * 
	 * @param html
	 *            - html code to parse
	 * @return plain text
	 */
	public static String cleanHTML(String html) {
		String text = html;
		text = stripElement("script", text);
		text = stripElement("style", text);
		text = stripTags(text);
		text = stripEntities(text);
		return text;
	}

	/**
	 * Removes everything between the element tags, and the element tags
	 * themselves. For example, consider the html code:
	 * 
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 * 
	 * If removing the "style" element, all of the above code will be removed,
	 * and replaced with the empty string.
	 * 
	 * @param name
	 *            - name of the element to strip, like style or script
	 * @param html
	 *            - html code to parse
	 * @return html code without the element specified
	 */
	public static String stripElement(String name, String html) {
		html = html.replaceAll("\n", " ");
		String regex = "<" + name.toLowerCase() + ".*?>.*?</"
				+ name.toLowerCase() + ".*?";
		String stripped = html.replaceAll("(?i)" + regex, " ");

		return stripped;
	}

	/**
	 * Removes all HTML tags, which is essentially anything between the < and >
	 * symbols. The tag will be replaced by the empty string.
	 * 
	 * @param html
	 *            - html code to parse
	 * @return text without any html tags
	 */
	public static String stripTags(String html) {
		html = html.replaceAll(" \n", " ");
		String regex = "<.*?>";
		html = html.replaceAll(regex, " ");

		return html;
	}

	/**
	 * Replaces all HTML entities in the text with the empty string. For
	 * example, "2010&ndash;2012" will become "20102012".
	 * 
	 * @param html
	 *            - the text with html code being checked
	 * @return text with HTML entities replaced by a space
	 */
	public static String stripEntities(String html) {
		String regex = "&.*?;";
		String noEntities = html.replaceAll(regex, " ");
		return noEntities;

	}
}