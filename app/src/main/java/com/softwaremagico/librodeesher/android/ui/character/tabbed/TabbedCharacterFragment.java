package com.softwaremagico.librodeesher.android.ui.character.tabbed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.ui.character.CharacterCreationFragment;
import com.softwaremagico.librodeesher.android.ui.characteristics.CharacteristicsFragment;
import com.softwaremagico.librodeesher.android.ui.equipment.EquipmentFragment;
import com.softwaremagico.librodeesher.android.ui.history.HistoryFragment;
import com.softwaremagico.librodeesher.android.ui.perks.PerksFragment;
import com.softwaremagico.librodeesher.android.ui.resistances.ResistancesFragment;
import com.softwaremagico.librodeesher.android.ui.skills.SkillsFragment;
import com.softwaremagico.librodeesher.android.ui.spells.SpellsFragment;
import com.softwaremagico.librodeesher.android.ui.weapontiers.WeaponTiersFragment;

public class TabbedCharacterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_tabbed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final androidx.fragment.app.FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        final CharacterTabPagerAdapter adapter = new CharacterTabPagerAdapter(activity);
        final ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getItemCount());

        final TabLayout tabs = view.findViewById(R.id.tabs);
        for (int i = 0; i < adapter.getItemCount(); i++) {
            tabs.addTab(tabs.newTab().setText(adapter.getTitle(i)));
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabs.selectTab(tabs.getTabAt(position));
            }
        });
    }
}
