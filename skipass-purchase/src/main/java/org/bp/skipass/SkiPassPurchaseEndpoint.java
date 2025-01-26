package org.bp.skipass;

import org.bp.BuySkiPassRequest;
import org.bp.CancelPurchaseRequest;
import org.bp.types.BookingInfo;
import org.bp.types.Fault;
import org.bp.types.SkiPass;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SkiPassPurchaseEndpoint implements SkiPassPurchase {

    @Override
    public BookingInfo buySkiPass(BuySkiPassRequest payload) throws SkiPassFaultMsg {
        if (payload == null || payload.getPerson() == null || payload.getSkiPass() == null) {
            Fault fault = new Fault();
            fault.setCode(400);
            fault.setText("Invalid request: person or ski pass details are missing.");
            throw new SkiPassFaultMsg("Invalid request", fault);
        }
        String firstName = payload.getPerson().getFirstName();
        String lastName = payload.getPerson().getLastName();
        if (!isValidName(firstName) || !isValidName(lastName)) {
            Fault fault = new Fault();
            fault.setCode(400);
            fault.setText("Invalid name: First name and last name must contain only letters and spaces.");
            throw new SkiPassFaultMsg("Validation error", fault);
        }

        SkiPass skiPass = payload.getSkiPass();
        String validity = skiPass.getDuration();
        String type = skiPass.getType();

        // Calculate the price based on validity and type
        BigDecimal calculatedPrice = calculateSkiPassCost(validity, type);

        // Update the SkiPass price
        skiPass.setPrice(calculatedPrice);

        // Create BookingInfo and set cost
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setId(generateUniqueId()); // Ensure unique ID generation logic
        bookingInfo.setCost(calculatedPrice); // Set the calculated price as cost

        return bookingInfo;
    }

    @Override
    public BookingInfo cancelPurchase(CancelPurchaseRequest payload) throws SkiPassFaultMsg {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setId(payload.getBookingId());
        bookingInfo.setCost(java.math.BigDecimal.ZERO);
        return bookingInfo;
    }

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public static int generateUniqueId() {
        return idCounter.incrementAndGet();
    }

    public BigDecimal calculateSkiPassCost(String duration, String type) {
        BigDecimal basePrice;
        switch (duration) {
            case "1 godzina":
                basePrice = new BigDecimal("30");
                break;
            case "2 godziny":
                basePrice = new BigDecimal("50");
                break;
            case "3 godziny":
                basePrice = new BigDecimal("70");
                break;
            case "5 godzin":
                basePrice = new BigDecimal("100");
                break;
            case "1 dzień":
                basePrice = new BigDecimal("150");
                break;
            case "3 dni":
                basePrice = new BigDecimal("400");
                break;
            case "5 dni":
                basePrice = new BigDecimal("600");
                break;
            case "1 tydzień":
                basePrice = new BigDecimal("1000");
                break;
            case "2 tygodnie":
                basePrice = new BigDecimal("1800");
                break;
            default:
                throw new IllegalArgumentException("Invalid duration: " + duration);
        }
        if ("DISCOUNTED".equalsIgnoreCase(type)) {
            basePrice = basePrice.multiply(new BigDecimal("0.5"));
        }

        return basePrice;
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z ]+");
    }
}