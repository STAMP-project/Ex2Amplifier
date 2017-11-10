package fr.inria.stamp.instrumentation.processor;

import fr.inria.diversify.utils.AmplificationChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/11/17
 */
public abstract class InstrumenterProcessor<T extends CtElement> extends AbstractProcessor<T> {

    @Override
    public boolean isToBeProcessed(T candidate) {
        return !candidate.getParent(CtPackage.class).getQualifiedName().startsWith("fr.inria.stamp") &&
                !(candidate.getParent(CtMethod.class) != null &&
                    AmplificationChecker.isTest(candidate.getParent(CtMethod.class))
                );
    }

}
