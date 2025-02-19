import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.google.zxing.BarcodeFormat.QR_CODE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

public class ResourceManager {

    public static final String PATH = "src/main/resources/";
    private static final String BOOK_LOG_DEFAULT_NAME = "book_log.dat";
    public static final String QR_CODE_DEFAULT = "qr_code/";
    private static final String IMAGE_FORMAT = ".png";

    private List<Book> books = new ArrayList<>();
    private final Path qrCodePath;
    private final Path bookPath;

    private static class BookNotFoundException extends RuntimeException { }

    public ResourceManager() {
        this(PATH);
    }

    public ResourceManager(String path) {

        qrCodePath = Path.of(path + QR_CODE_DEFAULT);

        try {
            if(!Files.exists(qrCodePath)) Files.createDirectory(qrCodePath);
        } catch (Exception e) {
            System.err.println("Error while verifying Qr Codes " + e.getMessage());
        }

        bookPath = Path.of(path + BOOK_LOG_DEFAULT_NAME);
        Stream<String> bookEntries;

        try {
            if(!Files.exists(bookPath)) Files.createFile(bookPath);
            bookEntries = Files.lines(bookPath);
        } catch (IOException e) {
            System.err.println("Error while reading book filesystem " + e.getMessage());
            books.add(new Book("Error. No Books found.", false, LocalDateTime.MIN, ""));
            return;
        }

        books = bookEntries
                .map(line -> line.split(";"))
                .filter(elements -> elements.length == 4)
                .map(elements -> new Book(elements[0], Boolean.parseBoolean(elements[1]), LocalDateTime.parse(elements[2].trim()), elements[3]))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("\u001B[42m" + "Some text" + "\u001B[0m");
        System.out.println("More Text");

            Scanner s = new Scanner(System.in);

            switch(s.nextLine()) {

                Float f = new Float(.5);
                f.
                case "hello"-> System.out.println("test");
                case "dostuff"-> System.out.println("test2");
                default -> System.out.println("kys");


            }


    }
    }

    public void safeEntries() {

        var sb = new StringBuilder();

        for (var book : books) {

            sb.append(book.name);
            sb.append(";");
            sb.append(book.isAvailable);
            sb.append(";");
            sb.append(book.rentedLastTime);
            sb.append(";");
            sb.append(book.lastRenter);
            sb.append(System.lineSeparator());

        }

        try {
            System.out.println(sb);
            Files.writeString(bookPath, sb, UTF_8, TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error. No file found to write to.");
        }
    }

    public synchronized void rent(String title, String renter) throws BookNotFoundException {
        Book wantedBook = books.stream().filter(elem -> elem.name.equals(title)).findFirst().orElseThrow(BookNotFoundException::new);

        wantedBook.isAvailable = false;
        wantedBook.rentedLastTime = LocalDateTime.now();
        wantedBook.lastRenter = renter;

        safeEntries();
    }

    public synchronized void giveBack(String title) throws BookNotFoundException {
        Book givenBook = books.stream().filter(elem -> elem.name.equals(title)).findFirst().orElseThrow(BookNotFoundException::new);

        givenBook.isAvailable = true;

        safeEntries();
    }

    public synchronized boolean addBook(String title) {

        if(findBookByTitle(title) != null) return false;

        var now = LocalDateTime.now();
        try {
            Files.writeString(bookPath, title + ";"
                    + true + ";"
                    + now
                    + ";Kein Ausleiher" + System.lineSeparator(), UTF_8, APPEND);
        } catch (Exception e) {
            return false;
        }

        return books.add(new Book(title, true, now, "Kein Ausleiher"));
    }


    public synchronized Book removeBook(String title) {
        var toRemove = books.stream().filter(elem -> elem.name.equals(title)).findFirst().orElse(null);


        if (toRemove == null) return null;
        books.remove(toRemove);

        try {
            Files.deleteIfExists(Path.of(qrCodePath + "/" + title + IMAGE_FORMAT));
        } catch (Exception e) {
            System.err.println("Error while trying to delete Qr Code " + e.getMessage());
        }

        safeEntries();
        return toRemove;
    }

    public byte[] getQrCode(String title) {

        if(findBookByTitle(title) == null) return null;

        Path qrCode = Path.of(qrCodePath + "/" + title + IMAGE_FORMAT);

        if(!Files.exists(qrCode)) addQrBook(title);

        try {
            return Files.readAllBytes(qrCode);
        } catch (Exception e) {
            System.err.println("Error while reading Qr Code " + e.getMessage());
            return null;
        }

    }

    public Book findBookByTitle(String title) {
        return books.stream().filter(elem -> elem.name.equals(title)).findFirst().orElse(null);
    }

    public List<Book> getBooks() {
        return books;
    }

    @Override
    public String toString() {
        return "books=" + books.toString();
    }

    public boolean addQrBook(String title) {

        String path = qrCodePath + "/" + title + IMAGE_FORMAT;
        String url = "http://" + Server.IP + "/registration/" + URLEncoder.encode(title, UTF_8);

        Map<EncodeHintType, ErrorCorrectionLevel> mapping = new HashMap<>();
        mapping.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        try {
            generateQR(url, path, mapping, 200, 200);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void generateQR(String data, String path, Map map, int h, int w) throws WriterException, IOException {
        var bitMatrix = new MultiFormatWriter().encode(new String(data.getBytes(UTF_8), UTF_8), QR_CODE, w, h, map);
        MatrixToImageWriter.writeToPath(bitMatrix, path.substring(path.lastIndexOf('.') + 1), Path.of(path));
    }


}
