package com.github.mhewedy.expressions.model;

import javax.persistence.Embeddable;

@Embeddable
public class LingualString {
    public String ar, en;

    public LingualString() {
    }

    public LingualString(String ar, String en) {
        this.ar = ar;
        this.en = en;
    }
}
