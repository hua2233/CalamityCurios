package hua223.calamity.net.S2CPacket;

import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class ExcelsusHit extends S2C {
    private final int id;

    public ExcelsusHit(LivingEntity target) {
        id = target.getId();
    }

    @OnlyIn(Dist.CLIENT)
    public ExcelsusHit(FriendlyByteBuf buf) {
        id = buf.readVarInt();
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnParticle(int count, ParticleEngine engine, Vec3 start, Vec3 section, RandomSource random, boolean isReverse) {
        float r1 = 140 / 255f;
        float g1 = 239 / 255f;
        float b1 = 254 / 255f;
        float r2 = 229 / 255f;
        float g2 = 30 / 255f;
        float b2 = 202 / 255f;

        for (int i = 0; i < count; i++) {
            double x = section.x * i;
            double y = section.y * i;
            double z = section.z * i;
            if (isReverse) {
                x = -x;
                y = -y;
                z = -z;
            }

            //it shouldn't escape
            Particle particle = engine.makeParticle(ParticleTypes.END_ROD, start.x + x, start.y + y,
                start.z + z, 0d, 0d, 0d);

            if (particle != null) {
                particle.setLifetime(10);
                particle.gravity = 0f;
                particle.scale(0.6f);
                if (random.nextFloat() > 0.33) particle.setColor(r1, g1, b1);
                else particle.setColor(r2, g2, b2);
                engine.add(particle);
            }
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity target = level.getEntity(id);
            if (target != null && target.isAlive()) {
                ParticleEngine engine = Minecraft.getInstance().particleEngine;
                RandomSource random = level.getRandom();
                Vector2d offset = Vector2d.nextVector2Circular(3f, 3f, random);
                Vec3 start = target.getEyePosition();
                level.playLocalSound(start.x, start.y, start.z, CalamitySounds.EXCELSUS_RAY.get(), SoundSource.AMBIENT, 1f, 1f, false);

                Vec3 distance = new Vec3(offset.x, random.nextInt(4, 5) + random.nextFloat(), offset.y);
                double length = distance.length();
                int sectionLength = (int) (length / 0.15);
                Vec3 section = new Vec3(distance.x / sectionLength, distance.y / sectionLength, distance.z / sectionLength);
                int oppositeDirection = (int) (sectionLength * 0.2f) + 1;
                sectionLength -= oppositeDirection;


                spawnParticle(sectionLength, engine, start, section, random, false);
                //Pass through the target a little bit to prevent the end point from being in the model
                spawnParticle(oppositeDirection, engine, start, section, random, true);
            }
        }
    }
}
