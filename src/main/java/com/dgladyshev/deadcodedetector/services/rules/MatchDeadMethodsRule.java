package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.AntiPatternType;
import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class MatchDeadMethodsRule extends MatchAbstractRule {

    protected MatchDeadMethodsRule() {
        super(AntiPatternType.DEAD_CODE);
    }

    @Override
    public List<AntiPatternCodeOccurrence> findMatches(Database db) {
        //we don't consider public methods dead because it can produce false positive in case of library code
        Entity[] privateMethods = db.ents("method private ~constructor ~unknown ~unresolved");
        return Arrays.stream(privateMethods)
                .filter(method -> !isCalled(method))
                .map(this::toReference)
                .map(this::toAntiPatternCodeOccurrence)
                .collect(Collectors.toList());
    }

}
