package payment.gateway.ProductsDto;

public class ProductsDto {
    private String pname;
    private long pcost;
    private short units;
    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public long getPcost() {
        return pcost;
    }

    public void setPcost(long pcost) {
        this.pcost = pcost;
    }

    public short getUnits() {
        return units;
    }

    public void setUnits(short units) {
        this.units = units;
    }
}
