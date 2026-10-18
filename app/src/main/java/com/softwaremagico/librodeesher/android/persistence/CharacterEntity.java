package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.persistence.CharacterData;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Persisted character: a serialized LibroDeEsher {@link CharacterData} snapshot plus a few flat
 * columns kept for listing.
 */
@Entity(tableName = CharacterEntity.CHARACTER_PLAYER_TABLE)
public class CharacterEntity extends BaseEntity {
    public static final String CHARACTER_PLAYER_TABLE = "character_player";

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "race")
    public String race;

    @ColumnInfo(name = "culture")
    public String culture;

    @ColumnInfo(name = "profession")
    public String profession;

    @ColumnInfo(name = "level")
    public int level;

    @ColumnInfo(name = "character_as_json")
    public String json;

    public CharacterEntity() {
        super();
        creationTime = new Timestamp(new Date().getTime());
        updateTime = creationTime;
    }

    public CharacterEntity(CharacterPlayer characterPlayer) {
        this();
        setCharacterPlayer(characterPlayer);
    }

    public final void setCharacterPlayer(CharacterPlayer characterPlayer) {
        if (characterPlayer == null) {
            return;
        }
        updateTime = new Timestamp(new Date().getTime());
        final CharacterData data = CharacterDataMapper.toData(characterPlayer);
        setJson(CharacterJsonManager.toJson(data));
        setName(characterPlayer.getName());
        setLevel(characterPlayer.getLevel());
        setRace(display(resolveRace(characterPlayer)));
        setCulture(display(resolveCulture(characterPlayer)));
        setProfession(display(resolveProfession(characterPlayer)));
    }

    private static Race resolveRace(CharacterPlayer characterPlayer) {
        try {
            return characterPlayer.getRace();
        } catch (InvalidXmlElementException exception) {
            return null;
        }
    }

    private static Culture resolveCulture(CharacterPlayer characterPlayer) {
        try {
            return characterPlayer.getCulture();
        } catch (InvalidXmlElementException exception) {
            return null;
        }
    }

    private static Profession resolveProfession(CharacterPlayer characterPlayer) {
        try {
            return characterPlayer.getProfession();
        } catch (InvalidXmlElementException exception) {
            return null;
        }
    }

    public CharacterPlayer getCharacterPlayer() {
        final CharacterData data = CharacterJsonManager.fromJson(json);
        if (data == null) {
            return null;
        }
        try {
            return CharacterDataMapper.toCharacter(data);
        } catch (InvalidXmlElementException exception) {
            return null;
        }
    }

    private static String display(com.softwaremagico.librodeesher.Element element) {
        if (element == null) {
            return null;
        }
        if (element.getName() != null && element.getName().getTranslatedText() != null) {
            return element.getName().getTranslatedText();
        }
        return element.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
