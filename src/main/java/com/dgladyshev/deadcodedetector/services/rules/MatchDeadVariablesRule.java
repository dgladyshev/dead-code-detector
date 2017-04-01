package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.AntiPatternType;
import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class MatchDeadVariablesRule extends MatchAbstractRule {

    protected MatchDeadVariablesRule() {
        super(AntiPatternType.DEAD_CODE);
    }

    @Override
    public List<AntiPatternCodeOccurrence> findMatches(Database db) {
        Entity[] privateVariables = db.ents("variable private");
        Entity[] localVariables = db.ents("variable local");
        return Arrays.stream((Entity[]) ArrayUtils.addAll(privateVariables, localVariables))
                .filter(method -> !isUsed(method))
                .map(method -> toReference(method))
                .map(reference -> toAntiPatternCodeOccurrence(reference))
                .collect(Collectors.toList());
    }

}
