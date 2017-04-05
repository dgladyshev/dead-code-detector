package com.dgladyshev.deadcodedetector.services.rules;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.AntiPatternType;
import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import com.scitools.understand.Reference;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

@SuppressWarnings("PMD.NullAssignment")
public abstract class MatchAbstractRule implements MatchRuleFunction {

    @Value("${data.dir}")
    private String dataDir;

    private final AntiPatternType antiPatternType;

    MatchAbstractRule(AntiPatternType antiPatternType) {
        this.antiPatternType = antiPatternType;
    }

    public abstract List<AntiPatternCodeOccurrence> findMatches(Database db);

    AntiPatternCodeOccurrence toAntiPatternCodeOccurrence(Reference ref) {
        String file = ref.file().longname(true);
        String type = ref.scope().kind().name();
        String name = ref.scope().longname(true);
        int line = ref.line();
        int column = ref.column();
        return AntiPatternCodeOccurrence.builder()
                .type(type)
                .name(StringUtils.substringAfterLast(name, "."))
                .file(file.replace(dataDir + "/", "")) //TODO test this
                .line(line)
                .column(column)
                .antiPatternType(antiPatternType)
                .build();
    }

    @Nullable
    Reference toReference(Entity ent) {
        Reference[] defineInRefs = ent.refs("definein", null, false);
        return defineInRefs.length > 0 ? defineInRefs[0] : null;
    }

    boolean isCalled(Entity ent) {
        return ent.refs("callby", null, false).length > 0;
    }

    boolean isUsed(Entity ent) {
        return ent.refs("useby", null, false).length > 0;
    }
}
