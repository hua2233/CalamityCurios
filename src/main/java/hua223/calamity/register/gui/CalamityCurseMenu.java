package hua223.calamity.register.gui;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.net.packets.SpellTypeSync;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import hua223.calamity.util.CalamityHelp;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CalamityCurseMenu extends AbstractContainerMenu {
    public static final int VANILLA_FIRST_SLOT_INDEX = 0;
    public static final int CURSE_ENCHANTMENT_SLOT = 36;
    public final Player player;
    public SpellType type;

    public ItemStack[] spend;
    @OnlyIn(Dist.CLIENT)
    public boolean[] enough;
    public ItemStack result;
    public int reactantCount;
    public boolean isExhumed;
    public boolean isClient;

    @OnlyIn(Dist.CLIENT)
    public CalamityCurseScreen screen;

    private final ItemStackHandler curseSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            if (stack.isEmpty() || hasBeenCursed(stack)) {
                type = null;
                spend = null;

                if (isClient) {
                    screen.notEnableButtonState();
                    screen.canRenderContent = false;
                }
            } else if (!isExhumed(stack)) {
                spend = new ItemStack[]{CalamityItems.BRIMSTONE_LOCUS.get().getDefaultInstance()};

                if (isClient) {
                    SpellType spell = SpellType.getCanApply(stack);
                    if (spell != null) {
                        type = spell;
                        NetMessages.sendToServer(new SpellTypeSync(type.name()));
                        checkCostSituation();
                        screen.canRenderContent = true;
                        if (SpellType.isCanSwitch) screen.initButtonState();
                        else screen.notEnableButtonState();
                    } else {
                        screen.notEnableButtonState();
                        screen.canRenderContent = false;
                    }
                } else {
                    isExhumed = false;
                    reactantCount = 1;
                }
            }
        }
    };

    public CalamityCurseMenu(@Nullable MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        Inventory inventory = player.getInventory();
        checkContainerSize(inventory, 1);

        this.player = player;
        isClient = player.isLocalPlayer();

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        this.addSlot(new SlotItemHandler(curseSlot, 0, 115, 61));
    }

    public CalamityCurseMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(RegisterList.CALAMITY_CURES.get(), id, inventory.player);
    }

    @OnlyIn(Dist.CLIENT)
    private void checkCostSituation() {
        enough = new boolean[spend.length];
        NonNullList<Slot> slots = this.slots;
        for (int i = 0; i < spend.length; i++) {
            ItemStack stack = spend[i];
            Item item = stack.getItem();
            int count = stack.getCount();
            for (int j = 0; j < slots.size() - 1; j++) {
                ItemStack stack1 = slots.get(j).getItem();
                if (stack1.is(item)) {
                    int c = stack1.getCount();
                    if (c > count) {
                        enough[i] = true;
                    } else count -= c;
                }
            }
        }
    }

    public void setCurseItemChanged(ItemStack stack) {
        curseSlot.setStackInSlot(0, stack);
    }

    public void setShareRenderTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CalamityHelp.FONT_FLAG, 1);
        tag.putString("spell", type.name());
    }

    public ItemStack getCurseSlotItem() {
        return curseSlot.getStackInSlot(0);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceItem = slot.getItem();

        if (index < CURSE_ENCHANTMENT_SLOT) {
            if (!moveItemStackTo(sourceItem, CURSE_ENCHANTMENT_SLOT, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index == CURSE_ENCHANTMENT_SLOT) {
            if (!moveItemStackTo(sourceItem, VANILLA_FIRST_SLOT_INDEX, CURSE_ENCHANTMENT_SLOT, false))
                return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceItem.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, sourceItem);
        return sourceItem.copy();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public void removed(@NotNull Player player) {
        ItemStack stack = curseSlot.getStackInSlot(0);
        if (!stack.isEmpty()) player.getInventory().add(stack);
    }

    public void addPlayerInventory(Inventory inventory) {
        int x = 115;
        int y = 200;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
    }

    public void addPlayerHotbar(Inventory inventory) {
        int x = 115;
        int y = 258;
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inventory, i, x + i * 18, y));
    }

    private boolean isExhumed(ItemStack stack) {
        List<CalamityCurseRecipe> recipes = player.level().getRecipeManager()
            .getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);

        for (CalamityCurseRecipe recipe : recipes) {
            if (recipe.matching(stack)) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();

                ItemStack[] s = new ItemStack[1 + ingredients.size()];
                s[0] = CalamityItems.BRIMSTONE_LOCUS.get().getDefaultInstance();

                for (int i = 0; i < ingredients.size(); i++) {
                    s[i + 1] = ingredients.get(i).getItems()[0];
                }

                type = SpellType.EXHUMED;
                spend = s;
                if (isClient) {
                    checkCostSituation();
                    screen.canRenderContent = true;
                } else {
                    isExhumed = true;
                    result = recipe.assemble(null, null);
                    reactantCount = recipe.getReactant().getCount();
                }

                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean hasBeenCursed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(CalamityHelp.FONT_FLAG);
    }

    public boolean isEffectiveSlot() {
        if (isClient) {
            return screen.canRenderContent;
        } else {
            ItemStack stack = curseSlot.getStackInSlot(0);
            return !stack.isEmpty() && stack.getCount() >= reactantCount;
        }
    }

    public Int2IntMap synthesis() {
        Int2IntMap slotChange = new Int2IntArrayMap();

        loop: for (ItemStack ingredient : spend) {
            Item item = ingredient.getItem();
            int count = ingredient.getCount();

            for (int i = 0; i < slots.size() - 1; i++) {
                ItemStack stack1 = slots.get(i).getItem();
                if (stack1.is(item)) {
                    int c = stack1.getCount();
                    if (c > count) {
                        slotChange.put(i, c - count);
                        continue loop;
                    } else {
                        count -= c;
                        slotChange.put(i, 0);
                    }
                }
            }


            if (count > 0) return null;
        }

        return slotChange;
    }
}
