package com.bitflake.counter.algo.shared.current.record;

import com.bitflake.counter.algo.shared.current.CountState;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Iterator;
import java.util.List;

/**
 * Created by fahor on 04/10/2016.
 */
public class StateStatistics {
    private static void computeStateStats(List<CountState> states, List<CountState> newStates) {
        newStates.get(0).setTime(states.get(1).getTime() - 1);
        newStates.get(newStates.size() - 1).setTime(newStates.get(newStates.size() - 2).getTime() + 1);

        int i;
        Iterator<CountState> it = newStates.iterator();
        CountState[] s = new CountState[3];
        SummaryStatistics[] stats = new SummaryStatistics[3];
        for (i = 0; i < s.length; i++) {
            s[i] = it.next();
            stats[i] = new SummaryStatistics();
        }
        for (CountState state : states) {
            if (state.getTime() > s[2].getTime()) {
                setStats(s[0], stats[0]);
                setStats(s[1], stats[1]);
                SummaryStatistics tmp = stats[0];
                stats[0] = stats[2];
                stats[2] = tmp;
                stats[1].clear();
                stats[2].clear();
                s[0] = s[2];
                s[1] = it.next();
                s[2] = it.next();
            }
            for (i = 0; i < s.length; i++) {
                double d = s[i].getDistance(state.values);
                stats[i].addValue(d);
            }
        }
        setStats(s[0], stats[0]);
        setStats(s[1], stats[1]);
        setStats(s[2], stats[1]);
    }

    private static void setStats(CountState s, SummaryStatistics stats) {
        s.distanceMax = stats.getMax();
        s.distanceMean = stats.getMax();
        s.distanceSD = stats.getStandardDeviation();
    }
}
