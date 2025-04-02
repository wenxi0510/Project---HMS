public interface Replenish{
    void updateLowStockAlert();
    boolean viewReplenishmentRequests();
    void newReplenishmentRequest(String medicationID, int replenishAmount);
    void fulfillReplenishmentRequest(String medicationID);
}