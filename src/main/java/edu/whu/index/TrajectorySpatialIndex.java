package edu.whu.index;

import edu.whu.structure.Trajectory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class TrajectorySpatialIndex extends HashMap<Integer, ArrayList<Trajectory>> {
}


