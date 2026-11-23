package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Verifies that the sheet screen's PDF export produces a real PDF for the displayed character. */
public final class PdfExportCheck {

    private static final Pattern TEXT_TOKEN = Pattern.compile("[\\p{L}]{3,}");

    private PdfExportCheck() {
    }

    public static void exportAndVerify(String characterName, int level, String raceName, String cultureName,
                                       String professionName) throws Exception {
        final Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final File pdfDir = new File(targetContext.getCacheDir(), "pdf");
        final Set<String> existing = pdfFiles(pdfDir);

        onView(withId(R.id.sheet_export_pdf_button)).perform(click());

        File exported = null;
        final long deadline = System.currentTimeMillis() + 120_000;
        while (System.currentTimeMillis() < deadline) {
            for (final String name : pdfFiles(pdfDir)) {
                if (!existing.contains(name)) {
                    final File candidate = new File(pdfDir, name);
                    if (candidate.length() > 0) {
                        exported = candidate;
                        break;
                    }
                }
            }
            if (exported != null) {
                break;
            }
            Thread.sleep(500);
        }
        assertNotNull("No PDF was exported to " + pdfDir, exported);

        final byte[] bytes = Files.readAllBytes(exported.toPath());
        assertTrue("Not a PDF: " + exported, bytes.length > 5
                && "%PDF-".equals(new String(bytes, 0, 5, StandardCharsets.US_ASCII)));

        final PdfReader reader = new PdfReader(bytes);
        try {
            assertTrue("PDF has no pages", reader.getNumberOfPages() > 0);
            final PdfTextExtractor extractor = new PdfTextExtractor(reader);
            final StringBuilder text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page)).append('\n');
            }
            final String extracted = text.toString();
            final Set<String> tokens = tokens(extracted);
            for (final String expected : new String[]{characterName, raceName, cultureName, professionName}) {
                assertTrue("The exported PDF " + exported.getName() + " has " + reader.getNumberOfPages()
                                + " pages but does not contain \"" + expected + "\" (text: "
                                + extracted.substring(0, Math.min(400, extracted.length())).replace('\n', ' ') + ")",
                        tokens.containsAll(tokens(expected)));
            }
            assertTrue("The exported PDF does not show level " + level,
                    Pattern.compile("\\b" + level + "\\b").matcher(extracted).find());
        } finally {
            reader.close();
        }
    }

    private static Set<String> tokens(String text) {
        final String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        final Matcher matcher = TEXT_TOKEN.matcher(normalized);
        final Set<String> result = new HashSet<>();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private static Set<String> pdfFiles(File dir) {
        final Set<String> result = new HashSet<>();
        if (dir == null || !dir.isDirectory()) {
            return result;
        }
        final File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".pdf"));
        if (files != null) {
            for (final File file : files) {
                result.add(file.getName());
            }
        }
        return result;
    }
}
