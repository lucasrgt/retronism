package refinery.proof;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import worldline.extension.*;

/** External mod-owned contracts consume evidence produced by the fixture inside the real game process. */
public final class RefineryExtension implements WorldlineExtension {
    @Override public void register(WorldlineExtensionRegistry registry) {
        registry.subject(ExtensionSubject.of("refinery:machine",ExtensionSubjectKind.SUBSYSTEM,"5x3x3 refinery"));
        registry.fixture("fresh-game",context -> {
            if(!context.runtime().getClass().getName().startsWith("worldline.modloader.testkit."))
                throw new AssertionError("This proof requires the qualified real legacy client provider");
        });
        registry.action("exercise-machine",context -> {
            Path root=Paths.get(System.getenv("REFINERY_PROOF_ROOT"));
            Path evidence=root.resolve("runtime.properties");
            for(int tick=0;tick<1500;tick++) {
                context.tick();
                if(Files.isRegularFile(evidence)) {
                    Properties values=read(evidence);
                    if("FAILED".equals(values.getProperty("status"))) throw new AssertionError(values.getProperty("detail"));
                    if("PASS".equals(values.getProperty("status"))) {
                        if(!"true".equals(values.getProperty("nativeFormation"))) throw new AssertionError("Native formation input was not proved");
                        context.attach("minecraft-runtime.properties",Files.readAllBytes(evidence));
                        context.attach("window-input.log",Files.readAllBytes(root.resolve("window-input.log")));
                        try(java.util.stream.Stream<Path> shots=Files.walk(root.resolve("formation"))) {
                            for(Path path:(Iterable<Path>)shots.filter(Files::isRegularFile)::iterator)
                                context.attach(root.relativize(path).toString().replace('\\','-').replace('/','-'),Files.readAllBytes(path));
                        }
                        try(java.util.stream.Stream<Path> images=Files.list(root.resolve("screenshots"))) {
                            for(Path path:(Iterable<Path>)images::iterator) context.attach(path.getFileName().toString(),Files.readAllBytes(path));
                        }
                        return;
                    }
                }
            }
            throw new AssertionError("Runtime fixture did not finish within 1500 controlled ticks");
        });
        registry.observation("pipeline",context -> read(Paths.get(System.getenv("REFINERY_PROOF_ROOT"),"runtime.properties")).getProperty("status"));
        registry.oracle("equatable",ExtensionOracles.equatable());
        registry.contract(ExtensionContract.builder("real-machine-pipeline","refinery:machine")
            .fixture("fresh-game").action("exercise-machine").observation("pipeline").oracle("equatable")
            .mode(ExtensionMode.CUSTOM_CONTRACT).custom(ExtensionEvidence.signature(Collections.singletonMap("pipeline","PASS"))).build());
    }
    private static Properties read(Path path) throws Exception {
        Properties values=new Properties(); try(InputStream input=Files.newInputStream(path)) { values.load(input); } return values;
    }
}
