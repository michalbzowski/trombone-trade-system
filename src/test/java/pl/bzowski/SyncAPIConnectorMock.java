package pl.bzowski;

import java.io.IOException;
import java.net.UnknownHostException;

import pro.xstore.api.sync.SyncAPIConnector;
import pro.xstore.api.sync.ServerData.ServerEnum;

public class SyncAPIConnectorMock extends SyncAPIConnector {

  public SyncAPIConnectorMock() throws UnknownHostException, IOException {
    super(null);
    //TODO Auto-generated constructor stub
  }

}
