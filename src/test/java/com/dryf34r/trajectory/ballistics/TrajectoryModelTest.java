package com.dryf34r.trajectory.ballistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class TrajectoryModelTest {

    // In-game measurements, 2026-11-24 (docs/research/faz1-cbc-balistik.md):
    // superflat (ground top y=-60), steel cannon, solid shot, firing south (+z).
    // Mount (-2,-60,-9) -> pivot block (-2,-58,-9), muzzle (-2,-58,-3).
    // speed = 2 per powder charge; surfaceY = top of the block the shell came down on.
    private static final double SPEED = 4.0;
    private static final double PIVOT_CENTER_Z = -8.5;
    private static final double PIVOT_CENTER_Y = -57.5;
    private static final double LAUNCH_OFFSET = TrajectoryModel.launchOffset(6);

    @ParameterizedTest(name = "v={0}, {1}°, surface y={2} lands on block z={3}")
    @CsvSource({
            // fixed-angle shots
            "4, 0, -60, 36",
            "4, 15, -60, 131",
            "4, 30, -60, 177",
            "4, 45, -60, 173",
            "4, 60, -60, 133",
            // FireSolver validation shots (A1 = ground bounce point after hitting a player)
            "4, 19.625, -60, 152",
            "4, 54.625, -60, 151",
            "6, 14.5, -60, 253",
            "6, 57.75, -60, 252",
            "4, 18.125, -49, 118",
    })
    void matchesInGameShots(double speed, double pitch, double surfaceY, int measuredBlockZ) {
        Impact impact = TrajectoryModel.simulate(Ballistics.BIG_CANNON_OVERWORLD, speed, pitch,
                LAUNCH_OFFSET, surfaceY - PIVOT_CENTER_Y).orElseThrow();

        int modelBlockZ = (int) Math.floor(PIVOT_CENTER_Z + impact.distance());
        assertTrue(Math.abs(modelBlockZ - measuredBlockZ) <= 1,
                "model block " + modelBlockZ + ", measured " + measuredBlockZ);
    }

    @Test
    void launchOffsetIsMuzzleBlockCentre() {
        assertEquals(6.0, TrajectoryModel.launchOffset(6));
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
