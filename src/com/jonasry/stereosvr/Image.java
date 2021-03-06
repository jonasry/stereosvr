package com.jonasry.stereosvr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class Image {
	public static String getType(String name) {
		try {
			return name.substring(name.lastIndexOf('.') + 1);

		} catch (RuntimeException e) {
			throw new IllegalArgumentException("File name does not have a type: " + name);
		}
	}

	public static File writeStereoImage(final BufferedImage left, final BufferedImage right, String fileName) throws IOException {
		final File file = new File(fileName);
		write(createStereoImage(left, right), fileName, new FileOutputStream(file));
		return file;
	}

	public static void writeStereoImage(final BufferedImage left, final BufferedImage right, OutputStream outputStream) throws IOException {
		write(createStereoImage(left, right), "out.png", outputStream);
	}

	public static void write(BufferedImage image, String name, OutputStream out) throws IOException {
		System.out.println("Writing " + name + ". W:" + image.getWidth() + " H:" + image.getHeight());
		final long start = System.nanoTime();
		ImageIO.write(image, getType(name), out);
		final long duration = System.nanoTime() - start;
		System.out.println("Saved " + name + " in " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");
	}

	public static BufferedImage read(String name) throws IOException {
		return ImageIO.read(new File(name));
	}

	public static BufferedImage read(InputStream inputStream) throws IOException {
		return ImageIO.read(inputStream);
	}

	public static BufferedImage createStereoImage(BufferedImage left, BufferedImage right) {
		final BufferedImage imageOutput = new BufferedImage(left.getWidth() * 2, left.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = imageOutput.createGraphics();
		g.drawImage(left, 0, 0, null);
		g.drawImage(right, left.getWidth(), 0, null);
		g.dispose();
		return imageOutput;
	}
}
