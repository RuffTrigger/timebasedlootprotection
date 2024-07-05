package org.rufftrigger.timebasedlootprotection;

import java.util.UUID;

public class ItemData {
    private final UUID itemUUID;
    private final double dropLocationX;
    private final double dropLocationY;
    private final double dropLocationZ;
    private final UUID worldUUID;
    private final long expirationTime;

    public ItemData(UUID itemUUID, double dropLocationX, double dropLocationY, double dropLocationZ, UUID worldUUID, long expirationTime) {
        this.itemUUID = itemUUID;
        this.dropLocationX = dropLocationX;
        this.dropLocationY = dropLocationY;
        this.dropLocationZ = dropLocationZ;
        this.worldUUID = worldUUID;
        this.expirationTime = expirationTime;
    }

    public UUID getItemUUID() {
        return itemUUID;
    }

    public double getDropLocationX() {
        return dropLocationX;
    }

    public double getDropLocationY() {
        return dropLocationY;
    }

    public double getDropLocationZ() {
        return dropLocationZ;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
