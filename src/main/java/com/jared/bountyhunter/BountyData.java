package com.jared.bountyhunter;

import java.util.UUID;

public class BountyData {
    private UUID targetUUID;
    private UUID placedByUUID;
    private String placedByName;
    private double amount;
    private UUID hunterUUID;
    private String hunterName;
    private boolean isAccepted;
    
    public BountyData(UUID targetUUID, UUID placedByUUID, String placedByName, double amount) {
        this.targetUUID = targetUUID;
        this.placedByUUID = placedByUUID;
        this.placedByName = placedByName;
        this.amount = amount;
        this.hunterUUID = null;
        this.hunterName = null;
        this.isAccepted = false;
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
    
    public double getAmount() {
        return amount;
    }
    
    public UUID getHunterUUID() {
        return hunterUUID;
    }
    
    public String getHunterName() {
        return hunterName;
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }
    
    public void setHunter(UUID hunterUUID, String hunterName) {
        this.hunterUUID = hunterUUID;
        this.hunterName = hunterName;
        this.isAccepted = true;
    }
    
    public void clearHunter() {
        this.hunterUUID = null;
        this.hunterName = null;
        this.isAccepted = false;
    }
}
