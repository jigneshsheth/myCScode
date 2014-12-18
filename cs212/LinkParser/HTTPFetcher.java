import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * An abstract class designed to make fetching the results of different HTTP
 * operations easier.
 * 
 * @author Sophie Engle & Paul Hundal
 * @author CS 212 Software Development
 * @author University of San Francisco
 * 
 * @see HTTPFetcher
 * @see HTMLFetcher
 * @see HeaderFetcher
 */
public class HTTPFetcher {
	/** Port used by socket. For web servers, should be port 80. */
	private static final int PORT = 80;

	/** The URL to fetch from a web server. */
	private final URL url;

	private final StringBuilder html;

	private final StringBuilder header;

	private boolean isHead;

	/**
	 * Initializes this fetcher. Must call {@link #fetch()} to actually start
	 * the process.
	 * 
	 * @param url
	 *            - the link to fetch from the webserver
	 * @throws MalformedURLException
	 *             if unable to parse URL
	 */
	public HTTPFetcher(String url) throws MalformedURLException {
		this.url = new URL(url);
		this.html = new StringBuilder();
		this.header = new StringBuilder();
		this.isHead = true;
	}

	/**
	 * Returns the port being used to fetch URLs.
	 * 
	 * @return port number
	 */
	public int getPort() {
		return PORT;
	}

	/**
	 * Returns the URL being used by this fetcher.
	 * 
	 * @return URL
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Return the header
	 * 
	 * @return
	 */
	public String getHeader() {
		return header.toString();
	}

	/**
	 * Crafts the HTTP request from the URL. Must be overridden.
	 * 
	 * @return HTTP request
	 */
	protected String craftRequest() {
		String host = this.getURL().getHost();
		String resource = this.getURL().getFile().isEmpty() ? "/" : this
				.getURL().getFile();

		StringBuffer output = new StringBuffer();
		output.append("GET " + resource + " HTTP/1.1\n");
		output.append("Host: " + host + "\n");
		output.append("Connection: close\n");
		output.append("\r\n");

		return output.toString();
	}

	/**
	 * Will skip any headers returned by the web server, and then output each
	 * line of HTML to the console.
	 */
	protected String processLine(String line) {
		if (isHead) {
			if (line.trim().isEmpty()) {
				// Check if we hit the blank line separating headers and HTML
				isHead = false;
			}
		} else {
			return line + "\n";
		}
		return "";
	}

	/**
	 * Gets the html
	 * 
	 * @return
	 */
	public String getHTML() {
		return html.toString();
	}

	/**
	 * Connects to the web server and fetches the URL and appends the header.
	 */
	public void fetchHeader() {
		try (Socket socket = new Socket(url.getHost(), PORT);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());) {

			String request = craftRequest();
			writer.println(request);
			writer.flush();

			String nextLine = reader.readLine();
			while (nextLine != null) {
				if (nextLine.isEmpty()) {
					break;
				} else {
					header.append(nextLine + " ");
					nextLine = reader.readLine();
				}
			}
			while (nextLine != null) {
				html.append(processLine(nextLine));
				nextLine = reader.readLine();
			}
		} catch (IOException e) {
		}
	}

}