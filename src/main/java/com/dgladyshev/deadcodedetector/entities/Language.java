package com.dgladyshev.deadcodedetector.entities;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.exceptions.UnsupportedParameterException;
import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Language {

    private Set<String> supportedLanguages = Sets.newHashSet("java", "ada", "fortran", "c++");

    @NotEmpty
    private String name;

    public Language(String name) {
        String verifiable = trimToEmpty(name.toLowerCase());
        if (supportedLanguages.contains(verifiable)) {
            this.name = StringUtils.trimToEmpty(name);
        } else {
            throw new UnsupportedParameterException("Language " + name + " is not supported");
        }
    }

}
