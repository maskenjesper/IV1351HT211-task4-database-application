package model;

public class RentalDTO {
    private final String rentalID;
    private final String studentID;
    private final String instrumentID;
    private final String startTime;
    private final String endTime;
    private final boolean withDelivery;
    private final double price;

    public RentalDTO(String rentalID, String studentID, String instrumentID, String startTime, String endTime, boolean withDelivery, double price) {
        this.rentalID = rentalID;
        this.studentID = studentID;
        this.instrumentID = instrumentID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.withDelivery = withDelivery;
        this.price = price;
    }

    public String getRentalID() {
        return rentalID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getInstrumentID() {
        return instrumentID;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isWithDelivery() {
        return withDelivery;
    }

    public double getPrice() {
        return price;
    }
}
