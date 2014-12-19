package org.dataconservancy.packaging.ingest.services;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses ORE resource map files as RDF (that is, the RDF is not interpreted as ORE; this service leverages the
 * RDF data model), and determines if there is a single graph.  Because this step operates on the RDF, and not on
 * Attribute Sets, it should be wired prior to the interpretation of the RDF and subsequent production of ORE-related
 * Attribute Sets.
 * <p/>
 * The ORE ReM referenced in the {@code PKG-ORE-REM} attribute of the {@link org.dataconservancy.packaging.model.AttributeSetName#BAGIT_PROFILE_DATACONS_METADATA} attribute set is
 * resolved, and used to produce a unified graph of RDF Resources.  Every RDF Subject is tested to see if it appears as
 * Object of any RDF statement.  Subjects that do not appear as an Object of RDF statements are candidate root nodes of
 * the RDF graph.  If there is more than one candidate root node, this service will throw a
 * {@code StatefulIngestServiceException}.
 * <p/>
 * Note: this service is not thread safe.
 */
public class OrphanResourceChecker extends BaseIngestService {

    /**
     * Error message emitted when there are no roots detected (which should never happen).
     * Parameters are: package ReM URI, extract plus basedir for the package
     */
    private static final String ERR_NO_ROOTS = "Unable to detect roots in the ORE ReM graph for the package URI %s " +
            "(located under %s).  There should be exactly one root.";

    /**
     * Error message emitted when there multiple roots detected.
     * Parameters are: number of possible roots, package ReM URI, extract plus basedir for the package,
     * a single string containing the possible roots
     */
    private static final String ERR_MULTIPLE_ROOTS = "Detected %s roots in the ORE ReM graph for the package URI %s " +
            "(extracted under %s).  There should only be a single root; a portion of the graph is disconnected.  " +
            "Possible roots are: %s";

    /**
     * Contains the candidate root resources that are found after parsing the RDF.  If the RDF is correct, there will
     * only be one root.  If there are multiple roots found, an exception should be thrown.  The state of this
     * List is managed by the {@link #execute(String, org.dataconservancy.packaging.ingest.api.IngestWorkflowState)}
     * method.
     */
    private List<Resource> rootsHolder;

    /**
     * Public, no-arg constructor, which is normally used in production.
     */
    public OrphanResourceChecker() {

    }

    /**
     * Accepts a List which is used to store candidate roots of the RDF graph.  This <em>must not</em> be used in a
     * production environment, because access to the {@code List} is not mediated in any way; it is not protected from
     * concurrent access by multiple threads.
     *
     * @param rootsHolder stores candidate roots of the RDF graph.
     */
    public OrphanResourceChecker(List<Resource> rootsHolder) {
        this.rootsHolder = rootsHolder;
    }

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);

        URI remUri = ResourceMapUtil.getPackageResourceMapURI(state.getAttributeSetManager());

        File baseDir = new File(state.getPackage().getSerialization().getExtractDir(),
                state.getPackage().getSerialization().getBaseDir().getPath()).getParentFile();

        // Look through the model for the ore:isDescribedBy Property, and attempt to load each resource into a Model
        // Create a union of all of the Models.
        Model m = ResourceMapUtil.loadRems(remUri, baseDir);

        final List<Resource> localRootsHolder;
        // Each resource must be the subject of at least one triple
        if (this.rootsHolder == null) {
            localRootsHolder = new ArrayList<Resource>();
        } else {
            localRootsHolder = this.rootsHolder;
            localRootsHolder.clear();
        }
        localRootsHolder.addAll(possibleRoots(m));

        if (localRootsHolder.size() > 1) {
            StringBuilder roots = new StringBuilder("\n");

            for (int i = 0; i < localRootsHolder.size(); i++) {
                roots.append("  <").append(localRootsHolder.get(i)).append(">");
                if ((i + 1) < localRootsHolder.size()) {
                    roots.append("\n");
                }
            }

            final String rootsMsg = String.format(ERR_MULTIPLE_ROOTS, localRootsHolder.size(), remUri.toString(),
                    baseDir.getAbsolutePath(), roots.toString());
            throw new StatefulIngestServiceException(rootsMsg);
        }

        if (localRootsHolder.size() < 1) {
            throw new StatefulIngestServiceException(String.format(ERR_NO_ROOTS, remUri.toString(),
                    baseDir.getAbsolutePath()));
        }
    }

    /**
     * Iterates over each Subject in the RDF model, testing to see if it is used as the Object of any Statement.
     * <p/>
     * If a Subject is not used as an Object of a Statement, it is considered to be a candidate for the root of the
     * RDF graph.
     *
     * @param m the RDF model
     * @return a List of possible roots
     */
    List<Resource> possibleRoots(Model m) {

        // For each Subject, get its rdf:about
        // Make sure that the Subject's rdf:about is referenced in another Subject's Predicate as an rdf:resource
        // If not, then the Subject is either the root Subject, or is not connected to the graph


        Set<Resource> possibleRoots = new HashSet<Resource>();
        ResIterator rIter = m.listSubjects();
        while (rIter.hasNext()) {
            Resource subjectResource = rIter.next();
            log.trace("Testing Subject {} to see if it is the Object of any Statement.", subjectResource);
            if (!m.listStatements(new SimpleSelector(null, null, subjectResource)).hasNext()) {
                // possible root
                log.debug("No Statements found with '{}' as an Object; Subject Resource '{}' is a possible root.",
                        subjectResource, subjectResource);
                possibleRoots.add(subjectResource);
            }
        }

        ArrayList<Resource> listRoots = new ArrayList<Resource>();
        listRoots.addAll(possibleRoots);
        return listRoots;
    }
}
