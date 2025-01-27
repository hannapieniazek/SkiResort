package org.bp.skitrip.model;

public class TripBookingRequest {
	private org.bp.skiequipment.Person person;
	private org.bp.SkiPass skiPass;
	private org.bp.skiequipment.SkiEquipment skiEquipment;
	private org.bp.payment.PaymentCard paymentCard;

	public org.bp.skiequipment.Person getPerson() {
		return person;
	}
	public void setPerson(org.bp.skiequipment.Person person) {
		this.person = person;
	}
	public org.bp.SkiPass getSkiPass() {
		return skiPass;
	}
	public void setSkiPass(org.bp.SkiPass skiPass) {
		this.skiPass = skiPass;
	}
	public org.bp.skiequipment.SkiEquipment getSkiEquipment() {
		return skiEquipment;
	}
	public void setSkiEquipment(org.bp.skiequipment.SkiEquipment skiEquipment) {
		this.skiEquipment = skiEquipment;
	}
	public org.bp.payment.PaymentCard getPaymentCard() {
		return paymentCard;
	}
	public void setPaymentCard(org.bp.payment.PaymentCard paymentCard) {
		this.paymentCard = paymentCard;
	}


}