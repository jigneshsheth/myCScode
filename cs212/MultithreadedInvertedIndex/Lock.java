import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Provides read-write lock functionality.
 * 
 * @author Paul Hundal
 * 
 */
public class Lock {

        private int readers;
        private boolean writers;

        private static Logger log = LogManager.getLogger();

        public Lock() { // TODO Indentation
        	this.readers = 0;
        	this.writers = false;
        }

        /**
         * Tries to acquire the read lock, waiting if necessary for other threads to
         * be done writing, incrementing the number of readers.
         */
        public synchronized void acquireReadLock() {
                while (writers) {
                        try {
                                this.wait();
                        } catch (InterruptedException e) {
                                 log.error("InterruptedException");
                        }
                }
                readers++;
        }

        /**
         * Releases the read lock, decrementing the number of readers.
         */
        public synchronized void releaseReadLock() {
                readers--;
                this.notifyAll(); // TODO Might be able to do this more efficiently 
        }

        /**
         * Releases the read lock, waiting if necessary for other threads to be done
         * reading and writing, decrementing the number of readers.
         */
        public synchronized void acquireWriteLock() {
                while (readers > 0 || writers) {
                        try {
                                this.wait();
                        } catch (InterruptedException e) {
                                log.error("InterruptedException");
                        }
                }
                writers = true;
        }

        /**
         * Releases the write lock, notifying the threads, decrementing the number
         * of writers.
         */
        public synchronized void releaseWriteLock() {
                writers = false;
                this.notifyAll();
        }

}