package com.jared.bountyhunter;

import java.util.UUID;

public class BountyData {
    private UUID targetUUID;
    private UUID placedByUUID;
    private String placedByName;
    private CurrencyType currency;
    private int amount;
    
    public enum CurrencyType {
        DIAMOND,
        EMERALD,
        NETHERITE
    }
    
    public BountyData(UUID targetUUID, UUID placedByUUID, String placedByName, CurrencyType currency, int amount) {
        this.targetUUID = targetUUID;
        this.placedByUUID = placedByUUID;
        this.placedByName = placedByName;
        this.currency = currency;
        this.amount = amount;
    }
    
    public UUID getTargetUUID() {
        return targetUUID;
    }
    
    public UUID getPlacedByUUID() {
        return placedByUUID;
    }
    
    public String getPlacedBy() {
        return placedByName;
    }
    
    public CurrencyType getCurrency() {
        return currency;
    }
    
    public int getAmount() {
        return amount;
    }
}
