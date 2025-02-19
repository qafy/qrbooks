
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HtmlGenerator {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Path HOME_PATH = Path.of("src/main/resources/home.html");
    private static final Path REGISTRATION_PATH = Path.of("src/main/resources/registration.html");
    private static final Path RENT_PATH = Path.of("src/main/resources/rent.html");
    private static final Path RETURNED_PATH = Path.of("src/main/resources/return.html");
    private static final Path REGISTRATION_HELP = Path.of("src/main/resources/registrationHelp.html");
    private static final Path BOOK_NOT_FOUND = Path.of("src/main/resources/bookNotFound.html");
    private static final Path NO_RENTER = Path.of("src/main/resources/NoRenter.html");
    private static final Path STYLES = Path.of("src/main/resources/styles.css");
    private static final Path BOOK = Path.of("src/main/resources/book.html");

    private static final String[] home, registration;
    private static final String rent, returned, bookNotFound, registrationHelp, noRenter, styles, book;

    private HtmlGenerator() {
    }

    static {

        String[] reg, hom;
        String ren, ret, not, hel, nor, sty, boo;

        try {
            reg = replaceÄÖÜ(Files.readString(REGISTRATION_PATH)).split("<!--Split-->");
            hom = replaceÄÖÜ(Files.readString(HOME_PATH)).split("<!--Split-->");
            ren = replaceÄÖÜ(Files.readString(RENT_PATH));
            ret = replaceÄÖÜ(Files.readString(RETURNED_PATH));
            not = replaceÄÖÜ(Files.readString(BOOK_NOT_FOUND));
            hel = replaceÄÖÜ(Files.readString(REGISTRATION_HELP));
            nor = replaceÄÖÜ(Files.readString(NO_RENTER));
            sty = replaceÄÖÜ(Files.readString(STYLES));
            boo = replaceÄÖÜ(Files.readString(BOOK));

        } catch (IOException e) {
            System.err.println("Could not Find HTML Files" + e.getMessage());
            reg = hom = null;
            ren = ret = not = hel = nor = sty = boo = "";
        }

        registration = reg;
        home = hom;
        rent = ren;
        returned = ret;
        bookNotFound = not;
        registrationHelp = hel;
        noRenter = nor;
        styles = sty;
        book = boo;
    }


    public static String home() {
        return home(null);
    }

    public static String home(List<Book> books) {

        var sb = new StringBuilder();

        sb.append(home[0]);

        if (books != null) {

            for (var book : books) {

                sb
                        .append("<tr onclick=\"window.location='/book/")
                        .append(book.name)
                        .append("';\">")

                        .append("<td>")
                        .append(book.name)
                        .append("</td>")

                        .append("<td>")
                        .append(book.isAvailable ? "Ja" : "Nein")
                        .append("</td>")

                        .append("<td>")
                        .append(book.rentedLastTime.format(DATE_PATTERN).toString())
                        .append("</td>")

                        .append("<td>")
                        .append(book.lastRenter)
                        .append("</td>")

                        .append("</tr>");
            }
        }

        sb.append(home[1]);

        return replaceÄÖÜ(sb.toString());
    }

    public static String registration(boolean bookAvailable) {

        var sb = new StringBuilder();

        sb.append(registration[0]);

        if (!bookAvailable) sb.append(registration[1]);
        sb.append(registration[2]);

        return sb.toString();
    }

    private static String replaceÄÖÜ(String toReplace) {
        return toReplace
                .replaceAll("ü", "&uuml")
                .replaceAll("Ü", "&Uuml")
                .replaceAll("ä", "&auml")
                .replaceAll("Ä", "&Auml")
                .replaceAll("ö", "&ouml")
                .replaceAll("Ö", "&Ouml");
    }

    public static String book(String title) {
        return book.replaceAll("BOOK", title);
    }
    public static String rent() {
        return rent;
    }

    public static String returned() {
        return returned;
    }

    public static String bookNotFound() {
        return bookNotFound;
    }

    public static String registrationHelp() {
        return registrationHelp;
    }

    public static String noRenter() {
        return noRenter;
    }

    public static String styles() {
        return styles;
    }
}
