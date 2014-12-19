package org.dataconservancy.packaging.ingest.services;

import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_COLLECTION;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_DATAITEM;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_FILE;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_PACKAGE;
import static org.dataconservancy.packaging.model.AttributeSetName.ORE_REM_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATED_BY_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_DATAITEM;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_AGGREGATES_FILE;
import static org.dataconservancy.packaging.model.Metadata.COLLECTION_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_AGGREGATES_FILE;
import static org.dataconservancy.packaging.model.Metadata.DATA_ITEM_IS_PART_OF_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.PACKAGE_AGGREGATES_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.PACKAGE_AGGREGATES_DATAITEM;
import static org.dataconservancy.packaging.model.Metadata.PACKAGE_AGGREGATES_FILE;
import static org.dataconservancy.packaging.model.Metadata.PACKAGE_AGGREGATES_PROJECT;
import static org.dataconservancy.packaging.model.Metadata.PROJECT_AGGREGATES_COLLECTION;
import static org.dataconservancy.packaging.model.Metadata.PROJECT_AGGREGATES_FILE;

/**
 * A base class providing minimal logic for iterating over the relationships between objects in the ORE graph, allowing
 * subclasses execute implementation-dependent tasks.  This class also provides basic error handling; subclasses may
 * populate a {@code List} of error messages and this class will throw StatefulIngestServiceException if the list is
 * not empty.
 */
public abstract class BaseOreChecker extends BaseIngestService {

    /**
     * Contains a list of errors that are encountered when type-checking the AttributeSets. <em>Only</em> for use
     * by unit tests!
     */
    private List<String> errors;

    /**
     * A regular expression Pattern that is meant to match the name of ResourceId Attributes.  For example:
     * 'Project-ResourceId', 'Collection-ResourceId', 'DataItem-ResourceId', etc.  The Attribute should occur exactly
     * once in each ORE-related AttributeSet, serving to uniquely identify the AttributeSet within the package.
     */
    static final Pattern IDENTIFIER_ATTR_NAME = Pattern.compile(".*-ResourceId");

    /**
     * Public, no-arg constructor typically used in Production.
     */
    public BaseOreChecker() {

    }

    /**
     * Accepts a mutable List that is updated with the errors.  <em>Only</em> for use in a test environment!  The
     * implementation is not thread-safe because access to the supplied list is not mediated in any way.
     * <p/>
     * Subclasses are expected to populate this list as appropriate.  The {@link #execute(String, IngestWorkflowState)}
     * method of {@code BaseOreChecker} manages the initial state of this list; subclasses will be able to simply add
     * Strings to the List.
     *
     * @param errors a mutable List populated with errors after each run of the {@code execute(...)} method
     */
    public BaseOreChecker(List<String> errors) {
        this.errors = errors;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation details:<br/>
     * Checks the types of the ORE-related aggregations in the AttributeSetManager.  It manages a {@code List} of
     * errors; as errors are found, they are appended to the {@code List}.  If the List is non-empty at the end of
     * execution, a {@code StatefulIngestServiceException} is thrown.  No events are emitted by this service.
     *
     * @param depositId the deposit identifier, must not be empty or {@code null}
     * @param state the state associated with identified deposit, must not be {@code null}, and must have its components
     *              set
     * @throws StatefulIngestServiceException if any type check fails
     */
    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);

        final List<String> localErrors;

        if (errors == null) {
            localErrors = new ArrayList<String>();
        } else {
            localErrors = errors;
            localErrors.clear();
        }

        final AttributeSetManager asm = state.getAttributeSetManager();

        // Check Package aggregations that aggregate Projects, Files, Collections, and DataItems
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_PROJECT, PACKAGE_AGGREGATES_PROJECT, asm, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_FILE, PACKAGE_AGGREGATES_FILE, asm, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_COLLECTION, PACKAGE_AGGREGATES_COLLECTION, asm, localErrors);
        checkAggregation(ORE_REM_PACKAGE, ORE_REM_DATAITEM, PACKAGE_AGGREGATES_DATAITEM, asm, localErrors);

        // Check Project aggregations that aggregate Files and Collections
        checkAggregation(ORE_REM_PROJECT, ORE_REM_FILE, PROJECT_AGGREGATES_FILE, asm, localErrors);
        checkAggregation(ORE_REM_PROJECT, ORE_REM_COLLECTION, PROJECT_AGGREGATES_COLLECTION, asm, localErrors);

        // Check Collection aggregations that aggregate Collections, DataItems, and Files
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_COLLECTION, COLLECTION_AGGREGATES_COLLECTION, asm, localErrors);
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_DATAITEM, COLLECTION_AGGREGATES_DATAITEM, asm, localErrors);
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_FILE, COLLECTION_AGGREGATES_FILE, asm, localErrors);

        // Check Collections that are part of Other Collections
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_COLLECTION, COLLECTION_IS_PART_OF_COLLECTION, asm, localErrors);

        // Check Collections that are aggregated by Projects
        checkAggregation(ORE_REM_COLLECTION, ORE_REM_PROJECT, COLLECTION_AGGREGATED_BY_PROJECT, asm, localErrors);

        // Check DataItem aggregations that aggregate Files
        checkAggregation(ORE_REM_DATAITEM, ORE_REM_FILE, DATA_ITEM_AGGREGATES_FILE, asm, localErrors);

        // Check DataItems that are aggregated by Collections
        checkAggregation(ORE_REM_DATAITEM, ORE_REM_COLLECTION, DATA_ITEM_IS_PART_OF_COLLECTION, asm, localErrors);

        if (localErrors.size() > 0) {
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < localErrors.size(); i++) {
                msg.append(localErrors.get(i));
                if (i + 1 < localErrors.size()) {
                    msg.append("\n");
                }
            }
            throw new StatefulIngestServiceException(msg.toString());
        }
    }

    /**
     * Subclasses use the supplied parameters to look up AttributeSets from the supplied {@code asm} and perform
     * implementation dependent logic.
     * <p/>
     * Parameters are:
     * <dl>
     *     <dt>aggregatingType</dt>
     *     <dd>the type (name) of the AttributeSet that is aggregating the {@code aggregatedType}; for example
     *     <strong>Ore-Rem-Collection</strong></dd>
     *     <dt>aggregatedType</dt>
     *     <dd>the type (name) of the AttributeSet that is being aggregated by {@code aggregatingType}; for example
     *     <strong>Ore-Rem-DataItem</strong></dd>
     *     <dt>aggregatingRelationship</dt>
     *     <dd>the relationship that is used to aggregate {@code aggregatingType} and {@code aggregatedType}; for
     *     example <strong>Collection-Aggregates-DataItem</strong></dd>
     * </dl>
     *
     * @param aggregatingType the type (name) of the AttributeSet that is aggregating the {@code aggregatedType}
     * @param aggregatedType the type (name) of the AttributeSet that is being aggregated by {@code aggregatingType}
     * @param aggregatingRelationship the relationship that is used to aggregate {@code aggregatingType} and
     *                                {@code aggregatedType}
     * @param asm the AttributeSetManager used by implementations to retrieve AttributeSets
     * @param errors a mutable List populated by subclasses, containing Strings of error messages.
     */
    abstract void checkAggregation(String aggregatingType, String aggregatedType, String aggregatingRelationship,
                                             AttributeSetManager asm, List<String> errors);

}
