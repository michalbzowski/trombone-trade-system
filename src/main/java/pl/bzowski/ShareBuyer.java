package pl.bzowski;

public class ShareBuyer {

    private final ShareRepository shareRepository;
    private final MyLogger logger;

    public ShareBuyer(ShareRepository shareRepository, MyLogger logger, String userName, String password, String userId) {
        this.shareRepository = shareRepository;
        this.logger = logger;
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