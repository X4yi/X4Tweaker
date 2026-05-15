package com.x4yi.x4tweaker.automation.control;

public class MovementConfig {
    private final boolean sprintEnabled;
    private final boolean jumpEnabled;
    private final boolean strafeEnabled;
    private final int strafeInterval;
    private final double attackRange;
    private final double maxChaseDistance;
    private final boolean avoidMobsInPath;

    private MovementConfig(Builder builder) {
        this.sprintEnabled = builder.sprintEnabled;
        this.jumpEnabled = builder.jumpEnabled;
        this.strafeEnabled = builder.strafeEnabled;
        this.strafeInterval = builder.strafeInterval;
        this.attackRange = builder.attackRange;
        this.maxChaseDistance = builder.maxChaseDistance;
        this.avoidMobsInPath = builder.avoidMobsInPath;
    }

    public boolean isSprintEnabled()    { return sprintEnabled; }
    public boolean isJumpEnabled()      { return jumpEnabled; }
    public boolean isStrafeEnabled()    { return strafeEnabled; }
    public int getStrafeInterval()      { return strafeInterval; }
    public double getAttackRange()      { return attackRange; }
    public double getMaxChaseDistance()  { return maxChaseDistance; }
    public boolean isAvoidMobsInPath()  { return avoidMobsInPath; }

    public static class Builder {
        private boolean sprintEnabled = true;
        private boolean jumpEnabled = true;
        private boolean strafeEnabled = true;
        private int strafeInterval = 15;
        private double attackRange = 3.5;
        private double maxChaseDistance = 20.0;
        private boolean avoidMobsInPath = true;

        public Builder sprintEnabled(boolean v)     { this.sprintEnabled = v; return this; }
        public Builder jumpEnabled(boolean v)       { this.jumpEnabled = v; return this; }
        public Builder strafeEnabled(boolean v)     { this.strafeEnabled = v; return this; }
        public Builder strafeInterval(int v)        { this.strafeInterval = v; return this; }
        public Builder attackRange(double v)        { this.attackRange = v; return this; }
        public Builder maxChaseDistance(double v)    { this.maxChaseDistance = v; return this; }
        public Builder avoidMobsInPath(boolean v)   { this.avoidMobsInPath = v; return this; }

        public MovementConfig build() {
            return new MovementConfig(this);
        }
    }
}
