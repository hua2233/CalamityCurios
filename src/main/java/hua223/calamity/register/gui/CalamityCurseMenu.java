package hua223.calamity.register.gui;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.net.C2SPacket.SpellTypeSync;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import net.minecraft.core.NonNullList;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CalamityCurseMenu extends AbstractContainerMenu {
    public static final int VANILLA_FIRST_SLOT_INDEX = 0;
    public static final int CURSE_ENCHANTMENT_SLOT = 36;
    public final Player player;
    public SpellType type;

    public ItemStack[] spend;
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
            } else {
                if (!isExhumed(stack)) {
                    spend = new ItemStack[]{CalamityItems.BRIMSTONE_LOCUS.get().getDefaultInstance()};

                    if (isClient) {
                        SpellType spell = SpellType.getCanApply(stack);
                        if (spell != null) {
                            type = spell;
                            NetMessages.sendToServer(new SpellTypeSync(type.name()));
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
        this(RegisterMenuType.CALAMITY_CURES.get(), id, inventory.player);
    }

    private static boolean matching(ItemStack stack, NonNullList<ItemStack> stacks, Map<Integer, Integer> slotChange) {
        Item item = stack.getItem();
        int count = stack.getCount();

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack1 = stacks.get(i);
            if (stack1.is(item)) {
                int c = stack1.getCount();
                if (c > count) {
                    int difference = c - count;
                    slotChange.put(i, difference);
                    return true;
                } else {
                    count -= c;
                    slotChange.put(i, 0);
                }
            }
        }

        return count <= 0;
    }

    public void setCurseItemChanged(ItemStack stack) {
        curseSlot.setStackInSlot(0, stack);
    }

    public void setShareRenderTag(ItemStack stack, String spell) {
        stack.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).ifPresent(enchantment -> {
            if (enchantment.isEffective())
                Objects.requireNonNullElseGet(stack.getShareTag(), stack::getOrCreateTag)
                    .putString(EnchantmentProvider.FONT_FLAG, spell);
        });
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
    public void removed(Player player) {
        ItemStack stack = curseSlot.getStackInSlot(0);
        if (!stack.isEmpty()) player.getInventory().add(stack);
    }

    public void addPlayerInventory(Inventory inventory) {
        int x = 115;
        int y = 200;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
    }

    public void addPlayerHotbar(Inventory inventory) {
        int x = 115;
        int y = 258;
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y));
        }
    }

    private boolean isExhumed(ItemStack stack) {
        List<CalamityCurseRecipe> recipes = player.level.getRecipeManager()
            .getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);

        for (CalamityCurseRecipe recipe : recipes) {
            CalamityCurseRecipe r = recipe.returnIfMatching(stack);
            if (r != null) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();

                ItemStack[] s = new ItemStack[1 + ingredients.size()];
                s[0] = CalamityItems.BRIMSTONE_LOCUS.get().getDefaultInstance();

                for (int i = 0; i < ingredients.size(); i++) {
                    s[i + 1] = ingredients.get(i).getItems()[0];
                }

                type = SpellType.EXHUMED;
                spend = s;
                if (isClient) {
                    screen.canRenderContent = true;
                } else {
                    isExhumed = true;
                    result = recipe.getResultItem();
                    reactantCount = recipe.getReactant().getCount();
                }

                return true;
            }
        }

        return false;
    }

    private boolean hasBeenCursed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(EnchantmentProvider.FONT_FLAG);
    }

    public boolean isEffectiveSlot() {
        if (isClient) {
            return screen.canRenderContent;
        } else {
            ItemStack stack = curseSlot.getStackInSlot(0);
            return !stack.isEmpty() && stack.getCount() >= reactantCount;
        }
    }

    public Map<Integer, Integer> synthesis() {
        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> stacks = inventory.items;
        Map<Integer, Integer> slotChange = new HashMap<>();

        for (ItemStack ingredient : spend) {
            if (!matching(ingredient, stacks, slotChange)) return null;
        }

        return slotChange;
    }
}
