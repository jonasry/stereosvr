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
		final double correctionRadius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight) / strength;
		final double rz = zoom * halfHeight / correctionRadius;
		final double z = zoom * rz / Math.atan(rz);

		System.out.println("Applying Barrel Distortion Correction");
		System.out.println("   Using correctionRadius=" + correctionRadius);
		System.out.println("   Using z=" + z);

		final BufferedImage output = new BufferedImage(width, height, image.getType());
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int dx = x - halfWidth;
				final int dy = y - halfHeight;
				final double distance = Math.sqrt(dx * dx + dy * dy);
				final double r = distance / correctionRadius;
				final double theta = r == 0 ? 1 : Math.atan(r) / r;
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
	
	public static void main(String[] args) throws Exception {
		final BufferedImage image = Image.read(args[0]);
		final BufferedImage result = applyCorrection(image);
		Image.write(result, "result-" + args[0]);
	}
}
