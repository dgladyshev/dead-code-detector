package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.AntiPatternType;
import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import com.scitools.understand.Reference;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                            .map(ref -> ref.ent())
                            //.filter(method -> clazz.kind().name().contains("Abstract")) //TODO check
                            .filter(method -> !(hasReference(method) && hasReferenceOnTheSameLine(clazz, method)))
                            .map(method -> processParams(method))
                            .flatMap(List::stream)
                            .collect(Collectors.toSet())
            );
        }
        return occurrences;
    }

    private List<AntiPatternCodeOccurrence> processParams(Entity method) {
        Reference[] methodReferences = method.refs("~unresolved ~unknown ~catch", "parameter", true);
        return Arrays.stream(methodReferences)
                .map(reference -> reference.ent())
                .filter(parameter -> !isUsed(parameter))
                .map(parameter -> toReference(parameter))
                .map(reference -> toAntiPatternCodeOccurrence(reference))
                .collect(Collectors.toList());
    }

    boolean hasReference(Entity ent) {
        return toReference(ent) != null;
    }

    boolean hasReferenceOnTheSameLine(Entity ent1, Entity ent2) {
        Reference reference1 = toReference(ent1);
        Reference reference2 = toReference(ent2);
        return reference1 != null && reference2 != null && reference1.line() == reference2.line();
    }

}
