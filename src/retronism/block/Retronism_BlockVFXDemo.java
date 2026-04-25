package retronism.block;

import net.minecraft.src.*;
import aero.particlelib.*;

/**
 * VFX Demo Block — showcases the Aero ParticleLib effects.
 *
 * Right-click to cycle through demo effects:
 *   0: Glow orbs rising (ChromatiCraft-style)
 *   1: Spiral vortex with attraction
 *   2: Energy beam to the sky
 *   3: Lightning bolt burst
 *   4: Ring burst explosion
 *   5: Floating seeds (organic drift)
 *   6: Ember fountain (fire particles)
 *   7: Color gradient trail
 *   8: Combined: orbital + noise + beam
 *
 * Shift+right-click to go backwards.
 * The block also emits ambient particles while placed.
 */
public class Retronism_BlockVFXDemo extends Block {

    private static final int TOTAL_DEMOS = 9;
    private static final String[] DEMO_NAMES = {
        "Glow Orbs", "Spiral Vortex", "Sky Beam", "Lightning",
        "Ring Burst", "Floating Seeds", "Ember Fountain",
        "Color Trail", "Combined FX"
    };

    public Retronism_BlockVFXDemo(int id, int textureIndex) {
        super(id, textureIndex, Material.iron);
        setLightValue(0.8f);
    }

    public boolean blockActivated(World world, int x, int y, int z,
                                   EntityPlayer player) {
        if (world.multiplayerWorld) return true;

        int meta = world.getBlockMetadata(x, y, z);
        if (player.isSneaking()) {
            meta = (meta - 1 + TOTAL_DEMOS) % TOTAL_DEMOS;
        } else {
            meta = (meta + 1) % TOTAL_DEMOS;
        }
        world.setBlockMetadataWithNotify(x, y, z, meta);

        // Trigger the effect
        spawnEffect(world, x + 0.5, y + 1.2, z + 0.5, meta);

        // Chat feedback
        player.addChatMessage("§eVFX Demo: §f" + DEMO_NAMES[meta]
            + " §7(" + (meta + 1) + "/" + TOTAL_DEMOS + ")");
        return true;
    }

    /** Ambient particles while block exists. */
    public void randomDisplayTick(World world, int x, int y, int z,
                                   java.util.Random rand) {
        if (!Aero_ParticleTextures.isReady()) return;

        // Subtle ambient glow
        double px = x + 0.3 + rand.nextFloat() * 0.4;
        double py = y + 1.0 + rand.nextFloat() * 0.3;
        double pz = z + 0.3 + rand.nextFloat() * 0.4;

        int meta = world.getBlockMetadata(x, y, z);
        float hue = (meta * 40f) % 360f;
        float[] rgb = hueToRGB(hue);

        Aero_ParticleBuilder.create(world, px, py, pz)
            .velocity(0, 0.01, 0)
            .color(rgb[0], rgb[1], rgb[2])
            .scale(0.1f, 0f)
            .lifetime(25)
            .blend(Aero_BlendMode.GLOW)
            .texture(Aero_ParticleTextures.SOFT_GLOW)
            .fullBright()
            .spawn();
    }

    private void spawnEffect(World world, double x, double y, double z, int effect) {
        if (!Aero_ParticleTextures.isReady()) return;

        switch (effect) {
            case 0: demoGlowOrbs(world, x, y, z); break;
            case 1: demoSpiralVortex(world, x, y, z); break;
            case 2: demoSkyBeam(world, x, y, z); break;
            case 3: demoLightning(world, x, y, z); break;
            case 4: demoRingBurst(world, x, y, z); break;
            case 5: demoFloatingSeeds(world, x, y, z); break;
            case 6: demoEmberFountain(world, x, y, z); break;
            case 7: demoColorTrail(world, x, y, z); break;
            case 8: demoCombined(world, x, y, z); break;
        }
    }

    // -----------------------------------------------------------------------
    // Demo effects
    // -----------------------------------------------------------------------

