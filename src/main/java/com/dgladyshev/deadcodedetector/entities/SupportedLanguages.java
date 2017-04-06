package com.dgladyshev.deadcodedetector.entities;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.exceptions.UnsupportedParameterException;

public enum SupportedLanguages {

    JAVA("java"),
    ADA("ada"),
    FORTRAN("fortran"),
    C_PLUS_PLUS("c++");

    private String name;

    SupportedLanguages(String name) {
        this.name = name;
    }

    public static SupportedLanguages fromName(String languageName) {
        for (SupportedLanguages lang : SupportedLanguages.values()) {
            if (lang.name.equals(trimToEmpty(languageName.toLowerCase()))) {
                return lang;
            }
        }
        throw new UnsupportedParameterException("Language " + languageName + " is not supported");
    }

}
