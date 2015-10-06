package com.jonasry.stereosvr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import org.junit.Test;

public class BarrelTest {
	private static final int N = 24;
	private static final int SZ = 20;
	private static final int SIZE = SZ * N * 2;

	@Test
	public void test() throws Exception {
		final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_BINARY);
		final Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, SIZE, SIZE);

		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(SZ));

		for (int y = 0; y <= N; y += 1) {
			for (int x = 0; x <= N; x += 1) {
				if ((x + y) % 2 == 0) {
					graphics.drawRect(x * SZ * 2 + SZ / 2, y * SZ * 2 + SZ / 2, SZ, SZ);
				}
			}
		}

		final BufferedImage corr = Barrel.applyCorrection(image, Barrel.STRENGTH, 0.85);
		Image.write(Image.createStereoImage(corr, corr), "test.png", new FileOutputStream("test.png"));

		graphics.dispose();
	}
}
