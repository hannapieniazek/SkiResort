package org.bp.skitripui;

public class SkiEquipmentRental {
  private Person person;

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

  private SkiEquipment skiEquipment;
}