package org.bp.skitrip;
import static org.apache.camel.model.rest.RestParamType.body;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.bp.skitrip.exceptions.PaymentException;
import org.bp.skitrip.exceptions.SkiEquipmentException;
import org.bp.skitrip.model.*;
import org.bp.skitrip.state.ProcessingEvent;
import org.bp.skitrip.state.ProcessingState;
import org.bp.skitrip.state.StateService;
import org.bp.skitrip.exceptions.SkiPassException;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TripBookingService extends RouteBuilder{
    @org.springframework.beans.factory.annotation.Autowired
    BookingIdentifierService bookingIdentifierService;

    @org.springframework.beans.factory.annotation.Autowired
    PaymentService paymentService;

    @org.springframework.beans.factory.annotation.Autowired
    StateService skiEquipmentStateService;

    @org.springframework.beans.factory.annotation.Autowired
    StateService skiPassStateService;

    private static final Logger logger = LoggerFactory.getLogger(TripBookingService.class);


    @Override
    public void configure() throws Exception {
            buySkiPassExceptionHandlers();
            rentSkiEquipmentExceptionHandlers();
            paymentExceptionHandlers();
            gateway();
            skiPass();
            skiEquipment();
            payment();
    }


    private void buySkiPassExceptionHandlers() {
        onException(SkiPassException.class)
                .process((exchange) -> {
                            ExceptionResponse er = new ExceptionResponse();
                            er.setTimestamp(OffsetDateTime.now());
                            Exception cause =
                                    exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            er.setMessage(cause.getMessage());
                            exchange.getMessage().setBody(er);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("skiPass"))
                .to("kafka:TripBookingFailTopic?brokers=localhost:9092")
                .handled(true)
        ;
    }

    private void rentSkiEquipmentExceptionHandlers() {
        onException(SkiEquipmentException.class)
        .process((exchange) -> {
                    ExceptionResponse er = new ExceptionResponse();
                    er.setTimestamp(OffsetDateTime.now());
                    Exception cause =
                            exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    er.setMessage(cause.getMessage());
                    exchange.getMessage().setBody(er);
                }
        )
        .marshal().json()
        .to("stream:out")
        .setHeader("serviceType", constant("skiEquipment"))
        .to("kafka:TripBookingFailTopic?brokers=localhost:9092")
        .handled(true)
    ;
    }

    private void paymentExceptionHandlers() {
        onException(PaymentException.class)
                .process((exchange) -> {
                            ExceptionResponse er = new ExceptionResponse();
                            er.setTimestamp(OffsetDateTime.now());
                            Exception cause =
                                    exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                            er.setMessage(cause.getMessage());
                            exchange.getMessage().setBody(er);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .setHeader("serviceType", constant("payment"))
                .to("kafka:TripBookingFailTopic?brokers=localhost:9092")
                .handled(true)
        ;
    }

    private void gateway() {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .contextPath("/api")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Micro ski trip booking API")
                .apiProperty("api.version", "1.0.0");

        rest("/skitrip").description("Micro ski trip booking REST service")
                .consumes("application/json")
                .produces("application/json")
                .post("/booking").description("Book a ski trip").type(BookTripRequest.class).outType(BookingInfo.class)
                .param().name("body").type(body).description("The ski trip to book").endParam()
                .responseMessage().code(200).message("Ski trip successfully booked").endResponseMessage()
                .to("direct:bookTrip")
        ;

        from("direct:bookTrip").routeId("bookTrip")
                .log("bookTrip fired")
                .process((exchange) -> {
                    exchange.getMessage().setHeader("bookingTripId", bookingIdentifierService.getBookingIdentifier());
                })
                .to("direct:TripBookRequest")
                .to("direct:bookRequester")
        ;

        from("direct:bookRequester").routeId("bookRequester")
                .log("bookRequester fired")
                .process(
                        (exchange) -> {
                            exchange.getMessage().setBody(Utils.prepareBookingInfo(
                                    exchange.getMessage().getHeader("bookingTripId", String.class), null));
                        }
                )
        ;
        from("direct:TripBookRequest").routeId("TripBookRequest")
                .log("brokerTopic fired")
                .marshal().json()
                .to("kafka:TripReqTopic?brokers=localhost:9092")
        ;
    }

    private void skiPass() {
        from("kafka:TripReqTopic?brokers=localhost:9092").routeId("buySkiPass")
                .log("fired buySkiPass")
                .unmarshal().json(JsonLibrary.Jackson, BookTripRequest.class)
                .process(
                        (exchange) -> {
                            String bookingTripId = exchange.getMessage().getHeader("bookingTripId", String.class);
                            ProcessingState previousState = skiPassStateService.sendEvent(bookingTripId, ProcessingEvent.START);
                            if (previousState!= ProcessingState.CANCELLED) {
                                BookingInfo bi = new BookingInfo();
                                bi.setId(bookingIdentifierService.getBookingIdentifier());
                                BookTripRequest btr = exchange.getMessage().getBody(BookTripRequest.class);
                                if (btr != null && btr.getSkiPass() != null) {
                                    String duration = btr.getSkiPass().getDuration();
                                    String type = btr.getSkiPass().getType();
                                    BigDecimal cost = calculateSkiPassCost(duration, type);
                                    if (cost == null) {
                                        throw new SkiPassException("cost: " + cost);
                                    } else {
                                        bi.setCost(cost);

                                    }
                                }
                                exchange.getMessage().setBody(bi);
                                previousState = skiPassStateService.sendEvent(bookingTripId, ProcessingEvent.FINISH);
                            }
                            exchange.getMessage().setHeader("previousState", previousState);
                        }
                )
                .marshal().json()
                .to("stream:out")
                .choice()
                    .when(header("previousState").isEqualTo(ProcessingState.CANCELLED))
                    .to("direct:buySkiPassCompensationAction")
                .otherwise()
                    .setHeader("serviceType", constant("skiPass"))
                .to("kafka:BookingInfoTopic?brokers=localhost:9092")
                .endChoice();

        from("kafka:TripBookingFailTopic?brokers=localhost:9092").routeId("buySkiPassCompensation")
                .log("fired buySkiPassCompensation")
                .unmarshal().json(JsonLibrary.Jackson, ExceptionResponse.class)
                .choice()
                .when(header("serviceType").isNotEqualTo("skiPass"))
                .process((exchange) -> {
                    String bookingTripId = exchange.getMessage().getHeader("bookingTripId", String.class);
                    ProcessingState previousState = skiPassStateService.sendEvent(bookingTripId,
                            ProcessingEvent.CANCEL);
                    exchange.getMessage().setHeader("previousState", previousState);
                })
                .choice()
                .when(header("previousState").isEqualTo(ProcessingState.FINISHED))
                .to("direct:buySkiPassCompensationAction")
                .endChoice()
                .endChoice();
        from("direct:buySkiPassCompensationAction").routeId("buySkiPassCompensationAction")
                .log("fired buySkiPassCompensationAction")
                .to("stream:out");
    }

    private void skiEquipment() {
        from("kafka:TripReqTopic?brokers=localhost:9092").routeId("rentSkiEquipment")
                .log("fired rentSkiEquipment")
                .unmarshal().json(JsonLibrary.Jackson, BookTripRequest.class)
                .process(
                        (exchange) -> {
                            String bookingTripId = exchange.getMessage().getHeader("bookingTripId", String.class);
                            ProcessingState previousState = skiEquipmentStateService.sendEvent(bookingTripId, ProcessingEvent.START);
                            if (previousState!= ProcessingState.CANCELLED) {
                                BookingInfo bi = new BookingInfo();
                                bi.setId(bookingIdentifierService.getBookingIdentifier());
                                BookTripRequest btr = exchange.getMessage().getBody(BookTripRequest.class);
                                if (btr != null && btr.getSkiEquipment() != null) {
                                    String equipmentType = btr.getSkiEquipment().getEquipmentType();
                                    String rentalDuration = btr.getSkiEquipment().getRentalDuration();
                                    BigDecimal cost = calculateRentalCost(equipmentType, rentalDuration);
                                    if (cost == null) {
                                        throw new SkiEquipmentException("cost: " + cost);
                                    } else {
                                        bi.setCost(cost);

                                    }
                                }
                                exchange.getMessage().setBody(bi);
                                previousState = skiEquipmentStateService.sendEvent(bookingTripId, ProcessingEvent.FINISH);
                            }
                            exchange.getMessage().setHeader("previousState", previousState);
                            }

                )
                .marshal().json()
                .to("stream:out")
                .choice()
                    .when(header("previousState").isEqualTo(ProcessingState.CANCELLED))
                    .to("direct:rentSkiEquipmentCompensationAction")
                .otherwise()
                    .setHeader("serviceType", constant("skiEquipment"))
                .to("kafka:BookingInfoTopic?brokers=localhost:9092" )

                .endChoice()
        ;
        from("kafka:TripBookingFailTopic?brokers=localhost:9092").routeId("rentSkiEquipmentCompensation")
                .log("fired rentSkiEquipmentCompensation")
                .unmarshal().json(JsonLibrary.Jackson, ExceptionResponse.class)
                .choice()
                .when(header("serviceType").isNotEqualTo("skiEquipment"))
                .process((exchange) -> {
                    String bookingTripId = exchange.getMessage().getHeader("bookingTripId", String.class);
                    ProcessingState previousState = skiEquipmentStateService.sendEvent(bookingTripId,
                            ProcessingEvent.CANCEL);
                    exchange.getMessage().setHeader("previousState", previousState);
                })
                .choice()
                .when(header("previousState").isEqualTo(ProcessingState.FINISHED))
                .to("direct:rentSkiEquipmentCompensationAction")
                .endChoice()
                .endChoice();
        from("direct:rentSkiEquipmentCompensationAction").routeId("rentSkiEquipmentCompensationAction")
                .log("fired rentSkiEquipmentCompensationAction")
                .to("stream:out");
    }

    private void payment() {
        from("kafka:BookingInfoTopic?brokers=localhost:9092").routeId("paymentBookingInfo")
                .log("fired paymentBookingInfo")
                .unmarshal().json(JsonLibrary.Jackson, BookingInfo.class)
                .process(
                        (exchange) -> {
                            String bookingTripId =
                                    exchange.getMessage().getHeader("bookingTripId", String.class);

                            boolean isReady= paymentService.addBookingInfo(
                                    bookingTripId,
                                    exchange.getMessage().getBody(BookingInfo.class),
                                    exchange.getMessage().getHeader("serviceType", String.class));
                            exchange.getMessage().setHeader("isReady", isReady);
                        }
                )
                .choice()
                .when(header("isReady").isEqualTo(true)).to("direct:finalizePayment")
                .endChoice();

        from("kafka:TripReqTopic?brokers=localhost:9092").routeId("paymentTripReq")
                .log("fired paymentTripReq")
                .unmarshal().json(JsonLibrary.Jackson, BookTripRequest.class)
                .process(
                        (exchange) -> {
                            String bookingTripId = exchange.getMessage()
                                    .getHeader("bookingTripId", String.class);
                            boolean isReady= paymentService.addBookTripRequest(
                                    bookingTripId,
                                    exchange.getMessage().getBody(BookTripRequest.class));
                            exchange.getMessage().setHeader("isReady", isReady);
                        }
                )
                .choice()
                .when(header("isReady").isEqualTo(true)).to("direct:finalizePayment")
                .endChoice();

        from("direct:finalizePayment").routeId("finalizePayment")
                .log("fired finalizePayment")
                .process(exchange -> {
                        String bookingTripId = exchange.getMessage().getHeader("bookingTripId", String.class);
                        PaymentService.PaymentData paymentData = paymentService.getPaymentData(bookingTripId);
                        BookTripRequest bookTripRequest = paymentData.bookTripRequest;
                        if (bookTripRequest == null || bookTripRequest.getPaymentCard() == null || bookTripRequest.getPerson() == null) {
                            throw new PaymentException("Payment details are missing.");
                        }
                            Person person = bookTripRequest.getPerson();
                        PaymentCard paymentCard = bookTripRequest.getPaymentCard();

                        if (person.getFirstName() == null || !Pattern.matches("^[\\p{L} ]+$", person.getFirstName()) ||
                                person.getLastName() == null || !Pattern.matches("^[\\p{L} ]+$", person.getLastName())) {
                            throw new PaymentException("First name and last name must contain only letters and spaces.");
                        }

                        if (paymentCard.getName() == null || !Pattern.matches("^[\\p{L} ]+$", paymentCard.getName())) {
                            throw new PaymentException("Card name must contain only letters and spaces.");
                        }

                        if (paymentCard.getValidTo() == null || !Pattern.matches("^(0[1-9]|1[0-2])/\\d{2}$", paymentCard.getValidTo())) {
                            throw new PaymentException("Card valid-to date must be in the format MM/YY.");
                        }

                        YearMonth validToDate = YearMonth.parse(paymentCard.getValidTo(), DateTimeFormatter.ofPattern("MM/yy"));
                        if (validToDate.isBefore(YearMonth.now())) {
                            throw new PaymentException("Card has expired.");
                        }

                        if (paymentCard.getNumber() == null || !paymentCard.getNumber().matches("^\\d+$")) {
                            throw new PaymentException("Card number must contain only digits.");
                        }

                        BigDecimal skiPassCost = paymentData.skiPassPurchaseInfo.getCost();
                        BigDecimal skiEquipmentCost = paymentData.skiEquipmentRentalInfo.getCost();
                        BigDecimal totalCost = skiPassCost.add(skiEquipmentCost);
                        BookingInfo tripBookingInfo = exchange.getMessage().getBody(BookingInfo.class);
                        tripBookingInfo.setId(bookingTripId);
                        tripBookingInfo.setCost(totalCost);
                        exchange.getMessage().setBody(tripBookingInfo);
                    }
                    )
                .to("direct:notification")
        ;
        from("direct:notification").routeId("notification")
                .streamCaching()
                .log("fired notification")
                .to("stream:out");

    }
    private BigDecimal calculateSkiPassCost(String duration, String type) {
        BigDecimal basePrice;
        switch (duration) {
            case "1 hour":
                basePrice = new BigDecimal("30");
                break;
            case "2 hours":
                basePrice = new BigDecimal("50");
                break;
            case "3 hours":
                basePrice = new BigDecimal("70");
                break;
            case "5 hours":
                basePrice = new BigDecimal("100");
                break;
            case "1 day":
                basePrice = new BigDecimal("150");
                break;
            case "3 days":
                basePrice = new BigDecimal("400");
                break;
            case "5 days":
                basePrice = new BigDecimal("600");
                break;
            case "1 week":
                basePrice = new BigDecimal("1000");
                break;
            case "2 weeks":
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


