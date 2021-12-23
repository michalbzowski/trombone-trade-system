package pl.bzowski;

public class ShareBuyer {

    private ShareRepository shareRepository;
    private MyLogger logger;

    public void sell() {
        shareRepository.saveSellOrder(new SellOrder());
        logger.log("sell");
    }

    public void buy() {
        shareRepository.saveBuyOrder(new BuyOrder());
        logger.log("buy");
    }

    public void setShareRepository(ShareRepository shareRepository) {
        this.shareRepository = shareRepository;
    }

    public void setLogger(MyLogger logger)  {
        this.logger = logger;
    }

}
