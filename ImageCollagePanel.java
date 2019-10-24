import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Shows a "collage" of images, with an images placed at a random location
 * in the display.  This class is passive; images must be added by calling
 * the addImage() method.  Note that this panel is not meant to be resized
 * once it has appeared on the screen.
 */
public class ImageCollagePanel extends JPanel {
	
	private BufferedImage OSC; // Original offscreen copy of the display.
	private Graphics OSG;      // Graphics context for drawing to OSC.
	
	private Display display; // The display panel, which fills most of this panel.
	
	private static Color gray = new Color(220,220,220);
	
	/**i
	 * Add an image to the display.  The image is placed at a random position
	 * that allows half of the image to lie outside the display horizontally
	 * and vertically.  If the image is too big, it is scaled so that its
	 * width and height are at most 1/3 of the display width and height.
	 */
	public void addImage(BufferedImage image) {
		if (OSG == null) {
			return; // just in case this method is called before display.paintComponent()
		}
		if (image == null) {
			return; // to be safe, check for and ignore null parameter.
		}
		int width = image.getWidth();
		int height = image.getHeight();
		double xscale = 1, yscale = 1;
		if (width > display.getWidth()/3)
			xscale = display.getWidth()/3.0/width;
		if (height > display.getHeight()/3)
			yscale = display.getHeight()/3.0/height;
		if (xscale < 1 || yscale < 1) { 
			   // scale uniformly so that the displayed image is not more than
			   // 1/4 of the display, either horizontally or vertically
			double scale = Math.min(xscale, yscale);
			width = (int)(scale*width);
			height = (int)(scale*height);
		}
		int x = (int)(display.getWidth()*Math.random()) - width/2;
		int y = (int)(display.getHeight()*Math.random()) - height/2;
		OSG.drawImage(image, x, y, width, height, null);
		display.repaint();
	}
	
	
	/**
	 * Constructor creates a display panel to fill the center of the panel,
	 * with a button at the top for clearing the screen.
	 */
	public ImageCollagePanel() {
		this.setBackground(Color.DARK_GRAY);
		this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
		this.setLayout(new BorderLayout(3,3));
		display = new Display();
		display.setPreferredSize( new Dimension(1200, 900) );
		this.add(display, BorderLayout.CENTER);
		JButton clear = new JButton("CLEAR");
		JPanel top = new JPanel();
		top.setBackground(Color.WHITE);
		top.add(clear);
		this.add(top, BorderLayout.NORTH);
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				OSG.setColor(gray);
				OSG.fillRect(0, 0, OSC.getWidth(), OSC.getHeight());
				display.repaint();
			}
		});
	}
	
	
	/**
	 * This class defines the display that fills most of the panel.
	 * The display simply draws an off-screen canvas to the screen.
	 * When an image is to be added, it is actually drawn to the
	 * canvas, and then the canvas is copied to the screen.
	 */
	private class Display extends JPanel {
		protected void paintComponent(Graphics g) {
			if (OSC == null) {
				    // The canvas is actually created the first time paintComponent
				    // is called, when the program first starts.  It is done here
				    // since it can't be done in the constructor (because the width
				    // and height of the display have not been set when the constructor
				    // is called).
				OSC = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				OSG = OSC.getGraphics();
				OSG.setColor(gray);
				OSG.fillRect(0, 0, getWidth(), getHeight());
			}
			g.drawImage(OSC, 0, 0, null);
		}
	}
	
}
