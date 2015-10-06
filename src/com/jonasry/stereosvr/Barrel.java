package com.jonasry.stereosvr;

import java.awt.image.BufferedImage;

/**
 * Implementation of (with some modifications):
 *   http://www.tannerhelland.com/4743/simple-algorithm-correcting-lens-distortion
 */
public class Barrel {
	// Computed by setting d/dr(atan(r)/r) = 0
	public static final double STRENGTH = 0.824265949420801;
	public static final double ZOOM = Integer.getInteger("corrections.barrel.zoom", 10) / 10.0d;

	public static BufferedImage applyCorrection(BufferedImage image) {
		return applyCorrection(image, STRENGTH, ZOOM);
	}

	public static BufferedImage applyCorrection(BufferedImage image, double strength, double zoom) {
		final int width = image.getWidth();
		final int height = image.getHeight();
		final int halfWidth = width / 2;
		final int halfHeight = height / 2;
		final double rc = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight) / strength;
		final double rz = zoom * halfHeight / rc;
		final double z = Math.atan(rz) / rz / zoom;

		System.out.println("Applying Barrel Distortion Correction");
		System.out.println("   Using rc = " + String.format("%8.4f", rc));
		System.out.println("   Using  z = " + String.format("%8.4f", z));

		final BufferedImage output = new BufferedImage(width, height, image.getType());
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final Point s = Point
						.from(x, y)
						.translate(-halfWidth, -halfHeight)
						.applyTheta(rc, z)
						.translate(halfWidth, halfHeight);

				if (s.x < width && s.x >= 0 && s.y < height && s.y >= 0) {
					output.setRGB(x, y, image.getRGB((int) s.x, (int) s.y));
				}
			}
		}
		return output;
	}

	public static final class Point {
		public final double x;
		public final double y;
		public Point(double x, double y) { this.x = x; this.y = y; }
		public static Point from(double x, double y) { return new Point(x, y); }
		public Point translate(double dX, double dY) { return new Point(x + dX, y + dY); }
		public Point multiply(double theta) { return new Point(x * theta, y * theta); }
		public Point applyTheta(double rc, double z) { return multiply(theta(rc, z)); }
		public double length() { return Math.sqrt(x * x + y * y); }
		public double theta(double rc, double z) { double r = length() / rc; return r == 0 ? 1 : z * r / Math.atan(r); }
	}
}
