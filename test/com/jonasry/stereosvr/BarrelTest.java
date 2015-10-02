package com.jonasry.stereosvr;

import java.awt.image.BufferedImage;

import org.junit.Test;

public class BarrelTest {

	@Test
	public void test() {
		System.setProperty("corrections.barrel", "true");
		System.setProperty("corrections.barrel.strength", "20");
		System.setProperty("corrections.barrel.zoom", "13");
		BufferedImage image = new BufferedImage(960, 1080, BufferedImage.TYPE_INT_RGB);
		Barrel.applyCorrection(image);
	}
}
