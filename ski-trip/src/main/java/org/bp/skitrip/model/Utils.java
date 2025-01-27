package org.bp.skitrip.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static org.bp.BuySkiPassRequest prepareSkiPassPurchaseRequest(TripBookingRequest tripBookingRequest) {
        org.bp.BuySkiPassRequest buySkiPassRequest =  new org.bp.BuySkiPassRequest();
        buySkiPassRequest.setSkiPass(tripBookingRequest.getSkiPass());

        org.bp.Person person =  new org.bp.Person();
        if (tripBookingRequest.getPerson()!=null) {
            person.setFirstName(tripBookingRequest.getPerson().getFirstName());
            person.setLastName(tripBookingRequest.getPerson().getLastName());
        }

        buySkiPassRequest.setPerson(person );

        return buySkiPassRequest;
    }

    public static org.bp.CancelPurchaseRequest prepareSkiPassCancelRequest(org.bp.BookingInfo bookingInfo) {
        org.bp.CancelPurchaseRequest cancelPurchaseRequest = new org.bp.CancelPurchaseRequest();
        cancelPurchaseRequest.setBookingId(bookingInfo.getId());
        return cancelPurchaseRequest;
    }

    public static org.bp.skiequipment.CancelBooking prepareSkiEquipmentCancelRequest(org.bp.skiequipment.RentSkiEquipmentResponse rentSkiEquipmentResponse) {
        org.bp.skiequipment.CancelBooking cancelBookingRequest = new org.bp.skiequipment.CancelBooking();
        cancelBookingRequest.setArg0(rentSkiEquipmentResponse.getReturn().getId());
        return cancelBookingRequest;
    }

    public static org.bp.skiequipment.RentSkiEquipment prepareSkiEquipmentRentalRequest(TripBookingRequest tripBookingRequest) {
        org.bp.skiequipment.RentSkiEquipment rentSkiEquipment =  new org.bp.skiequipment.RentSkiEquipment();
        org.bp.skiequipment.SkiEquipmentRental skiEquipmentRental =  new org.bp.skiequipment.SkiEquipmentRental();
        skiEquipmentRental.setSkiEquipment(tripBookingRequest.getSkiEquipment());
        skiEquipmentRental.setPerson(tripBookingRequest.getPerson());

        rentSkiEquipment.setArg0(skiEquipmentRental);

        return rentSkiEquipment;
    }

    public static org.bp.payment.PaymentRequest preparePaymentRequest(TripBookingRequest tripBookingRequest) {
        org.bp.payment.PaymentRequest paymentRequest = new org.bp.payment.PaymentRequest();
        paymentRequest.setPaymentCard(tripBookingRequest.getPaymentCard());
        org.bp.payment.Amount amount = new org.bp.payment.Amount();
        amount.setValue(new BigDecimal(0));
        amount.setCurrency("PLN");
        paymentRequest.setAmount(amount);
        return paymentRequest;
    }

    public static org.bp.payment.PaymentResponse createPaymentResponse() {
        org.bp.payment.PaymentResponse paymentResponse = new org.bp.payment.PaymentResponse();
        paymentResponse.setTransactionId(generateUniqueId());
        paymentResponse.setTransactionDate(OffsetDateTime.now());
        return paymentResponse;
    }

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public static int generateUniqueId() {
        return idCounter.incrementAndGet();
    }
}