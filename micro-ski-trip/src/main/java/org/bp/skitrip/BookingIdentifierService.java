package org.bp.skitrip;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class BookingIdentifierService {

    public String getBookingIdentifier() {
        return UUID.randomUUID().toString();
    }


}