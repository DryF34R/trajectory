package com.dryf34r.trajectory.ballistics;

/**
 * Where a simulated shot comes down to the target height.
 *
 * @param distance horizontal distance from the cannon's pivot to the impact point, in blocks
 * @param ticks    flight time in ticks (the tick during which the shell crossed the target height)
 */
public record Impact(double distance, int ticks) {
}
