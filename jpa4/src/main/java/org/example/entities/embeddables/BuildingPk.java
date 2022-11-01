package org.example.entities.embeddables;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class BuildingPk implements Serializable {
    private String code;
    private String number;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
