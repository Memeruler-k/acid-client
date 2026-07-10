package dev.abstr3act.addon.utils.pathfinder;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class PathHub {
    private Vec3d loc;
    private ArrayList<Vec3d> pathway;
    private double sqDist;
    private double currentCost;
    private double maxCost;

    public PathHub(Vec3d loc, PathHub parentPathHub, ArrayList<Vec3d> pathway, double sqDist, double currentCost, double maxCost) {
        this.loc = loc;
        this.pathway = pathway;
        this.sqDist = sqDist;
        this.currentCost = currentCost;
        this.maxCost = maxCost;
    }

    public Vec3d getLoc() {
        return this.loc;
    }

    public void setLoc(Vec3d loc) {
        this.loc = loc;
    }

    public ArrayList<Vec3d> getPathway() {
        return this.pathway;
    }

    public void setPathway(ArrayList<Vec3d> pathway) {
        this.pathway = pathway;
    }

    public double getSqDist() {
        return this.sqDist;
    }

    public void setSqDist(double sqDist) {
        this.sqDist = sqDist;
    }

    public double getCurrentCost() {
        return this.currentCost;
    }

    public void setCurrentCost(double currentCost) {
        this.currentCost = currentCost;
    }

    public void setParentPathHub(PathHub parentPathHub) {
    }

    public double getMaxCost() {
        return this.maxCost;
    }

    public void setMaxCost(double maxCost) {
        this.maxCost = maxCost;
    }
}
