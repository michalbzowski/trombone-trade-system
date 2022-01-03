package pl.bzowski.trader;

public class ShareBuyer {

    private final ShareRepository shareRepository;
    private final MyLogger logger;
    private final User user;

    public ShareBuyer(ShareRepository shareRepository, MyLogger logger, User user) {
        this.shareRepository = shareRepository;
        this.logger = logger;
        this.user = user;
    }

    public void sell() {
        shareRepository.saveSellOrder(new SellOrder());
        logger.log("sell");
    }

    public void buy() {
        shareRepository.saveBuyOrder(new BuyOrder());
        logger.log("buy");
    }
}