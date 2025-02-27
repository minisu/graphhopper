package com.graphhopper.routing.util;

import com.graphhopper.routing.profiles.EnumEncodedValue;
import com.graphhopper.routing.profiles.RoadClass;
import com.graphhopper.routing.profiles.RoadEnvironment;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;

import java.util.List;

import static com.graphhopper.routing.profiles.RoadClass.MOTORWAY;
import static com.graphhopper.routing.profiles.RoadClass.TRUNK;
import static com.graphhopper.routing.profiles.RoadEnvironment.*;

public class SnapPreventionEdgeFilter implements EdgeFilter {

    private final EnumEncodedValue<RoadEnvironment> reEnc;
    private final EnumEncodedValue<RoadClass> rcEnc;
    private final EdgeFilter filter;
    private boolean avoidMotorway = false, avoidTrunk;
    private boolean avoidTunnel, avoidBridge, avoidFerry, avoidFord, avoidPedestrianArea;

    public SnapPreventionEdgeFilter(EdgeFilter filter, EnumEncodedValue<RoadClass> rcEnc,
                                    EnumEncodedValue<RoadEnvironment> reEnc, List<String> snapPreventions) {
        this.filter = filter;
        this.reEnc = reEnc;
        this.rcEnc = rcEnc;

        for (String roadClassOrRoadEnv : snapPreventions) {
            if ("motorway".equals(roadClassOrRoadEnv)) {
                avoidMotorway = true;
                continue;
            } else if ("trunk".equals(roadClassOrRoadEnv)) {
                avoidTrunk = true;
                continue;
            }

            RoadEnvironment rc = RoadEnvironment.find(roadClassOrRoadEnv);
            if (rc == TUNNEL)
                avoidTunnel = true;
            else if (rc == BRIDGE)
                avoidBridge = true;
            else if (rc == FERRY)
                avoidFerry = true;
            else if (rc == FORD)
                avoidFord = true;
            else if (rc == PEDESTRIAN_AREA)
                avoidPedestrianArea = true;
            else
                throw new IllegalArgumentException("Cannot find " + Parameters.Routing.SNAP_PREVENTION + ": " + roadClassOrRoadEnv);
        }
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        return filter.accept(edgeState)
                && !(avoidMotorway && edgeState.get(rcEnc) == MOTORWAY)
                && !(avoidTrunk && edgeState.get(rcEnc) == TRUNK)
                && !(avoidTunnel && edgeState.get(reEnc) == TUNNEL)
                && !(avoidBridge && edgeState.get(reEnc) == BRIDGE)
                && !(avoidFord && edgeState.get(reEnc) == FORD)
                && !(avoidFerry && edgeState.get(reEnc) == FERRY)
                && !(avoidPedestrianArea && edgeState.get(reEnc) == PEDESTRIAN_AREA);
    }
}
