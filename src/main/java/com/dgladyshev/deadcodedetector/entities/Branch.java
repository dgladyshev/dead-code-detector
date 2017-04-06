package com.dgladyshev.deadcodedetector.entities;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Branch {

    @NotEmpty
    private String name;

    //in case of empty name parameter we use default one
    public Branch() {
        this.name = "master";
    }

    public Branch(String name) {
        this.name = trimToEmpty(name);
    }

}
