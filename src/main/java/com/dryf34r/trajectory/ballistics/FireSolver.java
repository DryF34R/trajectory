package com.dryf34r.trajectory.ballistics;

import java.util.Optional;

/**
 * Inverse problem: given a target, find the barrel elevation that hits it.
 * <p>
 * Usually there are two answers on flat-ish ground: a low (direct) arc below the max-range angle
 * and a high (lobbed) arc above it. With drag, the max-range angle is around 30–35°, not 45°.
 */
public final class FireSolver {

    private FireSolver() {
    }

    /**
     * @param lowArc  elevation of the flatter solution in degrees, if any
     * @param highArc elevation of the steeper solution in degrees, if any
     */
    public record Solution(Optional<Double> lowArc, Optional<Double> highArc) {
    }

    /**
     * Finds the elevations that put a shell at {@code distance} blocks horizontally from the pivot,
     * coming down through {@code targetHeight} (relative to the pivot centre).
     *
     * TODO(Arda): implement. Suggested approach:
     *  1. Scan pitch from the mount's min to max (e.g. -30..60) in 1° steps with
     *     {@link TrajectoryModel#simulate}, note the angle with the largest range.
     *  2. Below that angle, range grows with pitch; above it, range shrinks. In each half, find
     *     the step where the range crosses {@code distance}.
     *  3. Narrow it down with bisection (halve the interval ~20 times) to get a precise angle.
     *  4. If the target is farther than the max range, that arc is Optional.empty().
     */
    public static Solution solve(Ballistics ballistics, double speed, double launchOffset,
                                 double distance, double targetHeight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
