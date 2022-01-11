package model;

public class InstrumentDTO {
    private final String instrumentID;
    private final String type;
    private final String brand;
    private final String sn;
    private final String price;

    public InstrumentDTO(String instrumentID, String type, String brand, String sn, String price) {
        this.instrumentID = instrumentID;
        this.type = type;
        this.brand = brand;
        this.sn = sn;
        this.price = price;
    }

    public String getInstrumentID() {
        return instrumentID;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getSn() {
        return sn;
    }

    public String getPrice() {
        return price;
    }
}
