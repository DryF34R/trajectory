package com.dryf34r.trajectory.ballistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FireSolverTest {

    private static final Ballistics B = Ballistics.BIG_CANNON_OVERWORLD;
    private static final double SPEED = 4.0;
    private static final double LAUNCH_OFFSET = 5.0;
    private static final double TARGET_HEIGHT = -2.5;

    @Test
    void solutionsHitTheTarget() {
        double distance = 150;
        FireSolver.Solution s = FireSolver.solve(B, SPEED, LAUNCH_OFFSET, distance, TARGET_HEIGHT);

        double low = s.lowArc().orElseThrow();
        double high = s.highArc().orElseThrow();
        assertTrue(low < high);
        for (double pitch : new double[]{low, high}) {
            double hit = TrajectoryModel.simulate(B, SPEED, pitch, LAUNCH_OFFSET, TARGET_HEIGHT)
                    .orElseThrow().distance();
            assertEquals(distance, hit, 0.5, "pitch " + pitch);
        }
    }

    @Test
    void outOfRangeHasNoSolution() {
        FireSolver.Solution s = FireSolver.solve(B, SPEED, LAUNCH_OFFSET, 400, TARGET_HEIGHT);
        assertTrue(s.lowArc().isEmpty() && s.highArc().isEmpty());
    }

    @Test
    void reproducesInGameThirtyDegreeShot() {
        // 30° landed on block z=177; pivot centre z=-8.5 -> ~185.5 blocks.
        FireSolver.Solution s = FireSolver.solve(B, SPEED, LAUNCH_OFFSET, 185.5, TARGET_HEIGHT);
        assertEquals(30.0, s.lowArc().orElseThrow(), 3.0);
    }
}
