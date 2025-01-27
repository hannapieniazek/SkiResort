package org.bp.skitrip;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class BookingIdentifierService {
    HashMap<String, BookingIds> bookingIdsMap =  new HashMap<>();

    public String generateBookingId() {
        String bookingID=UUID.randomUUID().toString();
        BookingIds bookingIds= new BookingIds();
        bookingIdsMap.put(bookingID, bookingIds);
        return bookingID;
    }

    public void assignSkiPassPurchaseId(String tripBookingId, int skiPassPurchaseId) {
        bookingIdsMap.get(tripBookingId).setSkiPassPurchaseId(skiPassPurchaseId);
    }

    public void assignSkiEquipmentRentalId(String tripBookingId, int skiEquipmentRentalId) {
        bookingIdsMap.get(tripBookingId).setSkiEquipmentRentalId(skiEquipmentRentalId);
    }

    public int getSkiPassPurchaseId(String tripBookingId) {
        return bookingIdsMap.get(tripBookingId).getSkiPassPurchaseId();
    }

    public int getSkiEquipmentRentalId(String tripBookingId) {
        return bookingIdsMap.get(tripBookingId).getSkiEquipmentRentalId();
    }

    public static class BookingIds{
        private int skiPassPurchaseId;
        private int skiEquipmentRentalId;


        public int getSkiPassPurchaseId() {
            return skiPassPurchaseId;
        }

        public void setSkiPassPurchaseId(int skiPassPurchaseId) {
            this.skiPassPurchaseId = skiPassPurchaseId;
        }

        public int getSkiEquipmentRentalId() {
            return skiEquipmentRentalId;
        }

        public void setSkiEquipmentRentalId(int skiEquipmentRentalId) {
            this.skiEquipmentRentalId = skiEquipmentRentalId;
        }
    }

}