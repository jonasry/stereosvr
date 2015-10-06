package com.jonasry.stereosvr;

import static com.jonasry.stereosvr.Capture.captureAndSaveImage;

import java.io.IOException;

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
	private static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		createJettyServer();
	}

	private static void createJettyServer() throws Exception {
		final Server server = new Server(PORT);
		final ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(".");

		final ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(ControllerServlet.class, "/server/*");

		final HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, handler, new DefaultHandler() });
		server.setHandler(handlers);

		server.start();
		System.out.println("port: " + PORT);
		server.join();
	}

	@SuppressWarnings("serial")
	public static class ControllerServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			final String path = req.getPathInfo();
			if (path == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");

			} else {
				try {
					if (path.endsWith("stereo")) {
						resp.setContentType("image/png");
						captureAndSaveImage(resp.getOutputStream());

					} else {
						resp.sendRedirect("/index.html");
					}

				} catch (Exception e) {
					e.printStackTrace();
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				}
			}
		}
	}
}
