package com.dryf34r.trajectory.ballistics;

/**
 * Effective ballistic properties of one projectile in one dimension.
 * <p>
 * Mirrors CBC's {@code BallisticPropertiesComponent} after the dimension multipliers are applied:
 * {@code gravity = projectile.gravity × dimension.gravity_multiplier} and
 * {@code drag = projectile.drag × dimension.drag_multiplier}.
 *
 * @param gravity       vertical acceleration in blocks/tick², negative (pulls down), e.g. -0.05
 * @param drag          drag coefficient, &gt;= 0, e.g. 0.01
 * @param quadraticDrag true if drag scales with speed squared instead of speed
 */
public record Ballistics(double gravity, double drag, boolean quadraticDrag) {

    /** Default big cannon shell (shot, HE, AP, ...) in the overworld. */
    public static final Ballistics BIG_CANNON_OVERWORLD = new Ballistics(-0.05, 0.01, false);

    public Ballistics {
        if (gravity > 0) throw new IllegalArgumentException("gravity must be <= 0, was " + gravity);
        if (drag < 0) throw new IllegalArgumentException("drag must be >= 0, was " + drag);
    }

    /** Applies a dimension's multipliers (from CBC's dimension_munition_properties). */
    public Ballistics inDimension(double gravityMultiplier, double dragMultiplier) {
        return new Ballistics(gravity * gravityMultiplier, drag * dragMultiplier, quadraticDrag);
    }
}
