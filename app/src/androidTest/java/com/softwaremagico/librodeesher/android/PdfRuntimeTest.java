package com.softwaremagico.librodeesher.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.pdf.CharacterPdfExporter;
import com.softwaremagico.librodeesher.pdf.StandardCharacterSheet;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Guards the AWT shim needed by OpenPDF on Android. */
@RunWith(AndroidJUnit4.class)
public class PdfRuntimeTest {

    @Test
    public void openPdfGeneratesAValidDocumentOnAndroid() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, output);
        document.open();
        document.add(new Paragraph("Prueba PDF"));
        document.close();

        final byte[] bytes = output.toByteArray();
        assertTrue("The generated PDF is too small: " + bytes.length, bytes.length > 100);
        assertEquals("%PDF-", new String(bytes, 0, 5, StandardCharsets.US_ASCII));
    }

    @Test
    public void libroDeEsherCharacterSheetGeneratesAValidDocumentOnAndroid() throws Exception {
        RulesCatalog.getInstance();
        final CharacterPlayer character = new CharacterPlayer();
        character.setRaceId(RulesCatalog.getInstance().getRaces().get(0).getId());
        character.setCultureId(RulesCatalog.getInstance().getCultures().get(0).getId());
        character.setProfessionId(RulesCatalog.getInstance().getProfessions().get(0).getId());
        new RandomCharacterPlayer(character).createRandomValues();

        final String json = CharacterJsonManager.toJson(CharacterDataMapper.toData(character));
        final byte[] bytes = CharacterPdfExporter.exportFromJSON(json, new StandardCharacterSheet());
        assertTrue("The generated sheet is too small: " + bytes.length, bytes.length > 3000);
        assertEquals("%PDF-", new String(bytes, 0, 5, StandardCharsets.US_ASCII));

        final PdfReader reader = new PdfReader(bytes);
        try {
            assertTrue("The generated sheet has no pages", reader.getNumberOfPages() > 0);
        } finally {
            reader.close();
        }
    }
}
