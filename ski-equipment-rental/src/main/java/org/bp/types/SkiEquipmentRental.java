package org.bp.types;

import java.math.BigDecimal;

public class SkiEquipmentRental {
    private SkiEquipment skiEquipment;
    private Person person;

    public SkiEquipmentRental() {
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public SkiEquipment getSkiEquipment() {
        return skiEquipment;
    }

    public void setSkiEquipment(SkiEquipment skiEquipment) {
        this.skiEquipment = skiEquipment;
    }
}
