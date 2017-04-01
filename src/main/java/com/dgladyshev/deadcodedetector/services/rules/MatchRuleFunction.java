package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.scitools.understand.Database;
import java.util.List;

@FunctionalInterface
public interface MatchRuleFunction {

    List<AntiPatternCodeOccurrence> findMatches(Database db);

}
