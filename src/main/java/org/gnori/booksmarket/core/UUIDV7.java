package org.gnori.booksmarket.core;

import com.fasterxml.uuid.Generators;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UUIDV7 {

    public static UUID generate() {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
