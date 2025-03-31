package org.bp.skitripui;

public class BuySkiPassRequest {
  private Person person;

  private SkiPass skiPass;

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public SkiPass getSkiPass() {
    return skiPass;
  }

  public void setSkiPass(SkiPass skiPass) {
    this.skiPass = skiPass;
  }
}