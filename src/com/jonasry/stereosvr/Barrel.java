package com.jonasry.stereosvr;

import java.awt.image.BufferedImage;

/*
	http://www.tannerhelland.com/4743/simple-algorithm-correcting-lens-distortion/

	input:
	    strength as floating point >= 0.  0 = no change, high numbers equal stronger correction.
	    zoom as floating point >= 1.  (1 = no change in zoom)
	
	algorithm:
	    set halfWidth = imageWidth / 2
	    set halfHeight = imageHeight / 2
	    
	    if strength = 0 then strength = 0.00001
	    set correctionRadius = squareroot(imageWidth ^ 2 + imageHeight ^ 2) / strength
	
	    for each pixel (x,y) in destinationImage
	        set newX = x - halfWidth
	        set newY = y - halfHeight
	
	        set distance = squareroot(newX ^ 2 + newY ^ 2)
	        set r = distance / correctionRadius
	        
	        if r = 0 then
	            set theta = 1
	        else
	            set theta = arctangent(r) / r
	
	        set sourceX = halfWidth + theta * newX * zoom
	        set sourceY = halfHeight + theta * newY * zoom
	
	        set color of pixel (x, y) to color of source image pixel at (sourceX, sourceY)
 */
public class Barrel {
	public static final boolean ENABLED = Boolean.getBoolean("corrections.barrel");
	public static final double STRENGTH = Integer.getInteger("corrections.barrel.strength", 0);
	public static final double ZOOM = 1;

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
				final int sx = (int) Math.round(halfWidth + theta * dx * ZOOM);
				final int sy = (int) Math.round(halfHeight + theta * dy * ZOOM);
				if (sx < width && sx >= 0 && sy < height && sy >= 0) {
					output.setRGB(x, y, image.getRGB(sx, sy));
				}
			}
		}
		return output;
	}
}
