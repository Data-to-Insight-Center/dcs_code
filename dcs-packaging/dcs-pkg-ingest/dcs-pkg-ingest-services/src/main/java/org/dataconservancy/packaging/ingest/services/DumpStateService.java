package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.*;
import org.dataconservancy.packaging.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Dumps the state of the ingest workflow to an internally configured {@code Logger} instance at {@code INFO} level.
 */
public class DumpStateService extends BaseIngestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Invokes {@code toString()} or {@code toString(HierarchicalPrettyPrinter)} on the objects in the
     * {@code IngestWorkflowState}.  Of the two methods, {@code toString(HierarchicalPrettyPrinter)} is preferred.  The
     * output is logged at {@code INFO} level.
     * <p/>
     * Therefore, to get the most information from this ingest service, the components in the
     * {@code IngestWorkflowState} should have, at minimum, a reasonable implementation of {@code toString()}.  Classes
     * in the {@code IngestWorkflowState} can optionally implement {@code toString(HierarchicalPrettyPrinter)}, and use
     * the methods on the pretty printer to indent the output.
     *
     * @param depositId the deposit identifier, must not be empty or {@code null}
     * @param state the state associated with identified deposit, must not be {@code null}, and must have its components
     *              set
     * @throws StatefulIngestServiceException
     */
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);

        final EventManager em = state.getEventManager();
        final AttributeSetManager asm = state.getAttributeSetManager();
        final Integer ingestPhase = state.getIngestPhase().getPhaseNumber();
        final Package pkg = state.getPackage();

        final HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();
        hpp.append("State for deposit ").appendWithNewLine(depositId);
        hpp.incrementDepth();

        // Ingest Phase
        hpp.appendWithIndent("Current Ingest Phase: ").appendWithNewLine(String.valueOf(ingestPhase));

        // The Package
        hpp.appendWithIndentAndNewLine("Package State: ");
        hpp.incrementDepth();
        if (hasHpp(pkg)) {
            invokeHpp(hpp, pkg);
        } else {
            hpp.appendWithIndentAndNewLine(pkg.toString());
        }
        hpp.decrementDepth();

        // The Event Manager
        hpp.appendWithIndentAndNewLine("Event Manager State: ");
        hpp.incrementDepth();
        if (hasHpp(em)) {
            invokeHpp(hpp, em);
        } else {
            hpp.appendWithIndentAndNewLine(em.toString());
        }
        hpp.decrementDepth();

        // The AttributeSetManager
        hpp.appendWithIndentAndNewLine("Attribute Set Manager State: ");
        hpp.incrementDepth();
        if (hasHpp(asm)) {
            invokeHpp(hpp, asm);
        } else {
            hpp.appendWithIndentAndNewLine(asm.toString());
        }
        hpp.decrementDepth();

        log.info(hpp.toString());
    }

    /**
     * Uses reflection to determine if the supplied object has a {@code toString(HierarchicalPrettyPrinter)} method.
     *
     * @param o any object
     * @return true if {@code toString(HierarchicalPrettyPrinter)} is present
     */
    private boolean hasHpp(Object o) {
        try {
            return o.getClass().getDeclaredMethod("toString", HierarchicalPrettyPrinter.class) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Invokes {@code toString(HierarchicalPrettyPrinter)} on the supplied object using reflection.
     *
     * @param hpp a HierarchicalPrettyPrinter instance
     * @param o the object to invoke {@code toString(HierarchicalPrettyPrinter)} on
     */
    private void invokeHpp(HierarchicalPrettyPrinter hpp, Object o) {
        try {
            Method hppToString = o.getClass().getDeclaredMethod("toString", HierarchicalPrettyPrinter.class);
            hppToString.invoke(o, hpp);
        } catch (NoSuchMethodException e) {
            log.warn(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.warn(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.warn(e.getMessage(), e);
        }
    }
}
