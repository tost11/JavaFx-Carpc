package de.tostsoft.carpc.stuff;

/**
 * Created by tost-holz on 17.03.2019.
 */
public interface UpdateWLanStatusListener {
    public void handleNewStatus(WLan.ConnectionStatus connectionStatus);
}
