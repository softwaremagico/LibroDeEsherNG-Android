package com.softwaremagico.librodeesher.android.core;

import android.content.Context;

import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.pdf.CombinedOneColumnCharacterSheet;
import com.softwaremagico.librodeesher.pdf.CombinedTwoColumnsCharacterSheet;
import com.softwaremagico.librodeesher.pdf.LegacyCombinedCharacterSheet;
import com.softwaremagico.librodeesher.pdf.PdfDocument;
import com.softwaremagico.librodeesher.pdf.StandardCharacterSheet;

/** Builds the PDF sheet layout selected in the options screen. */
public final class PdfSheetFactory {

    private PdfSheetFactory() {
    }

    public static PdfDocument create(Context context) {
        switch (OptionsHandler.getInstance(context).getPdfMode()) {
            case 1:
                return new CombinedOneColumnCharacterSheet();
            case 2:
                return new CombinedTwoColumnsCharacterSheet();
            case 3:
                return new LegacyCombinedCharacterSheet();
            default:
                return new StandardCharacterSheet();
        }
    }
}
