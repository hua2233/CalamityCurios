package hua223.calamity.register.entity;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//I wonder if there are any entities that don't need an EntityRenderer，this doesn't t have to be rendered at all...
public class UniverseSplitterField extends Entity {
    private Player player;
    private LivingEntity target;

    @OnlyIn(Dist.CLIENT)
    private static final float SPIRAL_PRECISION = 25f;
    @OnlyIn(Dist.CLIENT)
    private static final float SPIRAL_RINGS = 5f;
    @OnlyIn(Dist.CLIENT)
    private final int TIME_LEFT = 240;
    @OnlyIn(Dist.CLIENT)
    private final Vector2d pos = new Vector2d(getX(), getY());
    @OnlyIn(Dist.CLIENT)
    private final ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
    @OnlyIn(Dist.CLIENT)
    private float dustRadius;

    public UniverseSplitterField(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public static void create(Level level, Player player, LivingEntity entity) {
        Vec3 pos;
        UniverseSplitterField field = CalamityEntity.USF.get().create(level);

        if (entity != null) {
            pos = entity.position().add(0, entity.getBoundingBox().getYsize() / 2, 0);
            CalamityHelp.setCalamityFlag(entity, 2, true);
        } else pos = player.getEyePosition().add(player.getLookAngle().normalize().scale(20));

        field.setPos(pos);
        field.player = player;
        field.target = entity;
        level.addFreshEntity(field);
    }

    @Override
    public void tick() {
        if (tickCount > TIME_LEFT) {
            discard();
            if (target != null && target.isAlive()) CalamityHelp.setCalamityFlag(target, 2, false);
            return;
        }

        if (tickCount < 10) dustRadius = Mth.lerp(tickCount / 10f, 0, 4f);
        else dustRadius = 4f + (float) Math.sin(tickCount / 18f);

        if (level.isClientSide) generateIdleDust();
        else spawnLasers();
    }

    @Override
    public void kill() {
        super.kill();
        if (target != null && target.isAlive()) CalamityHelp.setCalamityFlag(target, 2, false);
    }

    @OnlyIn(Dist.CLIENT)
    private void generateIdleDust() {
        // Generate a dust ring that pulsates
        for (int i = 0; i < 80; i++) {
            Vector2d direction = Vector2d.toRotationVector2(i / 80f * Mth.TWO_PI).mul(dustRadius);
            Particle particle = particleEngine.makeParticle(ParticleTypes.END_ROD,
                getX() + direction.x, getY() + direction.y, getZ(), 0, 0, 0);
            if (particle != null) {
                particle.setLifetime(1);
                particle.scale(0.8f);
                particle.gravity = 0f;
                particleEngine.add(particle);
            }
        }

        // Spirals that spin around
        for (int i = 0; i < SPIRAL_PRECISION; i++) {
            for (int direction = -1; direction <= 1; direction += 2) {
                for (int j = 0; j < SPIRAL_RINGS; j++) {
                    Vector2d spawnPos = Vector2d.NUNIT_Y.rotatedBy(tickCount / SPIRAL_PRECISION * direction, pos, false)
                        .rotatedBy(j / SPIRAL_RINGS * Mth.TWO_PI, pos, true)
                        .rotatedBy(i / SPIRAL_PRECISION * Mth.TWO_PI / SPIRAL_RINGS * direction, pos, true)
                        .mul(dustRadius * i / SPIRAL_PRECISION);

                    Particle particle = particleEngine.makeParticle(ParticleTypes.END_ROD,
                        getX() + spawnPos.x, getY() + spawnPos.y, getZ(), 0, 0, 0);

                    if (particle != null) {
                        particle.setLifetime(1);
                        particle.scale(0.45f);
                        particle.gravity = 0f;
                        particleEngine.add(particle);
                    }
                }
            }
        }

        // Outward expansion of dust
        boolean firingGiantLaserBeam = tickCount > TIME_LEFT - 60;
        for (int i = 0; i < (firingGiantLaserBeam ? 30 : 16); i++) {
            float scale = dustRadius / 5;
            Vector2d velocity = Vector2d.nextVector2Circular(scale, scale, level.random);
            Particle particle = particleEngine.makeParticle(ParticleTypes.END_ROD,
                getX(), getY(), getZ(), velocity.x, velocity.y, 0);
            if (particle != null) {
                particle.scale(0.2f);
                particle.setLifetime(20);
                particle.gravity = 0f;
                particleEngine.add(particle);
            }
        }

        // Energy dust that appears before the giant beam does and remains
        if (tickCount > TIME_LEFT - 100) {
            float outwardCircleRadius = Mth.lerp(Mth.clamp((tickCount -
                (TIME_LEFT - 100)) / 14f, 0f, 1f), 0f, dustRadius * 1.2f);
            for (int i = 0; i < 95; i++) {
                Vector2d pos = Vector2d.toRotationVector2(i / 95f * Mth.TWO_PI).mul(outwardCircleRadius).add(getX(), getY());
                Vec2 velocity = level.random.nextInt(7) == 0 ? new Vec2((float) (getX() - pos.x), (float) (getY() - pos.y)).normalized() : Vec2.ZERO;
                Particle particle = particleEngine.makeParticle(ParticleTypes.END_ROD,
                    pos.x, pos.y, getZ(), velocity.x, velocity.y, 0);

                if (particle != null) {
                    particle.scale(0.8f);
                    particle.setLifetime(1);
                    particle.gravity = 0f;
                    particleEngine.add(particle);
                }
            }
        }
    }

    private void spawnLasers() {
        // Create small beams sometimes
        if (tickCount > 40 &&
            tickCount < TIME_LEFT - 60 &&
            tickCount % 30 == 0f) {
            Vector2d pos = new Vector2d(random.nextInt(-10, 10) + random.nextFloat(), 20f);
            UniverseSplitterSmallBeam beam = CalamityEntity.USB.get().create(level);
            beam.owner = player;
            beam.setPos(getX() + pos.x, getY() + pos.y, getZ());
            beam.velocity = new Vector2d(-pos.x, -pos.y).normalize(true);
            beam.ai = beam.velocity.toRotation();
            level.addFreshEntity(beam);
            level.playSound(null, this, CalamitySounds.PLASMA_BOLT.get(), SoundSource.AMBIENT, 4f, 1f);
        }

        // Summon a giant beam
        if (tickCount == TIME_LEFT - UniverseSplitterHugeBeam.TIME_LEFT) {
            float zOffset = 0f;
            if (player != null) zOffset = player.getLookAngle().z < 0 ? 0.01f : -0.01f;
            UniverseSplitterHugeBeam beam = CalamityEntity.USH.get().create(level);
            beam.owner = player;
            beam.setPos(getX(), getY() + 30, getZ() + zOffset);
            beam.setStrikePoint();
            level.addFreshEntity(beam);
            level.playSound(null, this, CalamitySounds.PHANTOM_DEATH_RAY.get(), SoundSource.AMBIENT, 5f, 1f);
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<UniverseSplitterField> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(UniverseSplitterField universeSplitterField) {
            return null;
        }

        @Override
        public boolean shouldRender(UniverseSplitterField pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
            return false;
        }
    }
}
