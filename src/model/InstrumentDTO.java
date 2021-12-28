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

    @Override
    public String toString() {
        return "ID: " + instrumentID + " | type: " + type + " | brand: " + brand + " | SN: " + sn + " | price: " + price;
    }
}
