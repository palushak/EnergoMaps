
package markers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Properties {

    @SerializedName("mag")
    @Expose
    private Double mag;
    @SerializedName("inkasent")
    @Expose
    private String inkasent;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("miasto")
    @Expose
    private String miasto;
    @SerializedName("ulica")
    @Expose
    private String ulica;
    @SerializedName("numerDomu")
    @Expose
    private String numerDomu;


    /**
     * No args constructor for use in serialization
     */
    public Properties() {
    }

    /**
     * @param mag
     * @param inkasent
     * @param numerDomu
     * @param status
     * @param miasto
     * @param ulica
     * @param numerDomu
     */
    public Properties(Double mag, String inkasent, String status, String miasto, String ulica, String numerDomu) {
        super();
        this.mag = mag;
        this.inkasent = inkasent;
        this.status = status;
        this.miasto = miasto;
        this.ulica = ulica;
        this.numerDomu = numerDomu;

    }

    public Double getMag() {
        return mag;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public String getInkasent() {
        return inkasent;
    }

    public void setInkasent(String inkasent) {
        this.inkasent = inkasent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMiasto() {
        return miasto;
    }

    public void setMiasto(String miasto) {
        this.miasto = miasto;
    }

    public String getUlica() {
        return ulica;
    }

    public void setUlica(String ulica) {
        this.ulica = ulica;
    }

    public String getNumerDomu() {
        return numerDomu;
    }

    public void setNumerDomu(String numerDomu) {
        this.numerDomu = numerDomu;
    }


}
