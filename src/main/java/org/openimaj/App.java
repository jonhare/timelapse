package org.openimaj;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	@Option(name = "--width", aliases = "-w", usage = "capture width")
	int width = 1600;

	@Option(name = "--height", aliases = "-h", usage = "capture height")
	int height = 1200;

	@Option(name = "--millis-between-frames", aliases = "-m", usage = "milliseconds between frames")
	int millisBetweenFrames = 15000;

	@Option(
			name = "--output",
			aliases = "-o",
			usage = "output path (c printf format with %d for frame number)")
	String outputPath = null;

	@Option(name = "--camera", aliases = "-c", usage = "camera id")
	int cam = 0;

	@Option(name = "--num-frames", aliases = "-f", usage = "number of frames to capture (<=0 for unlimited)")
	int numFrames = 0;

	@Option(name = "--list-devices", aliases = "-l", usage = "list the devices and exit")
	boolean list;

	@Option(name = "--preview", aliases = "-p", usage = "show a live camera preview")
	boolean preview;

	public static void main(String[] args) throws IOException {
		final App lapse = new App();
		final CmdLineParser parser = new CmdLineParser(lapse);

		try {
			parser.parseArgument(args);

			if (!lapse.list && lapse.outputPath == null)
				throw new CmdLineException(parser, "Output path must be specified");
		} catch (final CmdLineException e) {
			System.err.println(e);
			parser.printUsage(System.out);
			return;
		}

		lapse.exec();
	}

	private void exec() throws IOException {
		if (list) {
			final List<Device> devs = VideoCapture.getVideoDevices();

			for (int i = 0; i < devs.size(); i++) {
				System.out.println(i + "\t" + devs.get(i));
			}
		} else {
			final VideoCapture vc = new VideoCapture(width, height, VideoCapture.getVideoDevices().get(cam));

			if (preview)
				VideoDisplay.createVideoDisplay(vc);

			// makedirs
			new File(String.format(outputPath, 0)).getParentFile().mkdirs();

			int i = 0;
			while (true) {
				if (numFrames > 0 && i >= numFrames) {
					break;
				}

				capture(vc, i);

				try {
					Thread.sleep(millisBetweenFrames);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				i++;
			}
		}
	}

	private void capture(VideoCapture vc, int i) throws IOException {
		final MBFImage image = vc.getNextFrame();

		final File file = new File(String.format(outputPath, i));

		System.out.println("Writing " + file);
		ImageUtilities.write(image, file);
	}
}
