package com.dryf34r.trajectory.ballistics;

import java.util.Optional;

/**
 * Inverse problem: given a target, find the barrel elevation that hits it.
 * <p>
 * Usually there are two answers on flat-ish ground: a low (direct) arc below the max-range angle
 * and a high (lobbed) arc above it. With drag, the max-range angle is around 30–35°, not 45°.
 */
public final class FireSolver {

    private static final double MIN_ANGLE = 0.0;
    private static final double MAX_ANGLE = 60.0;
    private static final double ANGLE_STEP = 1.0;
    private static final double TOLERANCE = 0.001;
    private static final int MAX_ITERATIONS = 30;

    private FireSolver() {
    }

    /**
     * @param lowArc  elevation of the flatter solution in degrees, if any
     * @param highArc elevation of the steeper solution in degrees, if any
     */
    public record Solution(Optional<Double> lowArc, Optional<Double> highArc) {
    }

    /** Range at this pitch, or -1 if the shell never comes down to the target height. */
    private static double getEffectiveRange(Ballistics ballistics, double speed, double launchOffset,
                                            double angle, double targetHeight) {
        Optional<Impact> result = TrajectoryModel.simulate(ballistics, speed, angle, launchOffset, targetHeight);
        return result.isPresent() ? result.get().distance() : -1.0;
    }

    /**
     * Finds the elevations that put a shell at {@code distance} blocks horizontally from the pivot,
     * coming down through {@code targetHeight} (relative to the pivot centre).
     * <p>
     * A coarse scan in {@link #ANGLE_STEP} steps finds where the range crosses {@code distance}:
     * rising through it gives the low arc, falling through it the high arc. Bisection then refines
     * each 1° bracket.
     */
    public static Solution solve(Ballistics ballistics, double speed, double launchOffset,
                                 double distance, double targetHeight) {
        double maxRange;

        Double lowBracketMin = null;
        Double lowBracketMax = null;

        Double highBracketMin = null;
        Double highBracketMax = null;

        // Referans
        double prevAngle = MIN_ANGLE;
        double prevRange = getEffectiveRange(ballistics, speed, launchOffset, prevAngle, targetHeight);
        maxRange = prevRange;

        // Kaba tarama
        for (double angle = MIN_ANGLE + ANGLE_STEP; angle <= MAX_ANGLE; angle += ANGLE_STEP) {
            double currentRange = getEffectiveRange(ballistics, speed, launchOffset, angle, targetHeight);
            if (currentRange > maxRange) {
                maxRange = currentRange;
            }

            // Menzil artarken hedefi geçti -> alçak yay
            if (lowBracketMin == null && prevRange < distance && currentRange >= distance) {
                lowBracketMin = prevAngle;
                lowBracketMax = angle;
            }

            // Menzil azalırken hedefi geçti -> yüksek yay
            if (prevRange >= distance && currentRange < distance) {
                highBracketMin = prevAngle;
                highBracketMax = angle;
            }

            prevAngle = angle;
            prevRange = currentRange;
        }

        // Menzil dışı
        if (maxRange < distance) {
            return new Solution(Optional.empty(), Optional.empty());
        }

        // Finalizasyon
        Optional<Double> finalLow = Optional.empty();
        if (lowBracketMin != null) {
            finalLow = Optional.of(refineAngle(ballistics, speed, launchOffset, targetHeight,
                    lowBracketMin, lowBracketMax, distance, true));
        }

        Optional<Double> finalHigh = Optional.empty();
        if (highBracketMin != null) {
            finalHigh = Optional.of(refineAngle(ballistics, speed, launchOffset, targetHeight,
                    highBracketMin, highBracketMax, distance, false));
        }

        return new Solution(finalLow, finalHigh);
    }

    /**
     * Bisection on [low, high]. {@code increasing} says whether range grows with angle in this
     * bracket (low arc) or shrinks (high arc).
     */
    private static double refineAngle(Ballistics ballistics, double speed, double launchOffset,
                                      double targetHeight, double low, double high, double distance,
                                      boolean increasing) {
        int iterations = 0;

        while ((high - low) > TOLERANCE && iterations < MAX_ITERATIONS) {
            double mid = (low + high) / 2.0;
            double midRange = getEffectiveRange(ballistics, speed, launchOffset, mid, targetHeight);

            // Alçak yayda kısa kalan atış daha yüksek açı ister, yüksek yayda tam tersi.
            boolean tooShort = midRange < distance;
            if (tooShort == increasing) {
                low = mid;
            } else {
                high = mid;
            }
            iterations++;
        }

        return (low + high) / 2.0;
    }
}
