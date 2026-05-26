package dev.cammiescorner.townships.api.inject;

import dev.cammiescorner.townships.util.Member;
import dev.cammiescorner.townships.util.Town;

import java.util.Optional;

public interface ServerPlayerExt {

    default Optional<Member> townships$asTownMember() {
        throw new AssertionError("Implemented in mixin");
    }

    default Optional<Town> townships$getTown() {
        throw new AssertionError("Implemented in mixin");
    }
}
