package com.softwaremagico.librodeesher.android.ui.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.softwaremagico.librodeesher.Element;
import com.softwaremagico.librodeesher.language.TranslatedText;

import java.util.List;

/**
 * Spinner adapter that renders LibroDeEsher rule elements using their translated display name
 * ({@link TranslatedText#getTranslatedText()}), falling back to the stable id.
 *
 * <p>This is the adapted counterpart of the Advisor {@code ElementAdapter}: the NG {@link Element}
 * is simpler than ThinkMachine's (no {@code getNameRepresentation}/{@code getRestrictions}), so the
 * display text is resolved directly from the name.</p>
 */
public class ElementAdapter<T extends Element> extends ArrayAdapter<T> {

    public ElementAdapter(Context context, List<T> items) {
        super(context, android.R.layout.simple_spinner_item, items);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    /** The text shown for {@code element}, used by spinners and external callers alike. */
    public static String displayName(Element element) {
        if (element == null) {
            return "";
        }
        final TranslatedText name = element.getName();
        if (name == null) {
            return element.getId() == null ? "" : element.getId();
        }
        final String translated = name.getTranslatedText();
        return translated == null ? element.getId() : translated;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);
        view.setText(displayName(getItem(position)));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(displayName(getItem(position)));
        return view;
    }
}
