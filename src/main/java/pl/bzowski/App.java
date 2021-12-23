package pl.bzowski;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String userName = args[0];
        String password = args[1];
        String userId = args[2];

        User user = new User(userId, password, userName);

        ShareRepository shareRepository = new ShareRepository();
        MyLogger logger = new MyLogger();
        ShareBuyer shareBuyer = new ShareBuyer(shareRepository, logger, user);
    }
}
