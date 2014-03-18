package matt.test.webcam;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.common.util.concurrent.RateLimiter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Generated JavaDoc Comment.
 *
 * @author Matt Nathan
 */
public class Main {

  private static JLabel imageHolder;

  public static void main(String[] args) {
    final Webcam webcam = Webcam.getDefault();

    Dimension targetSize = null;
    double maxArea = 0;
    for (Dimension size : webcam.getViewSizes()) {
      double area = size.getWidth() * size.getHeight();
      if (area > maxArea) {
        maxArea = area;
        targetSize = size;
      }
    }

    if (targetSize != null) {
      webcam.setViewSize(targetSize);
    }
    webcam.open();

    final BufferedImage image = webcam.getImage();
    final Thread capture = new Thread("Image Capture") {
      // how many frames per second will we be rendering? More == higher cpu usage
      private final RateLimiter limiter = RateLimiter.create(25);

      @Override
      public void run() {
        while (!isInterrupted()) {
          limiter.acquire();
          final BufferedImage newImage = webcam.getImage();
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              Icon oldIcon = imageHolder.getIcon();
              imageHolder.setIcon(new ImageIcon(newImage));
              flushOldImage(oldIcon);
            }
          });
        }
      }
    };
    capture.setDaemon(true);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame("Webcam Test");
        frame.getContentPane().setLayout(new BorderLayout());
        imageHolder = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(imageHolder, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // start capturing images
        capture.start();
      }
    });

  }

  private static void flushOldImage(Icon oldIcon) {
    if (oldIcon instanceof ImageIcon) {
      Image oldImage = ((ImageIcon) oldIcon).getImage();
      oldImage.flush(); // get rid of old resources
    }
  }
}
