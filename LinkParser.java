
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This method contains just a single static method that can
 * be used to extract links from a web page.
 */
public class LinkParser {
	
	/**
	 * Define some regular expression patterns that will be used to find the
	 * link tags on the page and extract the link URLs from them.
	 */
	private final static Pattern linkTagPattern = Pattern.compile("<a +([^>]+)",Pattern.CASE_INSENSITIVE);
	private final static Pattern imgOrFrameTagPattern = Pattern.compile("<(img|frame) +([^>]+)",Pattern.CASE_INSENSITIVE);
	private final static Pattern hrefPattern = Pattern.compile("href *= *(\"|')([^\"']+)(\"|')",Pattern.CASE_INSENSITIVE);
	private final static Pattern srcPattern = Pattern.compile("src *= *(\"|')([^\"']+)(\"|')",Pattern.CASE_INSENSITIVE);
	private final static Pattern protocolPattern = Pattern.compile("^[a-z]+:.*");
	
	
	/**
	 * Parses text read from an InputStream and tries to extract web links from it.
	 * The input should be HTML (or XHTML) text.  This method parses the HTML looking
	 * for hyperlinks to other web pages and links to images.  Any links that are
	 * found are dropped into an arraylist that is provided as a parameter.
	 * 
	 * This method is meant to be used for input that has a content type of
	 * "text/html" or "application/xhtml+xml".  Content type should be known
	 * before calling the method.  When applied to other content types,
	 * results will probably not be meaningful.
	 * 
	 * @param input the input stream from which the text will be read.
	 * @param baseURL should be the URL of the page that is being read.  Web pages can
	 *    contain "relative links", which are links that are based on the URL
	 *    of the page on which they occur.  In order to expand relative links to
	 *    complete URLs, a "base URL" is required.
	 * @param links links from the web page will be dropped into this arraylist.  The list
	 *    must be non-null.  The links placed in this arraylist are simply links found 
	 *    in "A" and "IMG" tags on the web page
	 * @return the number of lines that were read from the input.
	 * @throws IOException If an IO error occurs while reading the contents of the page.
	 */
	public static int grabReferences(InputStream input, URL baseURL,
			ArrayList<URL> links) throws IOException {
		int lineCt = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			lineCt++;
			Matcher matcher1 = imgOrFrameTagPattern.matcher(line);
			while (matcher1.find()) {
				String tag = matcher1.group();
				String tagName = matcher1.group(1).toLowerCase();  // "img" or "frame"
				Matcher matcher2 = srcPattern.matcher(tag);
				if (matcher2.find()) {
					String link = matcher2.group(2);
					try {
						links.add(new URL(baseURL,link));
					}
					catch (Exception e) {
					}
				}
			}
			Matcher matcher3 = linkTagPattern.matcher(line);
			while (matcher3.find()) {
				String tag = matcher3.group();
				Matcher matcher4 = hrefPattern.matcher(tag);
				if (matcher4.find()) {
					String link = matcher4.group(2);
					String lc = link.toLowerCase();
					if (!(lc.startsWith("http:") || lc.startsWith("https:"))
							&& protocolPattern.matcher(lc).matches())
						continue;  // don't process funny protocol names; only http(s)
					if (link.indexOf("#") >= 0)
						continue;  // don't process URLs to document "fragments"
					try {
						links.add(new URL(baseURL,link));
					}
					catch (Exception e) {
					}
				}
			}
		}
		return lineCt;
	}

}
