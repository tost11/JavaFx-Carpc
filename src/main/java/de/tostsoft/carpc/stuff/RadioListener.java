package de.tostsoft.carpc.stuff;

public interface RadioListener {
    void handleNewOne(RadioChecker.RadioInfo radioInfo);
    void handleUpdate(RadioChecker.RadioInfo radioInfo);
    void handleRemove(String sId);
}
