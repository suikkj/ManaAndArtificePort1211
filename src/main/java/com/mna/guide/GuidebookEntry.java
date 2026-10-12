 
package com.mna.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.guide.EntryCategory;
import com.mna.guide.RelatedRecipe;
import com.mna.guide.interfaces.IEntrySection;
import com.mna.guide.sections.ImageSection;
import com.mna.guide.sections.ItemSection;
import com.mna.guide.sections.RecipeSection;
import com.mna.guide.sections.TextSection;
import com.mna.guide.sections.TitleSection;
import com.mna.network.ClientMessageDispatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class GuidebookEntry {
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_TIER = "tier";
    private static final String KEY_ADVANCEMENT = "required_advancement";
    private static final String KEY_INDEX = "index";
    private static final String KEY_SECTIONS = "sections";
    private static final String KEY_SECTION_TYPE = "type";
    private static final String KEY_RELATED_RECIPES = "related_recipes";
    private static final int MAX_RELATED_RECIPES = 10;
    private static final int COLOR_NON_MNA = 6295964;
    private static final HashMap<String, Class<? extends IEntrySection>> typeRegistry = new HashMap<String, Class<? extends IEntrySection>>(){
        {
            this.put("text", TextSection.class);
            this.put("image", ImageSection.class);
            this.put("title", TitleSection.class);
            this.put("item", ItemSection.class);
            this.put("recipe", RecipeSection.class);
        }
    };
    private final NonNullList<IEntrySection> sections;
    private final NonNullList<RelatedRecipe> relatedRecipes;
    private final boolean isValid;
    private final EntryCategory category;
    private final Optional<Integer> tier;
    private final Optional<ResourceLocation> requiredAdvancement;
    private final int index;
    private final String name;
    private final String modid;
    int y = 10;
    int x_max = 108;
    int y_max = 168;
    int page = 0;

    public GuidebookEntry(JsonObject entryObject, String name, String modid) {
        boolean valid = true;
        this.name = name;
        this.modid = modid;
        this.sections = NonNullList.create();
        this.relatedRecipes = NonNullList.create();
        valid = this.parseSections(entryObject);
        valid = this.parseRelatedRecipes(entryObject);
        this.category = this.parseCategory(entryObject);
        this.tier = this.parseTier(entryObject);
        this.requiredAdvancement = this.parseAdvancement(entryObject);
        this.index = this.parseIndex(entryObject);
        if (this.getFirstTitle() == null) {
            ManaAndArtifice.LOGGER.error(String.format("Entry '%s' of the codex is missing the required title section (must have at least one)", name));
            valid = false;
        }
        this.isValid = valid;
    }

    private boolean parseSections(JsonObject entryObject) {
        boolean added_addedby = this.isBaseMnA();
        if (entryObject.has(KEY_SECTIONS)) {
            JsonArray sections = entryObject.get(KEY_SECTIONS).getAsJsonArray();
            for (JsonElement elem : sections) {
                JsonObject elemObj;
                if (!elem.isJsonObject() || !(elemObj = elem.getAsJsonObject()).has(KEY_SECTION_TYPE)) continue;
                String type = elemObj.get(KEY_SECTION_TYPE).getAsString();
                Class<? extends IEntrySection> clazz = typeRegistry.get(type);
                try {
                    IEntrySection section = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    Collection<IEntrySection> parsed_sections = section.parse(elemObj, this.y, this.y_max, this.x_max, this.page);
                    this.sections.addAll(parsed_sections);
                    if (!added_addedby && this.sections.get(0) instanceof TitleSection) {
                        ((TitleSection)this.sections.get(0)).setOverrideColor(6295964);
                        Optional otherModContainer = ModList.get().getModContainerById(this.modid);
                        if (!otherModContainer.isPresent()) {
                            ((TitleSection)this.sections.get(0)).getTooltip().add(Component.translatable((String)"item.mna.guide_book.othermod.unknown"));
                        } else {
                            ((TitleSection)this.sections.get(0)).getTooltip().add(Component.translatable((String)"item.mna.guide_book.othermod", (Object[])new Object[]{((ModContainer)otherModContainer.get()).getModInfo().getDisplayName()}));
                        }
                        added_addedby = true;
                    }
                    for (IEntrySection s : parsed_sections) {
                        if (!this.isBaseMnA()) {
                            s.setNotBaseMna();
                        }
                        if (s.getPage() > this.page) {
                            this.page = s.getPage();
                            this.y = s.getHeight(this.y_max);
                            continue;
                        }
                        this.y += s.getHeight(this.y_max);
                    }
                }
                catch (Exception exception) {
                }
            }
            ((IEntrySection)this.sections.get(this.sections.size() - 1)).setPadding(0);
            return true;
        }
        return false;
    }

    private boolean parseRelatedRecipes(JsonObject entryObject) {
        JsonElement rr;
        if (entryObject.has(KEY_RELATED_RECIPES) && (rr = entryObject.get(KEY_RELATED_RECIPES)).isJsonArray()) {
            JsonArray arr = rr.getAsJsonArray();
            for (JsonElement elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                String type = "crafting";
                ResourceLocation[] rLocs = new ResourceLocation[1];
                if (obj.has(KEY_SECTION_TYPE)) {
                    type = obj.get(KEY_SECTION_TYPE).getAsString();
                }
                if (obj.has("locations")) {
                    rLocs = this.parseResourceLocations(obj.get("locations").getAsJsonArray());
                } else {
                    rLocs[0] = ResourceLocation.parse(obj.get("location").getAsString());
                }
                this.relatedRecipes.add(new RelatedRecipe(type, rLocs));
                if (this.relatedRecipes.size() < 10) continue;
                break;
            }
        }
        return true;
    }

    private EntryCategory parseCategory(JsonObject entryObject) {
        if (entryObject.has(KEY_CATEGORY)) {
            EntryCategory eCat;
            String c = entryObject.get(KEY_CATEGORY).getAsString();
            try {
                eCat = EntryCategory.valueOf(c.toUpperCase());
            }
            catch (Exception ex) {
                eCat = EntryCategory.BASICS;
            }
            return eCat;
        }
        return EntryCategory.BASICS;
    }

    private Optional<Integer> parseTier(JsonObject entryObject) {
        if (entryObject.has(KEY_TIER)) {
            return Optional.of(entryObject.get(KEY_TIER).getAsInt());
        }
        return Optional.empty();
    }

    private Optional<ResourceLocation> parseAdvancement(JsonObject entryObject) {
        if (entryObject.has(KEY_ADVANCEMENT)) {
            return Optional.of(ResourceLocation.parse(entryObject.get(KEY_ADVANCEMENT).getAsString()));
        }
        return Optional.empty();
    }

    private int parseIndex(JsonObject entryObject) {
        if (entryObject.has(KEY_INDEX)) {
            return entryObject.get(KEY_INDEX).getAsInt();
        }
        return 0;
    }

    private ResourceLocation[] parseResourceLocations(JsonArray obj) {
        ArrayList<ResourceLocation> rLocs = new ArrayList<ResourceLocation>();
        for (int i = 0; i < obj.size(); ++i) {
            JsonElement elem = obj.get(i);
            if (!elem.isJsonPrimitive()) continue;
            rLocs.add(ResourceLocation.parse(elem.getAsString()));
        }
        return (ResourceLocation[])((rLocs).toArray(new ResourceLocation[0]));
    }

    public int getTier() {
        if (this.tier.isPresent()) {
            return this.tier.get();
        }
        if (this.relatedRecipes.size() == 0) {
            return 1;
        }
        Optional<RelatedRecipe> min = this.relatedRecipes.stream().min(Comparator.comparing(RelatedRecipe::getTier));
        return min.isPresent() ? min.get().getTier() : 1;
    }

    @Nullable
    public ResourceLocation getRequiredAdvancement() {
        if (this.requiredAdvancement.isPresent()) {
            return this.requiredAdvancement.get();
        }
        return null;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public String getFirstTitle() {
        TitleSection title = this.sections.stream().filter(s -> s instanceof TitleSection).map(s -> (TitleSection)s).findFirst().get();
        if (title == null) {
            return null;
        }
        return title.getText();
    }

    public EntryCategory getCategory() {
        return this.category;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public boolean isBaseMnA() {
        return this.modid == null || "mna".equals(this.modid);
    }

    public boolean isRead() {
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (player == null) {
            return false;
        }
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null) {
            return false;
        }
        return progression.isCodexEntryRead(this.name.hashCode());
    }

    public void setRead() {
        Player player = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (player == null) {
            return;
        }
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null) {
            return;
        }
        if (progression.setCodexEntryRead(this.name.hashCode())) {
            ClientMessageDispatcher.sendCodexEntryRead(this.name.hashCode());
        }
    }

    public String getModId() {
        return this.modid;
    }

    public int getOverrideColor() {
        return ((IEntrySection)this.sections.get(0)).getOverrideColor();
    }

    public NonNullList<Component> getTooltip() {
        NonNullList<Component> comp = ((IEntrySection)this.sections.get(0)).getTooltip();
        return comp;
    }

    public NonNullList<IEntrySection> getSections() {
        return this.sections;
    }

    public NonNullList<RelatedRecipe> getRelatedRecipes() {
        return this.relatedRecipes;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GuidebookEntry) {
            return ((GuidebookEntry)obj).getName().equals(this.getName());
        }
        return false;
    }

    public String toString() {
        return this.getName();
    }
}