    /** 0: Glowing orbs rising — classic magic mod look. */
    private void demoGlowOrbs(World world, double x, double y, double z) {
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterSphere(x, y, z, 0.3f, true))
            .burst(25)
            .particleColor(0.3f, 0.6f, 1.0f)
            .particleColorEnd(0.8f, 0.9f, 1.0f)
            .particleScale(0.25f, 0.0f)
            .particleLifetime(30, 50)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
            .particleFullBright(true)
            .particleGravity(-0.003f)
            .particleDrag(0.97f)
            .velocitySpread(0.02f, 0.04f, 0.02f);
        Aero_ParticleManager.addEmitter(em);
    }

    /** 1: Spiral vortex pulling particles inward. */
    private void demoSpiralVortex(World world, double x, double y, double z) {
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterRing(x, y, z, 2.5f))
            .rate(4)
            .lifetime(60)
            .particleColor(0.9f, 0.2f, 1.0f)
            .particleColorEnd(0.3f, 0.1f, 0.6f)
            .particleScale(0.2f, 0.0f)
            .particleLifetime(30, 45)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
            .particleFullBright(true)
            .particleMotion(Aero_MotionComposite.of(
                new Aero_MotionAttract(x, y + 1.0, z, 0.04f),
                new Aero_MotionOrbital(x, y, z, 0.15f, 2.5f).radiusShrink(0.96f).verticalSpeed(0.02f)
            ));
        Aero_ParticleManager.addEmitter(em);
    }

    /** 2: Beam of light shooting into the sky. */
    private void demoSkyBeam(World world, double x, double y, double z) {
        Aero_BeamBuilder.create(world)
            .from(x, y, z)
            .to(x, y + 15, z)
            .width(0.2f)
            .color(0.2f, 0.8f, 1.0f)
            .alpha(0.7f)
            .blend(Aero_BlendMode.GLOW)
            .texture(Aero_ParticleTextures.FLAT_SQUARE)
            .core(0.06f, 1f, 1f, 1f)
            .coreAlpha(0.5f)
            .uvScroll(0.08f)
            .lifetime(60)
            .fadeOut(15)
            .spawn();

        // Particles at the base
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterRing(x, y, z, 0.4f).outwardSpeed(-0.02f))
            .rate(3)
            .lifetime(55)
            .particleColor(0.4f, 0.9f, 1.0f)
            .particleScale(0.15f, 0.0f)
            .particleLifetime(15, 25)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
            .particleFullBright(true)
            .particleGravity(-0.01f);
        Aero_ParticleManager.addEmitter(em);
    }

    /** 3: Lightning bolt burst in random directions. */
    private void demoLightning(World world, double x, double y, double z) {
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 5; i++) {
            double ex = x + (r.nextFloat() * 2 - 1) * 4;
            double ey = y + (r.nextFloat() * 2 - 1) * 3;
            double ez = z + (r.nextFloat() * 2 - 1) * 4;

            Aero_BoltBuilder.create(world)
                .from(x, y, z)
                .to(ex, ey, ez)
                .segments(8 + r.nextInt(5))
                .spread(0.6f)
                .branches(1 + r.nextInt(3))
                .branchLength(0.35f)
                .color(0.6f, 0.7f, 1.0f)
                .lineWidth(2.0f)
                .reRandomize(2)
                .lifetime(12 + r.nextInt(8))
                .spawn();
        }

        // Bright flash at origin
        Aero_ParticleBuilder.create(world, x, y, z)
            .color(0.8f, 0.9f, 1.0f)
            .scale(2.0f, 0.0f)
            .scaleCurve(Aero_Curve.EASE_OUT)
            .alpha(1f, 0f)
            .lifetime(6)
            .blend(Aero_BlendMode.BRIGHT)
            .texture(Aero_ParticleTextures.SOFT_GLOW)
            .fullBright()
            .spawn();
    }

    /** 4: Ring burst explosion. */
    private void demoRingBurst(World world, double x, double y, double z) {
        // Horizontal ring
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterRing(x, y, z, 0.5f).outwardSpeed(0.12f))
            .burst(40)
            .particleColor(1.0f, 0.8f, 0.2f)
            .particleColorEnd(1.0f, 0.3f, 0.0f)
            .particleScale(0.18f, 0.0f)
            .particleLifetime(20, 35)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
            .particleFullBright(true)
            .particleDrag(0.95f);
        Aero_ParticleManager.addEmitter(em);

        // Central flash
        Aero_ParticleBuilder.create(world, x, y, z)
            .color(1f, 1f, 0.8f)
            .scale(1.5f, 0f)
            .scaleCurve(Aero_Curve.EASE_OUT)
            .lifetime(8)
            .blend(Aero_BlendMode.BRIGHT)
            .texture(Aero_ParticleTextures.STAR_4)
            .fullBright()
            .spawn();
    }

    /** 5: Floating seeds — organic drifting motes. */
    private void demoFloatingSeeds(World world, double x, double y, double z) {
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterBox(x - 2, y, z - 2, x + 2, y + 3, z + 2))
            .burst(20)
            .particleColor(0.4f, 1.0f, 0.5f)
            .particleColorEnd(0.8f, 1.0f, 0.3f)
            .particleScale(0.08f, 0.12f)
            .particleLifetime(80, 120)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
            .particleFullBright(true)
            .particleFadeIn(15)
            .particleFadeOut(20)
            .particleMotion(new Aero_MotionDrift(0.02f, 25f, 0.005f));
        Aero_ParticleManager.addEmitter(em);
    }

    /** 6: Ember fountain — fire sparks shooting up. */
    private void demoEmberFountain(World world, double x, double y, double z) {
        Aero_Emitter em = new Aero_Emitter(world)
            .shape(new Aero_EmitterPoint(x, y, z))
            .rate(6)
            .lifetime(60)
            .particleColor(1.0f, 0.6f, 0.1f)
            .particleColorEnd(1.0f, 0.2f, 0.0f)
            .particleScale(0.12f, 0.0f)
            .particleLifetime(20, 40)
            .particleBlend(Aero_BlendMode.GLOW)
            .particleTexture(Aero_ParticleTextures.SHARP_DOT)
            .particleFullBright(true)
            .particleGravity(-0.005f)
            .particleDrag(0.96f)
            .velocitySpread(0.06f, 0.12f, 0.06f)
            .particleMotion(new Aero_MotionNoise(0.01f, 0.2f));
        Aero_ParticleManager.addEmitter(em);
    }

    /** 7: Color gradient trail — particle with rainbow ribbon. */
    private void demoColorTrail(World world, double x, double y, double z) {
        Aero_ColorCurve rainbow = Aero_ColorCurve.keyframed(
            new float[]{0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f},
            new int[]{
                0xFFFF0000,  // red
                0xFFFFFF00,  // yellow
                0xFF00FF00,  // green
                0xFF00FFFF,  // cyan
                0xFF0044FF,  // blue
                0xFFFF00FF   // magenta
            }
        );

        for (int i = 0; i < 8; i++) {
            float angle = i * (float) (Math.PI * 2.0 / 8);
            float vx = (float) Math.cos(angle) * 0.06f;
            float vz = (float) Math.sin(angle) * 0.06f;

            Aero_ParticleBuilder.create(world, x, y, z)
                .velocity(vx, 0.04, vz)
                .colorKeyframes(rainbow)
                .scale(0.15f, 0.0f)
                .lifetime(50)
                .blend(Aero_BlendMode.GLOW)
                .texture(Aero_ParticleTextures.SOFT_GLOW)
                .fullBright()
                .gravity(-0.002f)
                .drag(0.97f)
                .trail(20)
                .spawn();
        }
    }

    /** 8: Combined — orbital particles + noise + beam. */
    private void demoCombined(World world, double x, double y, double z) {
        // Central beam
        Aero_BeamBuilder.create(world)
            .from(x, y - 0.5, z).to(x, y + 6, z)
            .width(0.1f).color(0.5f, 0.3f, 1.0f).alpha(0.5f)
            .blend(Aero_BlendMode.GLOW)
            .texture(Aero_ParticleTextures.FLAT_SQUARE)
            .core(0.03f, 1f, 0.8f, 1f)
            .uvScroll(0.05f)
            .lifetime(80).fadeOut(20)
            .spawn();

        // Orbital particles at two heights
        for (int h = 0; h < 2; h++) {
            double oy = y + 1.5 + h * 2.5;
            Aero_Emitter em = new Aero_Emitter(world)
                .shape(new Aero_EmitterRing(x, oy, z, 1.5f))
                .rate(2)
                .lifetime(75)
                .particleColor(0.6f, 0.4f, 1.0f)
                .particleColorEnd(1.0f, 0.6f, 0.9f)
                .particleScale(0.12f, 0.0f)
                .particleLifetime(25, 40)
                .particleBlend(Aero_BlendMode.GLOW)
                .particleTexture(Aero_ParticleTextures.SOFT_GLOW)
                .particleFullBright(true)
                .particleMotion(Aero_MotionComposite.of(
                    new Aero_MotionOrbital(x, oy, z, 0.12f, 1.5f)
                        .radiusShrink(0.99f).verticalSpeed(0.015f),
                    new Aero_MotionNoise(0.008f, 0.15f)
                ));
            Aero_ParticleManager.addEmitter(em);
        }

        // Bolts connecting to ground
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 3; i++) {
            double bx = x + (r.nextFloat() * 2 - 1) * 2;
            double bz = z + (r.nextFloat() * 2 - 1) * 2;
            Aero_BoltBuilder.create(world)
                .from(x, y + 5, z).to(bx, y, bz)
                .segments(6).spread(0.3f).branches(1)
                .color(0.7f, 0.5f, 1.0f).lineWidth(1.5f)
                .reRandomize(3).lifetime(60)
                .spawn();
        }
    }

    // -----------------------------------------------------------------------
    // Util
    // -----------------------------------------------------------------------

    /** Simple hue (0-360) to RGB float[3]. */
    private static float[] hueToRGB(float hue) {
        float h = (hue % 360) / 60f;
        int i = (int) h;
        float f = h - i;
        float q = 1f - f;
        switch (i) {
            case 0: return new float[]{1, f, 0};
            case 1: return new float[]{q, 1, 0};
            case 2: return new float[]{0, 1, f};
            case 3: return new float[]{0, q, 1};
            case 4: return new float[]{f, 0, 1};
            default: return new float[]{1, 0, q};
        }
    }
}
