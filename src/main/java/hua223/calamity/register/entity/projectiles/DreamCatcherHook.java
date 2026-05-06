package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.RenderUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.function.Consumer;

//抓住你的梦!
public class DreamCatcherHook extends FishingHook implements IEntityAdditionalSpawnData {
    byte id;
    @OnlyIn(Dist.CLIENT)
    private final ResourceLocation texture = CalamityCurios.ModResource("textures/entity/dream_catcher_hook.png");
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(texture);
    public DreamCatcherHook(EntityType<? extends FishingHook> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("ALL")
    public static void spawn(Player player, Level level, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        float f = player.getXRot();
        float angle = (float) Math.PI / 180F;
        float f4 = -Mth.cos(-f * angle);
        float f5 = Mth.sin(-f * angle);
        for (int i = -1; i < 2; i++) {
            DreamCatcherHook hook = new DreamCatcherHook(CalamityEntity.DREAM_CATCHER_HOOK.get(), level);
            hook.id = (byte) (i + 2);
            tag.putUUID("DreamCatcherHook" + hook.id, hook.getUUID());
            hook.lureSpeed += 2;
            hook.luck += 2;
            hook.setOwner(player);

            float f1 = player.getYRot() + i * 20;
            float f2 = Mth.cos(-f1 * angle - (float)Math.PI);
            float f3 = Mth.sin(-f1 * angle - (float)Math.PI);
            double d0 = player.getX() - f3 * 0.3;
            double d1 = player.getEyeY();
            double d2 = player.getZ() - f2 * 0.3;
            hook.moveTo(d0, d1, d2, f1, f);
            Vec3 vec3 = new Vec3(-f3, Mth.clamp(-(f5 / f4), -5.0F, 5.0F), -f2);
            double d3 = vec3.length();
            vec3 = vec3.multiply(0.6 / d3 + hook.random.triangle(0.5, 0.0103365), 0.6 / d3 +
                hook.random.triangle(0.5, 0.0103365), 0.6 / d3 + hook.random.triangle(0.5F, 0.0103365));
            hook.setDeltaMovement(vec3);
            hook.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * angle));
            hook.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * angle));
            hook.yRotO = hook.getYRot();
            hook.xRotO = hook.getXRot();

            level.addFreshEntity(hook);
        }
    }

    @Override
    public void tick() {
        syncronizedRandom.setSeed(getUUID().getLeastSignificantBits() ^ level().getGameTime());
        Player player = getPlayerOwner();
        if (player == null) {
            discard();
        } else if (level().isClientSide || !shouldStopFishing(player)) {
            if (onGround()) {
                ++life;
                if (life >= 1200) {
                    discard();
                    return;
                }
            } else {
                life = 0;
            }

            float f = 0.0F;
            BlockPos blockpos = blockPosition();
            FluidState fluidstate = level().getFluidState(blockpos);
            if (!fluidstate.isEmpty()) f = fluidstate.getHeight(level(), blockpos);

            boolean flag = f > 0.0F;
            if (currentState == FishingHook.FishHookState.FLYING) {
                if (hookedIn != null) {
                    setDeltaMovement(Vec3.ZERO);
                    currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
                    return;
                }

                if (flag) {
                    setDeltaMovement(getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    currentState = FishingHook.FishHookState.BOBBING;
                    return;
                }

                checkCollision();
            } else {
                if (currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
                    if (hookedIn != null) {
                        if (!hookedIn.isRemoved() && hookedIn.level().dimension() == level().dimension()) {
                            setPos(hookedIn.getX(), hookedIn.getY(0.8), hookedIn.getZ());
                        } else {
                            setHookedEntity(null);
                            currentState = FishingHook.FishHookState.FLYING;
                        }
                    }

                    return;
                }

                if (currentState == FishingHook.FishHookState.BOBBING) {
                    Vec3 vec3 = getDeltaMovement();
                    double d0 = getY() + vec3.y - (double)blockpos.getY() - (double)f;
                    if (Math.abs(d0) < 0.01) {
                        d0 += Math.signum(d0) * 0.1;
                    }

                    setDeltaMovement(vec3.x * 0.9, vec3.y - d0 * (double)random.nextFloat() * 0.2, vec3.z * 0.9);
                    if (nibble <= 0 && timeUntilHooked <= 0) {
                        openWater = true;
                    } else {
                        openWater = openWater && outOfWaterTime < 10 && calculateOpenWater(blockpos);
                    }

                    if (flag) {
                        outOfWaterTime = Math.max(0, outOfWaterTime - 1);
                        if (biting) {
                            setDeltaMovement(getDeltaMovement().add(0.0F, -0.1 * (double)
                                syncronizedRandom.nextFloat() * (double)syncronizedRandom.nextFloat(), 0.0F));
                        }

                        if (!level().isClientSide) {
                            if (nibble > 0) nibble = 99;
                            catchingFish(blockpos);
                        }
                    } else {
                        outOfWaterTime = Math.min(10, outOfWaterTime + 1);
                    }
                }
            }

            if (!fluidstate.isEmpty()) setDeltaMovement(getDeltaMovement().add(0.0F, -0.03, 0.0F));

            move(MoverType.SELF, getDeltaMovement());
            updateRotation();
            if (currentState == FishingHook.FishHookState.FLYING && (onGround() || horizontalCollision))
                setDeltaMovement(Vec3.ZERO);
             else setDeltaMovement(getDeltaMovement().scale(0.92));

            reapplyPosition();
            baseTick();
        }
    }

    public static void retrieve(Player player, @NotNull ItemStack stack) {
        if (!player.level().isClientSide) {
            ServerLevel level = (ServerLevel) player.level();
            CompoundTag tag = stack.getOrCreateTag();
            LootContext lootContext = null;
            for (int i = 1; i < 4; i++) {
                String key = "DreamCatcherHook" + i;
                Entity entity = level.getEntity(tag.getUUID(key));
                if (entity != null && entity.isAlive() && entity instanceof DreamCatcherHook hook && !hook.shouldStopFishing(player)) {
                    if (hook.hookedIn != null) {
                        hook.pullEntity(hook.hookedIn);
                        CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, stack, hook, Collections.emptyList());
                        level.broadcastEntityEvent(hook, (byte)31);
                    } else if (hook.nibble > 0) {
                        if (lootContext == null) {
                            //传递一些必要的上下文参数，例如随机数提供器，函数的底层会可能访问到参数。
                            //它已经跳过了所有先决条件，如果真的有Mod在创建ItemStack时进行额外的条件检测，它可能不通过甚至抛出异常;
                            //Mojang设计的很精妙但非常不利于Hook，大量的嵌套lambda功能函数，和可以在代码中任意实现的检测机制使得它的状态机依赖于多态实现和上下文，
                            //我不可能为任何Mod的非规范战利品表进行兼容，所以抛出异常时，将声明为不兼容。
                            lootContext = (new LootContext.Builder((new LootParams.Builder((ServerLevel)hook.level()))
                                .withParameter(LootContextParams.ORIGIN, hook.position())
                                .withParameter(LootContextParams.TOOL, stack)
                                .withParameter(LootContextParams.THIS_ENTITY, hook)
                                .withParameter(LootContextParams.KILLER_ENTITY, player)
                                .withParameter(LootContextParams.THIS_ENTITY, hook)
                                .withLuck((float)hook.luck + player.getLuck())
                                .create(LootContextParamSets.FISHING))).create(null);
                        }
                        ItemStack loot = hook.getTypeItem(lootContext);
                        if (loot != ItemStack.EMPTY) {
                            ItemEntity itementity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), loot);
                            double d0 = player.getX() - hook.getX();
                            double d1 = player.getY() - hook.getY();
                            double d2 = player.getZ() - hook.getZ();
                            itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
                            level.addFreshEntity(itementity);
                            player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + (double)0.5F,
                                player.getZ() + (double)0.5F, hook.random.nextInt(6) + 1));
                            if (loot.is(ItemTags.FISHES)) player.awardStat(Stats.FISH_CAUGHT, 1);
                        }
                    }

                    hook.discard();
                    tag.remove(key);
                }
            }

        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
       return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.getOwner() == null ? 0 : this.getOwner().getId());
        buffer.writeUUID(this.getUUID());
        buffer.writeInt(this.getId());
        buffer.writeFloat(this.getYRot());
        buffer.writeFloat(this.getXRot());
        Vec3 vec3 = getDeltaMovement();
        buffer.writeDouble(vec3.x);
        buffer.writeDouble(vec3.y);
        buffer.writeDouble(vec3.z);
        buffer.writeByte(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity entity = level().getEntity(additionalData.readInt());
        if (entity == null) {
            kill();
        } else {
            setOwner(entity);
            setUUID(additionalData.readUUID());
            setId(additionalData.readInt());
            setYRot(additionalData.readFloat());
            setXRot(additionalData.readFloat());
            setDeltaMovement(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
            id = additionalData.readByte();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private ItemStack getTypeItem(LootContext context) {
        ResourceLocation lootRL = id == 1 ? BuiltInLootTables.FISHING_FISH :
            id == 2 ? BuiltInLootTables.FISHING_TREASURE : null;
        if (lootRL != null) {
            LootTable table = level().getServer().getLootData().getLootTable(lootRL);
            final ItemStack[] lootWrapper = {ItemStack.EMPTY};
            LootPool pool = table.pools.get(random.nextInt(table.pools.size()));
            LootPoolEntryContainer container = null;
            try {
                Consumer<ItemStack> lootPoolConsumer = LootItemFunction.decorate(pool.compositeFunction, stack -> lootWrapper[0] = stack, context);
                container = pool.entries[random.nextInt(pool.entries.length)];
                container.calamity$SetAbsoluteOperation = true;
                container.expand(context, lootPoolEntry -> lootPoolEntry.createItemStack(lootPoolConsumer, context));
            } catch (Exception e) {
                if (container instanceof LootItem lootItem) return lootItem.item.getDefaultInstance();
                CalamityCurios.LOGGER.warn("Incompatible loot table types {}", pool.getName());
            } finally {
                container.calamity$SetAbsoluteOperation = false;
            }

            return lootWrapper[0];
        } else {
            ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
            Enchantment[] enchantments = ForgeRegistries.ENCHANTMENTS.getValues().toArray(new Enchantment[0]);
            Enchantment enchantment = enchantments[random.nextInt(enchantments.length)];
            stack.enchant(enchantment, random.nextInt(enchantment.getMinLevel(), enchantment.getMaxLevel() + 3));
            return stack;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<DreamCatcherHook> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(@NotNull DreamCatcherHook entity, float yaw, float partialTick, @NotNull PoseStack stack,
                           @NotNull MultiBufferSource buffer, int packedLight) {
            Player player = entity.getPlayerOwner();
            if (player != null) {
                stack.pushPose();
                stack.pushPose();
                stack.scale(0.5F, 0.5F, 0.5F);
                stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                PoseStack.Pose posestack$pose = stack.last();
                Matrix4f matrix4f = posestack$pose.pose();
                Matrix3f matrix3f = posestack$pose.normal();
                VertexConsumer vertexconsumer = buffer.getBuffer(entity.type);
                float v1 = entity.id * 0.3333f;
                float v = v1 - 0.3333f;
                vertex(vertexconsumer, matrix4f, matrix3f,0.0f, 0f, 0, v1);
                vertex(vertexconsumer, matrix4f, matrix3f, 1.0f, 0f, 1, v1);
                vertex(vertexconsumer, matrix4f, matrix3f,  1.0f, 1.2f, 1, v);
                vertex(vertexconsumer, matrix4f, matrix3f, 0.0f, 1.2f, 0, v);
                stack.popPose();
                int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
                ItemStack itemstack = player.getMainHandItem();
                if (!itemstack.canPerformAction(ToolActions.FISHING_ROD_CAST)) {
                    i = -i;
                }

                float f = player.getAttackAnim(partialTick);
                float f1 = Mth.sin(Mth.sqrt(f) * (float)Math.PI);
                float f2 = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180F);
                double d0 = Mth.sin(f2);
                double d1 = Mth.cos(f2);
                double d2 = (double)i * 0.35;
                double d4;
                double d5;
                double d6;
                float f3;
                if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
                    double d7 = (double)960.0F / (double) this.entityRenderDispatcher.options.fov().get();
                    Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)i * 0.525F, -0.1F);
                    vec3 = vec3.scale(d7);
                    vec3 = vec3.yRot(f1 * 0.5F);
                    vec3 = vec3.xRot(-f1 * 0.7F);
                    d4 = Mth.lerp(partialTick, player.xo, player.getX()) + vec3.x;
                    d5 = Mth.lerp(partialTick, player.yo, player.getY()) + vec3.y;
                    d6 = Mth.lerp(partialTick, player.zo, player.getZ()) + vec3.z;
                    f3 = player.getEyeHeight();
                } else {
                    d4 = Mth.lerp(partialTick, player.xo, player.getX()) - d1 * d2 - d0 * 0.8;
                    d5 = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)partialTick - 0.45;
                    d6 = Mth.lerp(partialTick, player.zo, player.getZ()) - d0 * d2 + d1 * 0.8;
                    f3 = player.isCrouching() ? -0.1875F : 0.0F;
                }

                double d9 = Mth.lerp(partialTick, entity.xo, entity.getX());
                double d10 = Mth.lerp(partialTick, entity.yo, entity.getY()) + 0.25;
                double d8 = Mth.lerp(partialTick, entity.zo, entity.getZ());
                float f4 = (float)(d4 - d9);
                float f5 = (float)(d5 - d10) + f3;
                float f6 = (float)(d6 - d8);
                VertexConsumer consumer = buffer.getBuffer(RenderType.lineStrip());
                PoseStack.Pose pose1 = stack.last();
                float cosineWave = (float) Math.cos(Mth.TWO_PI * entity.id / 3f + RenderUtil.getLocalTick() * 0.05f);
                int g = Mth.lerpInt(cosineWave, 105, 255);
                int b = Mth.lerpInt(cosineWave, 180, 255);
                for(int k = 0; k <= 16; ++k)
                    stringVertex(f4, f5, f6, g, b, consumer, pose1,k / 16f, k + 1 / 16f);

                stack.popPose();
            }
        }

        private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float u, float v) {
            consumer.vertex(pose, x / 2f, y / 2f, 0.0F).color(255, 255, 255, 255).
                uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        }

        private static void stringVertex(float x, float y, float z, int g, int b, VertexConsumer consumer, PoseStack.Pose pose, float w, float h) {
            float f = x * w;
            float f1 = y * (w * w + w) * 0.5F + 0.25F;
            float f2 = z * w;
            float f3 = x * h - f;
            float f4 = y * (h * h + h) * 0.5F + 0.25F - f1;
            float f5 = z * h - f2;
            float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
            f3 /= f6;
            f4 /= f6;
            f5 /= f6;
            consumer.vertex(pose.pose(), f, f1, f2).color(255, g, b, 255).normal(pose.normal(), f3, f4, f5).endVertex();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull DreamCatcherHook entity) {
            return entity.texture;
        }
    }
}
