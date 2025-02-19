
import com.beust.jcommander.converters.URLConverter;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;


import static java.nio.charset.StandardCharsets.UTF_8;

public class Server {

    public static final String IP = "192.168.178.25";
    public static final int PORT = 80;
    public HttpServer httpServer;
    public ResourceManager resourceManager;


    public Server() throws IOException {
        this(PORT);
    }

    public Server(int port) throws IOException {

        resourceManager = new ResourceManager();
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(port), 0);
        // TODO erst nach dem auftrennen der attribute decoden da sonst bei fragezeiche in texten nicht richtig aufgetrennt wird
        // TODO umschreiben mit HTTPResponse usw.
        // TODO Aktualisieren beim löschen ermöglichen
        // TODO sout interface für konsole
        /**
         * @HOME
         */
        httpServer.createContext("/", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String home = HtmlGenerator.home(resourceManager.getBooks());

                if (exchange.getRequestURI().toString().matches("/\\?name=.+")) {
                    String url = URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8);
                    String title = "";
                    if (url.length() > 7) title = url.substring(7).replaceAll("\\?.*", "");
                    System.out.println("title: " + title);
                    var book = resourceManager.removeBook(title);

                    if (book == null) System.err.println("Book could not be removed");

                    exchange.sendResponseHeaders(307, home.length());
                } else
                    exchange.sendResponseHeaders(200, home.length());

                OutputStream os = exchange.getResponseBody();
                os.write(home.getBytes(UTF_8));
                os.close();

            } else if (exchange.getRequestMethod().equals("POST")) {

                var br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                String body = URLDecoder.decode(br.readLine(), UTF_8);
                System.out.println(body);

                if (body.length() < 1) System.err.println("Body is empty");
                if (!body.matches("name=.+")) System.err.println("New Book does not have the right format");
                else resourceManager.addBook(body.substring(5));

                String home = HtmlGenerator.home(resourceManager.getBooks());
                exchange.sendResponseHeaders(303, home.length());
                OutputStream os = exchange.getResponseBody();
                os.write(home.getBytes(UTF_8));
                os.close();
            }
        });


        /**
         * @RETURN
         */
        httpServer.createContext("/return", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String returnedBook = exchange.getRequestURI().toString()
                        .substring(8)
                        .replaceAll("\\?", "");

                resourceManager.giveBack(returnedBook);
                String returned = HtmlGenerator.returned();
                exchange.sendResponseHeaders(200, returned.length());
                OutputStream os = exchange.getResponseBody();
                os.write(returned.getBytes(UTF_8));
                os.close();
            }
        });


        /**
         * @RENT
         */
        httpServer.createContext("/rent", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String site = "";
                String uri = URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8);

                String rentedBook = "";
                String renter = uri.replaceAll(".*\\?name=", "");

                if (uri.length() > 6) rentedBook = uri.substring(6).replaceAll("\\?.*", "");

                if (rentedBook.length() < 1)
                    site = HtmlGenerator.registrationHelp();
                else if (renter.length() < 1)
                    site = HtmlGenerator.noRenter();
                else
                    try {
                        resourceManager.rent(rentedBook, renter);
                        site = HtmlGenerator.rent();
                    } catch (Exception e) {
                        site = HtmlGenerator.bookNotFound();
                    }

                exchange.sendResponseHeaders(200, site.length());
                OutputStream os = exchange.getResponseBody();
                os.write(site.getBytes(UTF_8));
                os.close();
            }
        });


        /**
         * @REGISTRATION
         */
        httpServer.createContext("/registration", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String site = "";
                String url = URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8);
                String title = "";

                if (url.length() > 14) title = url.substring(14);
                var book = resourceManager.findBookByTitle(title);

                if (title.length() < 1)
                    site = HtmlGenerator.registrationHelp();
                else if (book == null)
                    site = HtmlGenerator.bookNotFound();
                else
                    site = HtmlGenerator.registration(book.isAvailable).replaceAll("\\*", title);

                exchange.sendResponseHeaders(200, site.length());
                OutputStream os = exchange.getResponseBody();
                os.write(site.getBytes(UTF_8));
                os.close();
            }
        });


        /**
         * @BOOKS
         */
        httpServer.createContext("/book", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String site = "";
                String url = URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8);
                String title = "";

                if (url.length() > 6) title = url.substring(6).replaceAll("\\?.*", "");

                var book = resourceManager.findBookByTitle(title);

                if (book == null || title.isEmpty()) site = HtmlGenerator.bookNotFound();

                else site = HtmlGenerator.book(title);

                exchange.sendResponseHeaders(200, site.length());
                OutputStream os = exchange.getResponseBody();
                os.write(site.getBytes(UTF_8));
                os.close();
            }
        });


        /**
         * @QR_CODES
         */
        httpServer.createContext("/qr_code", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String url = URLDecoder.decode(exchange.getRequestURI().toString(), UTF_8);
                String name = "";

                if (url.length() > 9) name = url.substring(9).replaceAll("\\?.*", "");

                byte[] img = resourceManager.getQrCode(name);

                if (img == null)
                    exchange.sendResponseHeaders(404, 0);
                else {
                    exchange.sendResponseHeaders(200, img.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(img);
                    os.close();
                }
            }
        });


        /**
         * @STYLES
         */
        httpServer.createContext("/styles.css", exchange -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String styles = HtmlGenerator.styles();

                exchange.sendResponseHeaders(200, styles.length());
                OutputStream os = exchange.getResponseBody();
                os.write(styles.getBytes(UTF_8));
                os.close();
            }
        });

        httpServer.createContext("/poweroff.svg", exchange -> {
           if(exchange.getRequestMethod().equals("GET")) {

               byte[] svg = Files.readAllBytes(Path.of("src/main/resources/poweroff.svg"));
                exchange.sendResponseHeaders(200, svg.length);
                OutputStream os = exchange.getResponseBody();
                os.write(svg);
                os.close();
           }
        });


        httpServer.createContext("/exit", exchange -> {
            String site = "Shutting down";
            exchange.sendResponseHeaders(200, site.length());
            OutputStream os = exchange.getResponseBody();
            os.write(site.getBytes(UTF_8));
            os.close();
            System.exit(0);
        });
    }

    public void start() {
        httpServer.start();
    }

    public static void main(String[] args) throws IOException {
        new Server().start();

        // Connect to server locally when starting the application
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create("http://localhost/"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
