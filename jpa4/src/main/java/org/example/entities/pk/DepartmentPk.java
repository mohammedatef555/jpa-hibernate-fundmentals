package org.example.entities.pk;

import java.io.Serializable;

public class DepartmentPk implements Serializable {
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
