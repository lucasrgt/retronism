package refinery.proof;

import java.nio.file.*;
import java.util.List;
import worldline.extension.*;
import worldline.modloader.testkit.ForgeTestRuntimeProvider;
import worldline.testkit.*;

public final class RunProof {
    public static void main(String[] args) throws Exception {
        Path root=Paths.get("").toAbsolutePath().normalize(), run=Paths.get(System.getenv("REFINERY_PROOF_ROOT"));
        List<WorldlineExtensionPlan> plans=WorldlineExtensionDiscovery.discover(root,RunProof.class.getClassLoader(),
            ExtensionCapabilities.of(ExtensionCapabilities.TESTKIT_V1,ExtensionCapabilities.ATLAS_V1));
        if(plans.size()!=1) throw new AssertionError("Expected one refinery extension");
        RunnerOptions options=new RunnerOptions().provider(new ForgeTestRuntimeProvider()).world(run.resolve("world"))
            .artifacts(run.resolve("worldline-results")).runtimeLock(root.resolve(".local/official-runtime.lock"))
            .timeout(240000).seed(173);
        TestRunResult result=new TestRunner().run(ExtensionTestSpecs.create(plans.get(0),ExtensionMode.CUSTOM_CONTRACT),options,
            new TestReporter() {
                public void testStarted(String path,int attempt) { System.out.println("WORLDLINE TEST " + path); }
                public void testFinished(TestResult test) {
                    System.out.println(test.status()+" "+test.path());
                    if(test.errorMessage()!=null) System.err.println(test.errorMessage());
                }
            });
        JUnitReporter junit=new JUnitReporter(run.resolve("worldline-junit.xml")); junit.runFinished(result);
        if(!result.passed()) throw new AssertionError("Worldline refinery proof failed: "+result.fatalError());
        System.out.println("REFINERY_WORLDLINE_PASS="+run);
    }
}
