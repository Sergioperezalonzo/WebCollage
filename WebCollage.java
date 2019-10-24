import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A program to crawl the web, starting from a given URL, and put any
 * images that are found into a "collage".
 */

public class WebCollage {

    private static ImageCollagePanel collagePanel;

    static HashSet<URL> visitedURLs = new HashSet<URL>();
    static LinkedBlockingQueue<URL> urlQueue = new LinkedBlockingQueue<URL>();
    static ArrayBlockingQueue<BufferedImage> imageQueue = new ArrayBlockingQueue<BufferedImage>(25);


    public static void main(String[] args) {
        ArrayList<URL> links;
        String urlString = JOptionPane.showInputDialog("Enter a URL for an HTML Web page.\n" +
                "(It should be a full URL.)");
        while (true) {
            if (urlString == null) {
                return;  // User canceled, so end program.
            }
            String errorMsg = null;
            try {
                if (! (urlString.startsWith("http://") || urlString.startsWith("https://"))) {
                    urlString = "http://" + urlString;
                }
                URL startURL = new URL(urlString);
                URLConnection conn = startURL.openConnection();
                InputStream in = conn.getInputStream();
                String type = conn.getContentType();
                if ( type != null && (type.startsWith("text/html") 
                        || type.startsWith("application/xhtml+xml")) ) {
                    links = new ArrayList<URL>();
                    int lines = LinkParser.grabReferences(in, startURL, links);
                    if (links.size() == 0)
                        errorMsg = "No links found on page at\n" + urlString;
                    else {
                        in.close();
                        System.out.printf("Read %d lines from start page, got %d links\n", lines, links.size());
                        break; // We have a list of links to work with.
                    }
                }
                in.close();
                errorMsg = String.format("Content type of '%s' is %s, not HTML", urlString, type);
            }
            catch (MalformedURLException e) {
                errorMsg =  String.format("'%s' is not a legal URL.\n", urlString);
            }
            catch (IOException e) {
                errorMsg =  String.format("Error while trying to access '%s':\n  %s", urlString, e);
            }
            catch (Exception e) {
                errorMsg  = String.format("Unexpected error:\n" + e);
            }
            urlString = JOptionPane.showInputDialog("Error: " + errorMsg + "\n" +
                    "Enter a URL for an HTML Web page\n(Or click Cancel)");
        }
        // Got a (hopefully) valid URL to work with, open the window and start downloading.
        JFrame window = new JFrame("Web Collage");
        collagePanel = new ImageCollagePanel();
        window.setContentPane(collagePanel);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = window.getWidth();
        if (width > screensize.width - 100)
            width = screensize.width - 100;
        int height = window.getHeight();
        if (height > screensize.height - 100)
            height = screensize.height - 100;
        window.setSize(width, height);
        window.setLocation(50,50);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);
        startCrawling(links);
    }
    /*
	 *A subroutine that obtains new URLS from webapps
	 *@param url obtains a URL
	 */
    private static boolean isNewURL(URL url) {
        synchronized(visitedURLs) {
            if (visitedURLs.contains(url))
                return false;
            else {
                visitedURLs.add(url);
                return true;
            }                
        }
    }
    /*
     * 
     * 
     */
    private static class proccessLinks extends Thread {  /// process links in the array of queue 
        public void run() {
            while(true) {
                try {
                    
                    URL link = urlQueue.take();
                    processURL(link);
                }
                catch (Exception e) {
                }
            }
        }
    }

    /*
     * 
     */
    private static class DrawImageThread extends Thread {  // draws the image in the panel after obtaining it from webapps
        public void run() {
            while(true) {
                try {
                    BufferedImage img = imageQueue.take();
                    collagePanel.addImage(img);
                    System.out.printf(
                            "ADDED IMAGE. Queue Sizes: links = %d, images = %d\n",
                            urlQueue.size(), imageQueue.size());
                    Thread.sleep(100);
                }
                catch (Exception e) {
                }
            }
        }
    }
    /**
     * Start crawling the web, starting from a list of URLs.
     */
    private static void startCrawling(ArrayList<URL> links) {
        // TODO: Create queues and threads.
        // Then, instead of calling processURL, add urls to url queue.
        
        DrawImageThread t = new DrawImageThread();
        t.start();
        
        for(int i = 0; i < 19; i++){
            
            proccessLinks web = new proccessLinks();
            web.start();
        }

        for (URL url: links) {
            //processURL(url);
            urlQueue.add(url);
            
        }
    }


    /**
     * Download the content from one URL.  If it's an image, queue it to be
     * added to the display panel.  If it's a web page, grab all the links
     * from the web page and queue them for later processing.
     */
    private static void processURL(URL url) {
        try {
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            String type = conn.getContentType();
            if (type.startsWith("text/html") || type.startsWith("application/xhtml+xml")) {
                // The link is to an HTML web page.
                // TODO: Get links from page and add them to the URL queue.

                ArrayList<URL> arraylist = new ArrayList<URL>();

                LinkParser.grabReferences(in,url,arraylist);

                for(URL z : arraylist ){
                    if(isNewURL(z)){

                        //synchronized(urlQueue) {
                            urlQueue.put(z);
                        //}

                    }
                }


                in.close();
            }
            else if (type.startsWith("image")) {
                // The link is to an image.
                in.close();
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    // TODO: Instead of next line, add img to image queue.
                    imageQueue.put(img);
                    
                }
            }
        }
        catch (Exception e){
        }
    }

}