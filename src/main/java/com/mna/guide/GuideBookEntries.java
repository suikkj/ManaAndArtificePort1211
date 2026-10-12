 
package com.mna.guide;

import com.mna.tools.EnchantmentUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mna.ManaAndArtifice;
import com.mna.api.guidebook.IGuideBookRegistry;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.guide.EntryCategory;
import com.mna.guide.GuidebookEntry;
import com.mna.guide.RelatedRecipe;
import com.mna.guide.recipe.init.RecipeRenderers;
import com.mna.guide.sections.TextSection;
import com.mna.network.ClientMessageDispatcher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import com.mna.api.tools.MNACap;

public class GuideBookEntries
implements IGuideBookRegistry {
    public static final GuideBookEntries INSTANCE = new GuideBookEntries();
    private Map<String, GuidebookEntry> _entries;
    private List<ResourceLocation> guidebookPaths;
    private boolean hasLoaded = false;
    private String lastLanguage = "";
    private boolean lastUnicode = false;
    ResourceLocation _default = ResourceLocation.fromNamespaceAndPath("mna", "guide/en_us.json");
    ResourceLocation _default_packadds = ResourceLocation.fromNamespaceAndPath("mna", "guide/pack_adds/en_us.json");

    private GuideBookEntries() {
        this.guidebookPaths = new ArrayList<ResourceLocation>();
        this._entries = new HashMap<String, GuidebookEntry>();
        this.guidebookPaths.add(ResourceLocation.fromNamespaceAndPath("mna", "guide"));
        NeoForge.EVENT_BUS.post(new RegisterGuidebooksEvent(this));
    }

    public GuidebookEntry getEntry(String id) {
        return this._entries.get(id);
    }

    private boolean playerHasAdvancement(ResourceLocation advancementID, LocalPlayer player) {
        if (advancementID == null) {
            return true;
        }
        try {
            ClientAdvancements clientAdvancements = player.connection.getAdvancements();
            Advancement adv = clientAdvancements.getAdvancements().get(advancementID);
            if (adv == null) {
                return false;
            }
            AdvancementProgress advProgress = (AdvancementProgress)clientAdvancements.progress.get(adv);
            if (advProgress != null) {
                return advProgress.isDone();
            }
        }
        catch (Exception ex) {
            ManaAndArtifice.LOGGER.warn("GuideBookEntries >> Error checking advancement completion on client!");
        }
        return true;
    }

    public Collection<GuidebookEntry> getEntries(EntryCategory category) {
        ArrayList<GuidebookEntry> matches = new ArrayList<GuidebookEntry>();
        for (GuidebookEntry e : this._entries.values()) {
            if (e.getCategory() != category || !this.playerHasAdvancement(e.getRequiredAdvancement(), Minecraft.getInstance().player)) continue;
            matches.add(e);
        }
        return matches;
    }

    public List<Map.Entry<String, GuidebookEntry>> getAllEntries() {
        return this._entries.entrySet().stream().filter(e -> this.playerHasAdvancement(((GuidebookEntry)e.getValue()).getRequiredAdvancement(), Minecraft.getInstance().player)).collect(Collectors.toList());
    }

    public List<GuidebookSearchResult> searchEntries(String query, int tier, int limit) {
        String lowerQuery = query.toLowerCase();
        Minecraft m = Minecraft.getInstance();
        List matchingContents = this._entries.values().stream().filter(e -> e.getTier() <= tier && this.playerHasAdvancement(e.getRequiredAdvancement(), m.player)).filter(e -> e.getSections().stream().anyMatch(s -> s instanceof TextSection && ((TextSection)s).getRawText().toLowerCase().contains(lowerQuery))).collect(Collectors.toList());
        HashMap relatedRecipes = new HashMap();
        this._entries.values().forEach(e -> e.getRelatedRecipes().stream().filter(r -> r.getTier() <= tier).forEach(r -> {
            block0: for (ItemStack is : r.getOutputItems((Level)m.level)) {
                if (is.isEmpty()) continue;
                if (is.isEnchanted()) {
                    Map map = EnchantmentUtils.enchantmentMap((ItemStack)is);
                    for (Object __e : map.entrySet()) {
                    Map.Entry ench = (Map.Entry)__e;
                        if (!Enchantment.getFullname((net.minecraft.core.Holder<Enchantment>)ench.getKey(), ((Integer)ench.getValue()).intValue()).getString().toLowerCase().contains(lowerQuery)) continue;
                        relatedRecipes.put(r, is);
                        continue block0;
                    }
                    continue;
                }
                if (!is.getHoverName().getString().toLowerCase().contains(lowerQuery)) continue;
                relatedRecipes.put(r, is);
                break;
            }
        }));
        List results = relatedRecipes.entrySet().stream().map((Map.Entry r) -> new GuidebookSearchResult((RelatedRecipe)r.getKey(), (ItemStack)r.getValue())).collect(Collectors.toList());
        results.addAll(matchingContents.stream().map(e -> new GuidebookSearchResult((GuidebookEntry)e)).collect(Collectors.toList()));
        return results.stream().filter(this.distinctByKey(GuidebookSearchResult::toString)).limit(limit).collect(Collectors.toList());
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        ConcurrentHashMap.KeySetView seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private void readEntries(ResourceManager resourceManager, ResourceLocation rLoc) {
        try {
            BufferedReader reader;
            InputStream in;
            Optional res = resourceManager.getResource(rLoc);
            if (res.isPresent() && (in = ((Resource)res.get()).open()) != null && (reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) != null) {
                JsonElement obj = JsonParser.parseReader((Reader)reader);
                if (obj != null && obj.isJsonObject()) {
                    this.parseJSON(obj.getAsJsonObject(), rLoc.getNamespace());
                }
                try {
                    reader.close();
                }
                catch (IOException iOException) {}
            }
        }
        catch (IOException ex) {
            ManaAndArtifice.LOGGER.catching((Throwable)ex);
        }
    }

    private void parseJSON(JsonObject obj, String modid) {
        for (Map.Entry elem : obj.getAsJsonObject().entrySet()) {
            if (!((JsonElement)elem.getValue()).isJsonObject()) continue;
            this._entries.put((String)elem.getKey(), new GuidebookEntry(((JsonElement)elem.getValue()).getAsJsonObject(), (String)elem.getKey(), modid));
        }
    }

    private void readAndParseGuidebooks(ResourceManager resourceManager) {
        Minecraft mc = Minecraft.getInstance();
        for (ResourceLocation rLoc : this.guidebookPaths) {
            if (!mc.options.languageCode.equalsIgnoreCase("en_us")) {
                ResourceLocation rLoc_localized = ResourceLocation.fromNamespaceAndPath(rLoc.getNamespace(), rLoc.getPath() + "/en_us.json");
                this.readEntries(resourceManager, rLoc_localized);
                ResourceLocation rLoc_packAdds = ResourceLocation.fromNamespaceAndPath(rLoc.getNamespace(), rLoc.getPath() + "/pack_adds/en_us.json");
                this.readEntries(resourceManager, rLoc_packAdds);
            }
            String guidebook_localized = String.format("%s.json", mc.options.languageCode);
            ResourceLocation rLoc_localized = ResourceLocation.fromNamespaceAndPath(rLoc.getNamespace(), rLoc.getPath() + "/" + guidebook_localized);
            this.readEntries(resourceManager, rLoc_localized);
            ResourceLocation rLoc_packAdds = ResourceLocation.fromNamespaceAndPath(rLoc.getNamespace(), rLoc.getPath() + "/pack_adds/" + guidebook_localized);
            this.readEntries(resourceManager, rLoc_packAdds);
        }
    }

    public void reload() {
        Minecraft mc = Minecraft.getInstance();
        if (this.hasLoaded && this.lastLanguage == mc.options.languageCode && mc.isEnforceUnicode() == this.lastUnicode) {
            return;
        }
        ResourceManager resourceManager = mc.getResourceManager();
        this._entries.clear();
        this.readAndParseGuidebooks(resourceManager);
        this.hasLoaded = true;
        this.lastLanguage = mc.options.languageCode;
        this.lastUnicode = mc.isEnforceUnicode();
    }

    public void updateUnlockedEntries(@Nullable ResourceLocation unlockedAdvancementID) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> {
            int lastCount = progression == null ? 0 : progression.unlockedEntryCount();
            List unlocked = this._entries.entrySet().stream().filter(e -> {
                if (unlockedAdvancementID == null) {
                    return this.playerHasAdvancement(((GuidebookEntry)e.getValue()).getRequiredAdvancement(), mc.player);
                }
                return ((GuidebookEntry)e.getValue()).getRequiredAdvancement() == null || unlockedAdvancementID.equals(((GuidebookEntry)e.getValue()).getRequiredAdvancement());
            }).map(e -> (String)e.getKey()).collect(Collectors.toList());
            if (unlocked.size() > lastCount) {
                long[] entryHashes = new long[unlocked.size()];
                int count = 0;
                for (String s : unlocked) {
                    entryHashes[count++] = s.hashCode();
                }
                ClientMessageDispatcher.updateCodexEntryUnlocks(entryHashes);
                ManaAndArtifice.instance.proxy.showCodexEntriesUnlocked();
            }
        });
    }

    @Override
    public void addGuidebookPath(ResourceLocation resourceLocation) {
        this.guidebookPaths.add(resourceLocation);
    }

    @Override
    public void registerRecipeRenderer(String recipe_type, Class<? extends RecipeRendererBase> clazz) {
        RecipeRenderers.registerRecipeRenderer(recipe_type, clazz);
    }

    @Override
    public void registerGuidebookCategory(String id, ResourceLocation icon) {
        EntryCategory.Register(id, icon);
    }

    public class GuidebookSearchResult {
        private GuidebookEntry _entry;
        private RelatedRecipe _recipe;
        private ItemStack _output;

        public GuidebookSearchResult(GuidebookEntry entry) {
            this._entry = entry;
            this._output = ItemStack.EMPTY;
        }

        public GuidebookSearchResult(RelatedRecipe recipe, ItemStack output) {
            this._recipe = recipe;
            this._output = output;
        }

        @Nullable
        public GuidebookEntry getEntry() {
            return this._entry;
        }

        public boolean isEntry() {
            return this._entry != null;
        }

        @Nullable
        public RelatedRecipe getRecipe() {
            return this._recipe;
        }

        public ItemStack getOutputStack() {
            return this._output;
        }

        public boolean isRecipe() {
            return this._recipe != null;
        }

        public boolean equals(Object obj) {
            if (obj instanceof GuidebookSearchResult) {
                GuidebookSearchResult other = (GuidebookSearchResult)obj;
                if (this.isEntry() && other.isEntry()) {
                    return this.getEntry().equals(other.getEntry());
                }
                if (this.isRecipe() && other.isRecipe()) {
                    return this.getRecipe().equals(other.getRecipe());
                }
            }
            return false;
        }

        public String toString() {
            if (this.isEntry()) {
                return this._entry.toString();
            }
            return this._recipe.toString();
        }
    }
}


