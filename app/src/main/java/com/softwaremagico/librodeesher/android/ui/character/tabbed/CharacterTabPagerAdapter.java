package com.softwaremagico.librodeesher.android.ui.character.tabbed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.ui.character.CharacterCreationFragment;
import com.softwaremagico.librodeesher.android.ui.characteristics.CharacteristicsFragment;
import com.softwaremagico.librodeesher.android.ui.equipment.EquipmentFragment;
import com.softwaremagico.librodeesher.android.ui.history.HistoryFragment;
import com.softwaremagico.librodeesher.android.ui.perks.PerksFragment;
import com.softwaremagico.librodeesher.android.ui.resistances.ResistancesFragment;
import com.softwaremagico.librodeesher.android.ui.skills.SkillsFragment;
import com.softwaremagico.librodeesher.android.ui.spells.SpellsFragment;
import com.softwaremagico.librodeesher.android.ui.categories.CategoriesFragment;
import com.softwaremagico.librodeesher.android.ui.weapontiers.WeaponTiersFragment;

public class CharacterTabPagerAdapter extends FragmentStateAdapter {

    private final int[] titles = new int[]{
            R.string.navigation_character,
            R.string.navigation_characteristics,
            R.string.navigation_skills,
            R.string.navigation_spells,
            R.string.navigation_perks,
            R.string.navigation_history,
            R.string.navigation_equipment,
            R.string.navigation_resistances,
            R.string.navigation_categories,
            R.string.navigation_weapon_tiers
    };

    public CharacterTabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new CharacterCreationFragment();
        } else if (position == 1) {
            return new CharacteristicsFragment();
        } else if (position == 2) {
            return new com.softwaremagico.librodeesher.android.ui.skills.SkillsDetailedFragment();
        } else if (position == 3) {
            return new SpellsFragment();
        } else if (position == 4) {
            return new PerksFragment();
        } else if (position == 5) {
            return new HistoryFragment();
        } else if (position == 6) {
            return new EquipmentFragment();
        } else if (position == 7) {
            return new ResistancesFragment();
        } else if (position == 8) {
            return new CategoriesFragment();
        } else if (position == 9) {
            return new WeaponTiersFragment();
        }
        return new CharacterCreationFragment();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public int getTitle(int position) {
        return titles[position];
    }
}
