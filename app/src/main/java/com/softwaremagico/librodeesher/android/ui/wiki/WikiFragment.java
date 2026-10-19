package com.softwaremagico.librodeesher.android.ui.wiki;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.Element;
import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rules browser ("Ayuda"): lists the elements loaded by the LibroDeEsher rules engine (races,
 * cultures, professions, categories, skills, weapons, perks and spell lists) with a detail dialog.
 */
public class WikiFragment extends Fragment {

    private Spinner typeSpinner;
    private TextView countText;
    private ListView listView;

    private final List<Section> sections = new ArrayList<>();
    private final List<Element> currentElements = new ArrayList<>();

    /** Supplies the elements of one section, loading them lazily from the rules catalog. */
    @FunctionalInterface
    private interface ElementSupplier {
        List<? extends Element> get() throws InvalidXmlElementException;
    }

    private static final class Section {
        private final String label;
        private final ElementSupplier supplier;

        private Section(String label, ElementSupplier supplier) {
            this.label = label;
            this.supplier = supplier;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wiki, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        typeSpinner = view.findViewById(R.id.wiki_type_spinner);
        countText = view.findViewById(R.id.wiki_count_text);
        listView = view.findViewById(R.id.wiki_list);

        buildSections();
        typeSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels()));
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View selected, int position, long id) {
                loadSection(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Nothing to do.
            }
        });
        listView.setOnItemClickListener((parent, selected, position, id) -> showDetail(position));
    }

    private void buildSections() {
        sections.clear();
        final RulesCatalog catalog = RulesCatalog.getInstance();
        sections.add(new Section(getString(R.string.wiki_races), catalog::getRaces));
        sections.add(new Section(getString(R.string.wiki_cultures), catalog::getCultures));
        sections.add(new Section(getString(R.string.wiki_professions), catalog::getProfessions));
        sections.add(new Section(getString(R.string.wiki_categories), catalog::getCategories));
        sections.add(new Section(getString(R.string.wiki_skills), catalog::getSkills));
        sections.add(new Section(getString(R.string.wiki_weapons), catalog::getWeapons));
        sections.add(new Section(getString(R.string.wiki_perks), catalog::getPerks));
        sections.add(new Section(getString(R.string.wiki_spell_lists), catalog::getSpellLists));
    }

    private List<String> labels() {
        final List<String> labels = new ArrayList<>();
        for (final Section section : sections) {
            labels.add(section.label);
        }
        return labels;
    }

    private void loadSection(int index) {
        if (index < 0 || index >= sections.size()) {
            return;
        }
        currentElements.clear();
        try {
            currentElements.addAll(sections.get(index).supplier.get());
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load wiki section", exception);
            countText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            listView.setAdapter(null);
            return;
        }
        currentElements.sort(Comparator.comparing(ElementAdapter::displayName));
        final List<String> names = new ArrayList<>();
        for (final Element element : currentElements) {
            names.add(ElementAdapter.displayName(element));
        }
        listView.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, names));
        countText.setText(getString(R.string.wiki_count, currentElements.size()));
    }

    private void showDetail(int position) {
        if (position < 0 || position >= currentElements.size()) {
            return;
        }
        final Element element = currentElements.get(position);
        final StringBuilder message = new StringBuilder();
        message.append(getString(R.string.wiki_detail_id, nullToDash(element.getId())));
        if (element.getModuleName() != null) {
            message.append('\n').append(getString(R.string.wiki_detail_module, element.getModuleName()));
        }
        if (element.getGroup() != null) {
            message.append('\n').append(getString(R.string.wiki_detail_group, element.getGroup()));
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(ElementAdapter.displayName(element))
                .setMessage(message.toString())
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
