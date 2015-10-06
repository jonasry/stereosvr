package com.jonasry.stereosvr;

import static com.jonasry.stereosvr.Barrel.applyCorrection;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Capture {
	private static final double ROT = 270.0;
	private static final int VERTICAL_OFFSET = 22;
	private static final String SIZE = "1920x1080";
	private static final String DEVICE_PREFIX = "V4L2";
	private static final String DEVICE_PATH = "/dev/video";
	private static final String LEFT = "0";
	private static final String RIGHT = "1";

	public static File captureAndSaveImage(String fileName) throws InterruptedException, ExecutionException, IOException {
		final ExecutorService service = Executors.newFixedThreadPool(2);
		final long start = System.nanoTime();
		try {
			final Future<BufferedImage> leftFuture = capture(service, "left", DEVICE_PATH + LEFT, 0);
			final Future<BufferedImage> rightFuture = capture(service, "right", DEVICE_PATH + RIGHT, VERTICAL_OFFSET);
			return Image.writeStereoImage(applyCorrection(leftFuture.get()), applyCorrection(rightFuture.get()), fileName);

		} finally {
			final long duration = System.nanoTime() - start;
			System.out.println("total: " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");
			service.shutdown();
		}
	}

	public static Future<BufferedImage> capture(ExecutorService service, final String description, final String path, final int verticalCorrection) {
		final String device = String.format("%s:%s", DEVICE_PREFIX, path);

		final Callable<BufferedImage> cmd = new Callable<BufferedImage>() {
			@Override
			public BufferedImage call() throws Exception {
				final long start = System.nanoTime();

				final ProcessBuilder b = new ProcessBuilder();
				b.command("fswebcam", "--png", "0", "-d", device, "-r", SIZE, "--rotate", String.valueOf(ROT), "--crop", "960x1080,60x" + (420 + verticalCorrection), "--no-banner", "-");
				System.out.println(description + ": " + flatten(b.command(), " "));

				final Process p = b.start();
				final BufferedImage imageIn = Image.read(p.getInputStream());
				p.waitFor();

				final long duration = System.nanoTime() - start;
				System.out.println(description + ": " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");

				return imageIn;
			}
		};

		return service.submit(cmd);
	}

	private static String flatten(List<String> l, String s) {
		if (l.isEmpty()) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		for (String x : l) {
			sb.append(x).append(s);
		}
		sb.setLength(sb.length() - s.length());
		return sb.toString();
	}
}
