package com.nice.avishkar;

import com.nice.avishkar.model.OptimalTravelSchedule;
import com.nice.avishkar.dao.ResourceInfo;

import java.io.IOException;
import java.util.Map;

public interface ITravelOptimizer {
    Map<String, OptimalTravelSchedule> getOptimalTravelOptions(ResourceInfo resourceInfo) throws IOException;
}
