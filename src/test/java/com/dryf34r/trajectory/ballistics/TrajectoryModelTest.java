package com.dryf34r.trajectory.ballistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class TrajectoryModelTest {

    // In-game measurement, 2026-11-24 (docs/research/faz1-cbc-balistik.md):
    // superflat (ground top y=-60), steel cannon, 2 powder charges + solid shot, firing south (+z).
    // Mount (-2,-60,-9) -> pivot block (-2,-58,-9), muzzle (-2,-58,-3).
    private static final double SPEED = 4.0;
    private static final double PIVOT_CENTER_Z = -8.5;
    private static final double PIVOT_CENTER_Y = -57.5;
    private static final double GROUND_Y = -60.0;
    private static final double LAUNCH_OFFSET = TrajectoryModel.launchOffset(6);

    @ParameterizedTest(name = "{0}° lands on block z={1}")
    @CsvSource({
            "0, 36",
            "15, 131",
            "30, 177",
            "45, 173",
            "60, 133",
    })
    void matchesInGameShots(double pitch, int measuredBlockZ) {
        Impact impact = TrajectoryModel.simulate(Ballistics.BIG_CANNON_OVERWORLD, SPEED, pitch,
                LAUNCH_OFFSET, GROUND_Y - PIVOT_CENTER_Y).orElseThrow();

        int modelBlockZ = (int) Math.floor(PIVOT_CENTER_Z + impact.distance());
        assertTrue(Math.abs(modelBlockZ - measuredBlockZ) <= 1,
                "model block " + modelBlockZ + ", measured " + measuredBlockZ);
    }

    @Test
    void launchOffsetIsBlockPastMuzzleMinusTwo() {
        assertEquals(5.0, TrajectoryModel.launchOffset(6));
    }

    @Test
    void withoutDragFlatGroundRangeIsLargestAt45() {
        Ballistics vacuum = new Ballistics(-0.05, 0, false);
        double at30 = TrajectoryModel.simulate(vacuum, SPEED, 30, 0, 0).orElseThrow().distance();
        double at45 = TrajectoryModel.simulate(vacuum, SPEED, 45, 0, 0).orElseThrow().distance();
        double at60 = TrajectoryModel.simulate(vacuum, SPEED, 60, 0, 0).orElseThrow().distance();
        assertTrue(at45 > at30 && at45 > at60);
    }

    @Test
    void targetAboveApexIsUnreachable() {
        assertTrue(TrajectoryModel.simulate(Ballistics.BIG_CANNON_OVERWORLD, SPEED, 10, 0, 500).isEmpty());
    }
}
