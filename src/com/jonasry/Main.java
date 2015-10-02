package com.jonasry;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;

public class Main {
    private static final ExecutorService service = Executors.newFixedThreadPool(2);
    private static final double ROT = 270.0;
    private static final int VERTICAL_OFFSET = 22;
    private static final String SIZE = "1920x1080";
    private static final String DEVICE_PREFIX = "V4L2";
    private static final String DEVICE_PATH = "/dev/video";
    private static final String RIGHT = "0";
    private static final String LEFT = "1";

    public static void main(String[] args) throws Exception {
        createJettyServer();
    }

    private static void createJettyServer() throws Exception {
        final Server server = new Server(8080);
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setResourceBase(".");

        final ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(InternalServer.class, "/server/*");

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, handler, new DefaultHandler() });
        server.setHandler(handlers);

        server.start();
        System.out.println("Serving on port 8080");

        server.join();
    }

    private static Future<BufferedImage> capture(final String description, final String path, final int verticalCorrection) throws Exception {
        final String device = String.format("%s:%s", DEVICE_PREFIX, path);

        final Callable<BufferedImage> cmd = new Callable<BufferedImage>() {
            @Override
            public BufferedImage call() throws Exception {
                final long start = System.nanoTime();

                final ProcessBuilder b = new ProcessBuilder();
                //b.command("fswebcam", "--png", "0", "-d", device, "-r", SIZE, "--no-banner", "-");
                b.command("fswebcam", "--png", "0", "-d", device, "-r", SIZE, "--rotate", String.valueOf(ROT), "--crop", "960x1080,60x" + (420 + verticalCorrection), "--no-banner", "-");
                System.out.println(description + ": " + flatten(b.command(), " "));

                final Process p = b.start();
                final BufferedImage imageIn = ImageIO.read(p.getInputStream());
                p.waitFor();

                final BufferedImage outputImage = imageIn; //getOutputImage(imageIn, verticalCorrection);

                final long duration = System.nanoTime() - start;
                System.out.println(description + ": " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");

                return outputImage;
            }
        };

        return service.submit(cmd);
    }

    private static BufferedImage getStereoImage(final BufferedImage left, final BufferedImage right) throws Exception {
        final BufferedImage imageOutput = new BufferedImage(left.getWidth() * 2, left.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = imageOutput.createGraphics();
        g.drawImage(left, 0, 0, null);
        g.drawImage(right, left.getWidth(), 0, null);
        g.dispose();
        return imageOutput;
    }

    private static void saveImage(BufferedImage imageOutput, String imageName, String format) throws IOException {
        System.out.println("Writing " + format.toUpperCase() + " to " + imageName + ". W:" + imageOutput.getWidth() + " H:" + imageOutput.getHeight());
        final long start = System.nanoTime();
        ImageIO.write(imageOutput, format, new File(imageName));
        final long duration = System.nanoTime() - start;
        System.out.println("Saved " + imageName + " in " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");
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

    @SuppressWarnings("serial")
	public static class InternalServer extends HttpServlet {
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final String path = req.getPathInfo();
            if (path == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");

            } else {
                final long start = System.nanoTime();
                try {
                    if (path.endsWith("stereo")) {
                        final Future<BufferedImage> leftFuture = Main.capture("left", DEVICE_PATH + LEFT, 0);
                        final Future<BufferedImage> rightFuture = Main.capture("right", DEVICE_PATH + RIGHT, VERTICAL_OFFSET);
                        final BufferedImage stereoImage = getStereoImage(leftFuture.get(), rightFuture.get());
                        saveImage(stereoImage, "out.png", "png");
                        resp.sendRedirect("/out.png");

                    } else {
                        resp.sendRedirect("/index.html");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
                final long duration = System.nanoTime() - start;
                System.out.println(path + ": " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms.");
            }
        }
    }
}
