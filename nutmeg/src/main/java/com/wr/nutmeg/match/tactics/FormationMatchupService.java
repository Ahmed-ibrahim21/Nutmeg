package com.wr.nutmeg.match.tactics;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class FormationMatchupService {

    private final Map<Formation, Map<Formation, MatchupModifiers>> matchups = new EnumMap<>(Formation.class);

    public FormationMatchupService() {
        register(Formation.F_4_3_3, Formation.F_5_3_2,
                new MatchupModifiers(6, -2, -2, 8, 4));
        register(Formation.F_4_3_3, Formation.F_4_5_1,
                new MatchupModifiers(4, -4, -3, 6, 3));
        register(Formation.F_5_3_2, Formation.F_4_3_3,
                new MatchupModifiers(-2, 8, 4, -6, -3));
        register(Formation.F_5_3_2, Formation.F_3_4_3,
                new MatchupModifiers(-3, 6, 2, -4, -2));
        register(Formation.F_4_4_2, Formation.F_4_4_2,
                new MatchupModifiers(0, 0, 0, 0, 0));
        register(Formation.F_3_4_3, Formation.F_5_3_2,
                new MatchupModifiers(5, -3, 0, 7, 5));
    }

    public MatchupModifiers modifiersFor(Formation own, Formation opponent) {
        Map<Formation, MatchupModifiers> opponentMap = matchups.get(own);
        if (opponentMap == null) {
            return MatchupModifiers.none();
        }
        return opponentMap.getOrDefault(opponent, MatchupModifiers.none());
    }

    private void register(Formation own, Formation opponent, MatchupModifiers modifiers) {
        matchups.computeIfAbsent(own, ignored -> new EnumMap<>(Formation.class))
                .put(opponent, modifiers);
    }
}
