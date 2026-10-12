/*
 * Decompiled with CFR 0.152.
 */
package com.mna.api.spells;

public enum SpellValidityResult {
    READY(""),
    NO_SHAPE("gui.mna.inscription.shape_missing"),
    NO_COMPONENT("gui.mna.inscription.component_missing"),
    MISSING_INK("gui.mna.inscription.ink_missing"),
    MISSING_PAPER("gui.mna.inscription.paper_missing"),
    MISSING_ASH("gui.mna.inscription.ash_missing"),
    OUTPUT_FULL("gui.mna.inscription.output_full"),
    LOW_TIER("gui.mna.inscription.low_tier"),
    TOO_COMPLEX("gui.mna.inscription.too_complex"),
    WRONG_FACTION("gui.mna.inscription.wrong_faction"),
    ALREADY_CRAFTING(""),
    CANNOT_CHANNEL("gui.mna.inscription.component_not_channeled");

    private String lang_key;

    private SpellValidityResult(String lang_key) {
        this.lang_key = lang_key;
    }

    public String getTranslationKey() {
        return this.lang_key;
    }
}

