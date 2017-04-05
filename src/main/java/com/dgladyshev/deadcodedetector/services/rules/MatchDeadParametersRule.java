package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.AntiPatternType;
import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import com.scitools.understand.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class MatchDeadParametersRule extends MatchAbstractRule {

    protected MatchDeadParametersRule() {
        super(AntiPatternType.DEAD_CODE);
    }

    @Override
    public List<AntiPatternCodeOccurrence> findMatches(Database db) {
        List<AntiPatternCodeOccurrence> occurrences = new ArrayList<>();
        Entity[] classes = db.ents("type ~interface ~unresolved ~unknown");
        for (Entity clazz : classes) {
            Reference[] methods = clazz.refs("~unresolved ~unknown", "method", true);
            occurrences.addAll(
                    Arrays.stream(methods)
                            .map(Reference::ent)
                            //.filter(method -> clazz.kind().name().contains("Abstract")) //TODO remove @Value(dir)
                            .filter(method -> !(hasReference(method) && hasReferenceOnTheSameLine(clazz, method)))
                            .map(this::processParams)
                            .flatMap(List::stream)
                            .collect(Collectors.toSet())
            );
        }
        return occurrences;
    }

    private List<AntiPatternCodeOccurrence> processParams(Entity method) {
        Reference[] methodReferences = method.refs("~unresolved ~unknown ~catch", "parameter", true);
        return Arrays.stream(methodReferences)
                .map(Reference::ent)
                .filter(parameter -> !isUsed(parameter))
                .map(this::toReference)
                .map(this::toAntiPatternCodeOccurrence)
                .collect(Collectors.toList());
    }

    private boolean hasReference(Entity ent) {
        return toReference(ent) != null;
    }

    private boolean hasReferenceOnTheSameLine(Entity ent1, Entity ent2) {
        Reference reference1 = toReference(ent1);
        Reference reference2 = toReference(ent2);
        return reference1 != null && reference2 != null && reference1.line() == reference2.line();
    }

}
