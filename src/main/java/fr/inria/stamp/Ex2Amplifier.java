package fr.inria.stamp;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.alloy.builder.ModelBuilder;
import fr.inria.stamp.alloy.model.Model;
import fr.inria.stamp.alloy.runner.AlloyRunner;
import fr.inria.stamp.instrumentation.TestInstrumentation;
import fr.inria.stamp.instrumentation.processor.ConstraintInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ExecutableInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.InvocationInstrumenterProcessor;
import fr.inria.stamp.instrumentation.processor.ModificationInstrumenterProcessor;
import fr.inria.stamp.test.launcher.TestLauncher;
import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public class Ex2Amplifier implements fr.inria.diversify.dspot.amplifier.Amplifier {

    @Deprecated
    private Launcher spoonModel;

    private InputConfiguration configuration;

    private CtType<?> testClass;

    public Ex2Amplifier(String pathToConfiguration) {
        try {
            this.configuration = new InputConfiguration(pathToConfiguration);
            this.spoonModel = init(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod> apply(CtMethod testMethod) {
        ModelBuilder.model = new Model();
        List<CtMethod> amplifiedMethods = new ArrayList<>();
        String dependencies = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
                .buildClasspath(configuration.getInputProgram().getProgramDir());
        TestLauncher.runFromSpoonNodes(configuration,
                configuration.getInputProgram().getProgramDir() + "/" +
                configuration.getClassesDir() + AmplificationHelper.PATH_SEPARATOR + dependencies,
                testClass, Collections.singletonList(testMethod)
        );
        final Model model = ModelBuilder.getModel();
        while (model.hasNextConstraintToBeNegated()) {
            final CtMethod<?> amplifiedMethod = printAndRun(model.negateNextConstraint().toAlloy(), testMethod.clone());
            if (amplifiedMethod != null) {
                amplifiedMethods.add(amplifiedMethod);
            }
        }
        return amplifiedMethods;
    }

    private CtMethod<?> printAndRun(String alloyModel, CtMethod<?> testMethod) {
        final File directoryDSpot = new File("target/dspot");
        if (! directoryDSpot.exists()) {
            try {
                FileUtils.forceMkdir(directoryDSpot);
            } catch (IOException ignored) {
                //ignored
            }
        }
        try (FileWriter writer = new FileWriter(new File("target/dspot/model.als"), false)) {
            writer.write(alloyModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<Object> newValues = AlloyRunner.run("target/dspot/model.als");
        if (newValues.isEmpty()) {
            return null;
        }
        testMethod.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
            @Override
            public boolean matches(CtLiteral element) {
                return super.matches(element);// TODO must be implemented
            }
        }).forEach(ctLiteral -> {
            if (ctLiteral.getParent() instanceof CtUnaryOperator) {
                ctLiteral.getParent().replace(ctLiteral.getFactory().createLiteral(newValues.remove(0)));
            } else {
                ctLiteral.replace(ctLiteral.getFactory().createLiteral(newValues.remove(0)));
            }
        });
        return testMethod;
    }

    @Override
    public CtMethod applyRandom(CtMethod ctMethod) {
        return null;
    }

    @Override
    public void reset(CtType ctType) {
        this.testClass = ctType;
    }

    private Launcher init(final InputConfiguration configuration) throws IOException {
        AutomaticBuilderFactory.reset();
        final InputProgram program = InputConfiguration.initInputProgram(configuration);
        program.setProgramDir(DSpotUtils.computeProgramDirectory.apply(configuration));
        configuration.setInputProgram(program);
        AutomaticBuilder builder = AutomaticBuilderFactory.getAutomaticBuilder(configuration);
        String dependencies = builder.buildClasspath(program.getProgramDir());
        if (configuration.getProperty("additionalClasspathElements") != null) {
            dependencies = dependencies + AmplificationHelper.PATH_SEPARATOR + program.getProgramDir() + configuration.getProperty("additionalClasspathElements");
        }
        File output = new File(program.getProgramDir() + "/" + program.getClassesDir());
        File outputTest = new File(program.getProgramDir() + "/" + program.getTestClassesDir());
        try {
            FileUtils.cleanDirectory(output);
            FileUtils.cleanDirectory(outputTest);
        } catch (Exception ignored) {
            //ignored
        }
        Launcher spoonModel = instrument(program.getAbsoluteSourceCodeDir(),
                program.getAbsoluteTestSourceCodeDir(),
                dependencies);
        SpoonModelBuilder modelBuilder = spoonModel.getModelBuilder();
        modelBuilder.setBinaryOutputDirectory(output);
        boolean status = modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
        DSpotUtils.copyResources(configuration);
        if (!status) {
            throw new RuntimeException("Error during compilation");
        }
        return spoonModel;
    }

    private Launcher instrument(String pathToSources, String pathToTestSources, String dependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = (pathToSources + AmplificationHelper.PATH_SEPARATOR + pathToTestSources +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/model/" +
                AmplificationHelper.PATH_SEPARATOR + "src/main/java/fr/inria/stamp/alloy/builder/"
        ).split(AmplificationHelper.PATH_SEPARATOR);
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        if (!dependencies.isEmpty()) {
            String[] dependenciesArray = dependencies.split(AmplificationHelper.PATH_SEPARATOR);
            launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        }
        launcher.buildModel();
        launcher.addProcessor(new InvocationInstrumenterProcessor());
        launcher.addProcessor(new ExecutableInstrumenterProcessor());
        launcher.addProcessor(new ConstraintInstrumenterProcessor());
        launcher.addProcessor(new ModificationInstrumenterProcessor());
        launcher.process();
        launcher.getFactory()
                .Class()
                .getAll()
                .stream()
                .flatMap(ctType -> ctType.getMethods().stream())
                .filter(AmplificationChecker::isTest)
                .forEach(TestInstrumentation::instrument);
        return launcher;
    }
}