package com.jonasry.stereosvr;

import java.awt.image.BufferedImage;

/**
 * Implementation of (with some modifications):
 *   http://www.tannerhelland.com/4743/simple-algorithm-correcting-lens-distortion
 */
public class Barrel {
	public static final boolean ENABLED = Boolean.getBoolean("corrections.barrel");
	public static final double STRENGTH = Integer.getInteger("corrections.barrel.strength", 0) / 10d;
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
		final double correctionRadius = Math.sqrt(width * width + height * height) / strength;
		final double n = ZOOM;
		final double z = n * n * halfHeight / correctionRadius / Math.atan(n * halfHeight / correctionRadius);

		System.out.println("Applying Barrel Distortion Correction");
		System.out.println("   Using correctionRadius=" + correctionRadius);
		System.out.println("   Using z=" + z);

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
				final double tx = dx / theta / z;
				final double ty = dy / theta / z;
				final int sx = (int) (halfWidth + tx);
				final int sy = (int) (halfHeight + ty);
				if (sx < width && sx >= 0 && sy < height && sy >= 0) {
					output.setRGB(x, y, image.getRGB(sx, sy));
				}
			}
		}
		return output;
	}
}
