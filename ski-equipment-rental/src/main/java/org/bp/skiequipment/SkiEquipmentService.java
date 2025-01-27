package org.bp.skiequipment;

import org.bp.types.*;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@javax.jws.WebService
@org.springframework.stereotype.Service
public class SkiEquipmentService {
    public BookingInfo rentSkiEquipment(SkiEquipmentRental skiEquipmentRental) throws Fault {
        SkiEquipment skiEquipment = skiEquipmentRental.getSkiEquipment();
        Person person = skiEquipmentRental.getPerson();

        if (skiEquipment == null || person == null) {
            throw new Fault(400, "Invalid rental request: SkiEquipment or Person is missing.");
        }

        String equipmentType = skiEquipment.getEquipmentType();
        String rentalDuration = skiEquipment.getRentalDuration();
        BigDecimal calculatedPrice = calculateRentalCost(equipmentType, rentalDuration);

        skiEquipment.setPrice(calculatedPrice);

        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setId(generateUniqueId());
        bookingInfo.setCost(calculatedPrice);

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
            case "1 day":
                basePrice = basePrice.multiply(new BigDecimal("1"));
                break;
            case "2 days":
                basePrice = basePrice.multiply(new BigDecimal("1.8"));
                break;
            case "3 days":
                basePrice = basePrice.multiply(new BigDecimal("2.5"));
                break;
            case "1 week":
                basePrice = basePrice.multiply(new BigDecimal("5"));
                break;
            case "2 weeks":
                basePrice = basePrice.multiply(new BigDecimal("9"));
                break;
            default:
                throw new IllegalArgumentException("Invalid rental duration: " + rentalDuration);
        }

        return basePrice;
    }
}
