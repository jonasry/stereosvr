package com.jonasry.stereosvr;

import java.awt.image.BufferedImage;

/**
 * Implementation of with some modifications:
 *   http://www.tannerhelland.com/4743/simple-algorithm-correcting-lens-distortion
 */
public class Barrel {
	public static final boolean ENABLED = Boolean.getBoolean("corrections.barrel");
	public static final double STRENGTH = Integer.getInteger("corrections.barrel.strength", 0) / 10;
	public static final double ZOOM = Integer.getInteger("corrections.barrel.zoom", 10) / 10.0d;

	public static BufferedImage applyCorrection(BufferedImage image) {
		if (!ENABLED) {
			return image;
		}
		final int width = image.getWidth();
		final int height = image.getHeight();
		final int halfWidth = width / 2;
		final int halfHeight = height / 2;
		final double strength = Math.max(STRENGTH, 0.00001);
		final double zoom = Math.max(ZOOM, 0.00001);
		final double correctionRadius = Math.sqrt(width * width + height * height) / strength;

		System.out.println("Applying Barrel Distortion Correction using correctionRadius=" + correctionRadius);

		final BufferedImage output = new BufferedImage(width, height, image.getType());
		for (int y = 0; y < width; y++) {
			for (int x = 0; x < width; x++) {
				final int dx = x - halfWidth;
				final int dy = y - halfHeight;
				final double distance = Math.sqrt(dx * dx + dy * dy);
				final double r = distance / correctionRadius;
				double theta = 1;
				if (r != 0) {
					theta = Math.atan(r) / r;
				}
				final int sx = (int) (halfWidth + dx / theta / zoom);
				final int sy = (int) (halfHeight + dy / theta / zoom);
				if (sx < width && sx >= 0 && sy < height && sy >= 0) {
					output.setRGB(x, y, image.getRGB(sx, sy));
				}
			}
		}
		return output;
	}
}
