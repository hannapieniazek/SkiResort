package org.bp.types;

import java.math.BigDecimal;

public class SkiEquipment {
    private String equipmentType;
    private String rentalDuration;
    private BigDecimal price;

    // Constructor
    public SkiEquipment(String equipmentType, String rentalDuration, BigDecimal price) {
        this.equipmentType = equipmentType;
        this.rentalDuration = rentalDuration;
        this.price = price;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getRentalDuration() {
        return rentalDuration;
    }

    public void setRentalDuration(String rentalDuration) {
        this.rentalDuration = rentalDuration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
