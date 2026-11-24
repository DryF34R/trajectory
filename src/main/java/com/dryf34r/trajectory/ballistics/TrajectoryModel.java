package com.dryf34r.trajectory.ballistics;

import java.util.Optional;

/**
 * Tick-by-tick replica of CBC's projectile flight ({@code AbstractCannonProjectile.tick/getForces}),
 * reduced to the vertical plane of the shot.
 * <p>
 * Coordinates are relative to the centre of the cannon's pivot block (the block above the mount):
 * {@code x} is horizontal distance along the firing direction, {@code y} is height.
 * Per tick:
 * <pre>
 * a    = -v̂ · min(drag · |v| [· |v|], |v|) + (0, gravity)
 * pos += v + a/2
 * v   += a
 * </pre>
 * Pure Java on purpose: no Minecraft classes, so it can be unit tested and reused anywhere.
 */
public final class TrajectoryModel {

    /** Upper bound on simulated ticks; a 4 b/t shell at 60° lands in ~120 ticks. */
    public static final int MAX_TICKS = 20 * 60;

    private TrajectoryModel() {
    }

    /**
     * Distance from the pivot centre to where CBC spawns the projectile, along the barrel.
     * CBC spawns it at the centre of the block past the muzzle, minus 2 blocks.
     *
     * @param muzzleOffset how many blocks the last barrel block is in front of the pivot block
     *                     (e.g. pivot at z=-9, muzzle at z=-3 gives 6)
     */
    public static double launchOffset(int muzzleOffset) {
        return muzzleOffset + 1 - 2;
    }

    /**
     * Simulates one shot until it comes down through {@code targetHeight}.
     *
     * @param ballistics   projectile properties in the current dimension
     * @param speed        muzzle speed in blocks/tick (CBC: sum of charge strengths, 2 per powder charge)
     * @param pitchDegrees barrel elevation, positive is up
     * @param launchOffset see {@link #launchOffset(int)}
     * @param targetHeight target surface height relative to the pivot centre (e.g. -2.5)
     * @return the impact, or empty if the shell never comes down through that height
     */
    public static Optional<Impact> simulate(Ballistics ballistics, double speed, double pitchDegrees,
                                            double launchOffset, double targetHeight) {
        double pitch = Math.toRadians(pitchDegrees);
        double dirX = Math.cos(pitch);
        double dirY = Math.sin(pitch);

        double x = launchOffset * dirX;
        double y = launchOffset * dirY;
        double vx = speed * dirX;
        double vy = speed * dirY;

        for (int tick = 1; tick <= MAX_TICKS; tick++) {
            double v = Math.sqrt(vx * vx + vy * vy);
            double ax = 0;
            double ay = ballistics.gravity();
            if (v > 1e-9) {
                double dragForce = ballistics.drag() * v * (ballistics.quadraticDrag() ? v : 1);
                dragForce = Math.min(dragForce, v);
                ax -= vx / v * dragForce;
                ay -= vy / v * dragForce;
            }

            double nx = x + vx + ax / 2;
            double ny = y + vy + ay / 2;

            if (ny <= targetHeight && y > targetHeight) {
                double t = (y - targetHeight) / (y - ny);
                return Optional.of(new Impact(x + t * (nx - x), tick));
            }
            // Already below the target and falling: it can never come down onto it.
            if (ny < targetHeight && vy + ay < 0) {
                return Optional.empty();
            }

            x = nx;
            y = ny;
            vx += ax;
            vy += ay;
        }
        return Optional.empty();
    }
}
