package org.bp.skiequipment;

import org.bp.types.BookingInfo;
import org.bp.types.Fault;
import org.bp.types.SkiEquipment;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@javax.jws.WebService
@org.springframework.stereotype.Service
public class SkiEquipmentService {
    public BookingInfo rentSkiEquipment(SkiEquipment skiEquipment) throws Fault {
//        BigDecimal calculatedPrice = calculateRentalCost(equipmentType, rentalDuration);
//
//        SkiEquipment skiEquipment = new SkiEquipment(equipmentType, rentalDuration, calculatedPrice);

        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setId(generateUniqueId());
        bookingInfo.setCost(new java.math.BigDecimal(345));
        return bookingInfo;

    }

    public BookingInfo cancelBooking(int bookingId)  throws Fault {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setId(bookingId);
        bookingInfo.setCost(java.math.BigDecimal.ZERO);
        return bookingInfo;
    }

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public static int generateUniqueId() {
        return idCounter.incrementAndGet();
    }

    public BigDecimal calculateRentalCost(String equipmentType, String rentalDuration) {
        BigDecimal basePrice;

        switch (equipmentType) {
            case "Skis":
                basePrice = new BigDecimal("35");
                break;
            case "Snowboard":
                basePrice = new BigDecimal("35");
                break;
            case "Boots":
                basePrice = new BigDecimal("20");
                break;
            case "Poles":
                basePrice = new BigDecimal("5");
                break;
            default:
                throw new IllegalArgumentException("Invalid equipment type: " + equipmentType);
        }

        switch (rentalDuration) {
            case "1 dzień":
                basePrice = basePrice.multiply(new BigDecimal("1"));
                break;
            case "2 dni":
                basePrice = basePrice.multiply(new BigDecimal("1.8"));
                break;
            case "3 dni":
                basePrice = basePrice.multiply(new BigDecimal("2.5"));
                break;
            case "1 tydzień":
                basePrice = basePrice.multiply(new BigDecimal("5"));
                break;
            case "2 tygodnie":
                basePrice = basePrice.multiply(new BigDecimal("9"));
                break;
            default:
                throw new IllegalArgumentException("Invalid rental duration: " + rentalDuration);
        }

        return basePrice;
    }
}
