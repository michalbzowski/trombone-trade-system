package pl.bzowski;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        ShareRepository shareRepository = new ShareRepository();
        MyLogger logger = new MyLogger();
        ShareBuyer shareBuyer = new ShareBuyer(shareRepository, logger);
    }
}
