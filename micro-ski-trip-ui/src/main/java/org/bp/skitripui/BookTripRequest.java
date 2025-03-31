package org.bp.skitripui;

import org.bp.skitripui.PaymentCard;
import org.bp.skitripui.Person;
import org.bp.skitripui.SkiEquipment;
import org.bp.skitripui.SkiPass;

public class BookTripRequest {
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

  public SkiEquipment getSkiEquipment() {
    return skiEquipment;
  }

  public void setSkiEquipment(SkiEquipment skiEquipment) {
    this.skiEquipment = skiEquipment;
  }

  public PaymentCard getPaymentCard() {
    return paymentCard;
  }

  public void setPaymentCard(PaymentCard paymentCard) {
    this.paymentCard = paymentCard;
  }

  private Person person;
  private SkiPass skiPass;
  private SkiEquipment skiEquipment;
  private PaymentCard paymentCard;
}