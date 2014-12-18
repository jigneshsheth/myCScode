import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InvertedIndexBuilder {
	
private static final Logger logger = LogManager.getLogger();
	
	private final WorkQueue workers;
	private final TreeSet<Path> paths;
	private final Lock lock;
	private int pending;
	
	/**
	 * The constructor, which takes in number of threads to run.
	 * @param threads
	 */
	public InvertedIndexBuilder(int threads) {
		pending = 0;
		workers = new WorkQueue(threads);
		lock = new Lock();
		paths = new TreeSet<>();
	}
	
	/**
	 * Gets all of the paths from a set.  
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
			
			while(pending > 0) {
				logger.debug("Waiting until finished.");
				this.wait();
			}
		} catch(InterruptedException e) {
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
		
		if(pending <= 0) {
			this.notifyAll();
		}
	}
	
	/**
	 * Calls the directory traverser and sends the files to file parser. 
	 * @param dir
	 * @param ext
	 * @param index
	 */
	public void parser(String dir, String ext, InvertedIndex index) { 
		directoryTraverser(dir, ext);
		
		for(Path file: getPaths()) {
			fileParser(file.toString(), index);
		}
		finish();
	}
	
	/**
	 * Traverses the directory in a multi-threaded manner.
	 * @param directory
	 * @param ext
	 */
	public void directoryTraverser(String directory, String ext) {
		Path dir = Paths.get(directory);
		logger.debug("Starting traverser now");
		
			if(Files.isDirectory(dir)) {
				workers.execute(new DirectoryWorker(dir, ext));
			}
			else if (Files.isReadable(dir) && dir.toString().toLowerCase().endsWith(ext)) {
				lock.acquireWriteLock();
				paths.add(dir);
				lock.releaseWriteLock();
			} 
	}
	
	private class DirectoryWorker implements Runnable {
		
		private final Path directory; 
		private String extension;
		
		/**
		 * Constructor for the worker class. 
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
			
			
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
				
				for(Path path : stream) {
					if(Files.isDirectory(path)) {
						workers.execute(new DirectoryWorker(path, extension));
					} else if (Files.isReadable(path) && path.toString().toLowerCase().endsWith(extension)) {
						temp.add(path);
						} 
				}
				addPaths(temp);
			}
			catch (IOException e) {
				logger.warn("Unable to parse {}", directory);
				logger.catching(Level.DEBUG, e);
			}
			decrementPending();
			
			logger.debug("Worker finished {}", directory);
			}
		}
	
	/**
	 * Executes a worker for each path while parsing. 
	 * @param path
	 * @param index
	 */
	public void fileParser(String path, InvertedIndex index) {
		if(path != null) {
			workers.execute(new FileWorker(path, index));
		} else {
			logger.warn("Files can't be read");
		}
	}
	
	private class FileWorker implements Runnable {
		private final String path;
		private InvertedIndex index;
		private InvertedIndex localIndex;

		
		/**
		 * Constructor for the worker class of file parser. 
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

			try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(path), Charset.forName("UTF-8"));
					
				){
					while (fileReader.ready()) {
						words = fileReader.readLine().split("\\s");
						for (String word : words) {
							word = word.replaceAll("\\W", "").replaceAll("_", "").trim().toLowerCase();
							if (word != null && !word.isEmpty()) {
								localIndex.addWord(word, path, position);
								position++;
							}
						}
					}
					index.addAll(localIndex);
				} 
				catch (FileNotFoundException e) {
					System.out.println("The file " + path + " you tried to read does not exist! ");
				} 
				catch (IOException e) {
					System.out.println("Cannot read the given file.");

				}
			decrementPending();
			}
		}
	}
