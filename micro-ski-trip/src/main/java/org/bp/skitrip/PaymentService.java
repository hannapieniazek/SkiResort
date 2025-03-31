package org.bp.skitrip;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.bp.skitrip.model.BookTripRequest;
import org.bp.skitrip.model.BookingInfo;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private HashMap<String, PaymentData> payments;

    @PostConstruct
    void init() {
        payments=new HashMap<>();
    }

    public static class PaymentData {
        BookTripRequest bookTripRequest;
        BookingInfo skiPassPurchaseInfo;
        BookingInfo skiEquipmentRentalInfo;
        public boolean isReady() {
            return bookTripRequest!=null && skiPassPurchaseInfo!=null && skiEquipmentRentalInfo!=null;
        }
    }

    public synchronized boolean addBookTripRequest(String bookTripId, BookTripRequest bookTripRequest) {
        PaymentData paymentData = getPaymentData(bookTripId);
        paymentData.bookTripRequest = bookTripRequest;
        return paymentData.isReady();
    }


    public synchronized boolean addBookingInfo(String bookTripId, BookingInfo bookingInfo, String serviceType) {
        PaymentData paymentData = getPaymentData(bookTripId);
        if (serviceType.equals("skiEquipment"))
            paymentData.skiEquipmentRentalInfo = bookingInfo;
        else
            paymentData.skiPassPurchaseInfo = bookingInfo;
        return paymentData.isReady();
    }


    public synchronized PaymentData getPaymentData(String bookTripId) {
        PaymentData paymentData = payments.get(bookTripId);
        if (paymentData==null) {
            paymentData = new PaymentData();
            payments.put(bookTripId, paymentData);
        }
        return paymentData;
    }






}