/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.packaging.ingest.api;

/**
 * Package related string constants.
 */
public interface Package {

    /**
     * Enumeration of package types
     */
    public interface Types {

        /**
         * A package conforming to the Data Conservancy BagIt Profile 1.0
         */
        public static final String BAGIT_DCS_10 = "bagit/dcs-1.0";
    }

    /**
     *  Enumeration of core ingest-relevant event types.
      * <p>
      * These events can be considered common to the ingest process, and ought to be
      * understood by those that need to reason about a general ingest process.
      * </p>
      * <p>
      * In addition to anything specified in individual javadoc, ingest events SHOULD
      * have an <code>eventDetail</code> that provides a human-readable description
      * of the event. These can be arbitrarily mundane.
      * </p>
     */
    public interface Events {

        /**
         * Archive has stored the entities within a SIP.
         * <p>
         * Indicates that a SIP has been sent to the archive. May represent an add,
         * or an update.
         * </p>
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>Number of entities archived</dd>
         * <dt>eventTarget</dt>
         * <dd>every archived entity</dd>
         * </dl>
         * </p>
         */
        public static final String ARCHIVE = "archive";

        /**
         * Files have been extracted from a SIP.
         * <p>
         * Indicates that a file
         * </p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>Number of files extracted from the package</dd>
         * <dt>eventDetail</dt>
         * <dd>SIP identifier</dd>
         * <dt>eventTarget</dt>
         * <dd>path of every extracted file</dd>
         * </dl>
         */
        public static final String FILE_EXTRACTION = "file.extraction";

        /**
         * Signifies that an entity has been identified as a member of a specific
         * batch load process.
         * <p>
         * There may be an arbitrary number of independent events signifying the
         * same batch (same outcome, date, but different sets of targets). A unique
         * combination of date and outcome (batch label) identify a batch.
         * </p>
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>Batch label/identifier</dd>
         * <dt>eventTarget</dt>
         * <dd>Entities in a batch</dd>
         * </dl>
         * </p>
         */
        public static final String BATCH = "batch";

        /**
         * File format characterization.
         * <p>
         * Indicates that a format has been verifiably characterized. Format
         * characterizations not accompanied by a corresponding characterization
         * event can be considered to be unverified.
         * </p>
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>format, in the form "scheme formatId" (whitespace separated)</dd>
         * <dt>eventTarget</dt>
         * <dd>name of characterized file</dd>
         * </dl>
         * </p>
         */
        public static final String CHARACTERIZATION_FORMAT =
                "characterization.format";

        /**
         * Metadata has been generated about a file or object in the package.
         * <p>
         * Indicates that some sort of calculation or extraction has produced an
         * attribute set containing metadata.
         * </p>
         * *
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>attributeSet in the format of AttributeSetName object or file name (whitespace seperated).</dd>
         * <dt>eventTarget</dt>
         * <dd>id of File or object the metadata describes</dd>
         * </dl>
         */
        public static final String METADATA_GENERATED =
                "characterization.metadata";

        /**
         * A fixity has been generated for a file in the package.
         * <p>
         * Indicates that a fixity has been verifiably calculated for a given file and for a certain hashing algorithm. Fixities
         * not accompanied by a corresponding fixity event can be considered to be unverified.
         * </p>
         * *
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>fixity, in the form "algorithm hash" (whitespace separated)</dd>
         * <dt>eventTarget</dt>
         * <dd>name of File the hash was generated for</dd>
         * </dl>
         */
        public static final String FIXITY_CALCULATED =
                "checksum.calculated";

        /**
         * Initial deposit/transfer of an item into the DCS, preceding ingest.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>SIP identifier uid</dd>
         * <dt>eventTarget</dt>
         * <dd>id of deposited entity</dd>
         * </dl>
         * </p>
         */
        public static final String DEPOSIT = "deposit";

        /**
         * Content retrieved by dcs.
         * <p>
         * Represents the fact that content has been downloaded/retrieved by the
         * dcs.
         * </p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>http header-like key/value pairs representing circumstances
         * surrounding upload</dd>
         * <dt>eventTarget</dt>
         * <dd>id of File whose staged content has been downloaded</dd>
         * </dl>
         */
        public static final String FILE_DOWNLOAD = "file.download";

        /**
         * uploaaded/downloaded file content resolution.
         * <p>
         * Indicates that the reference URI to a unit of uploaded or downloaded file
         * content has been resolved and replaced with the DCS file access URI.
         * </p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd><code>reference_URI</code> 'to' <code>dcs_URI</code></dd>
         * <dt>eventTarget</dt>
         * <dd>id of File whose staged content has been resolved</dd>
         * </dl>
         */
        public static final String FILE_RESOLUTION_STAGED = "file.resolution";

        /**
         * Indicates the uploading of file content.
         * <p>
         * Represents the physical receipt of bytes from a client.
         * </p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd>http header-like key/value pairs representing circumstanced
         * surrounding upload</dd>
         * <dt>eventTarget</dt>
         * <dd>id of File whose staged content has been uploaded</dd>
         * </dl>
         */
        public static final String FILE_UPLOAD = "file.upload";

        /**
         * Assignment of an identifier to the given entity, replacing an
         * existing/temporary id. *
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventOutcome</dt>
         * <dd><code>old_identifier</code> 'to' <code>new_identifier</code></dd>
         * <dt>eventTarget</dt>
         * <dd>new id of object</dd>
         * </dl>
         */
        public static final String ID_ASSIGNMENT = "identifier.assignment";

        /**
         * Marks the start of an ingest process.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of all entities an ingest SIP</dd>
         * </dl>
         * </p>
         */
        public static final String INGEST_START = "ingest.start";

        /**
         * Signifies a successful ingest outcome.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of all entities an ingest SIP</dd>
         * </dl>
         * </p>
         */
        public static final String INGEST_SUCCESS = "ingest.complete";

        /**
         * Signifies a failed ingest outcome.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of all entities an ingest SIP</dd>
         * </dl>
         * </p>
         */
        public static final String INGEST_FAIL = "ingest.fail";

        /**
         * Signifies that a feature extraction or transform has successfully
         * occurred.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of a DeliverableUnit or Collection</dd>
         * </dl>
         * </p>
         */
        public static final String TRANSFORM = "transform";

        /**
         * Signifies that a feature extraction or transform failed.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of a DeliverableUnit or Collection</dd>
         * </dl>
         * </p>
         */
        public static final String TRANSFORM_FAIL = "transform.fail";

        /**
         * Signifies a file has been scanned by the virus scanner.
         * <p>
         * Indicates that a file has been scanned by a virus scanner. There could be
         * more than one event for a file.
         * </p>
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of file whose content was scanned</dd>
         * </dl>
         * </p>
         */
        public static final String VIRUS_SCAN = "virus.scan";

        /**
         * Signifies an new deliverable unit is being ingested as an update to the target deliverable unit.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of the deliverable unit being updated</dd>
         * </dl>
         * </p>
         */
        public static final String DU_UPDATE = "du.update";

        /**
         * Signifies the successful verification of format for files in its target.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of the deliverable unit being updated</dd>
         * </dl>
         * </p>
         */
        public static final String FORMAT_VERIFIED = "format.verified";

        /**
         * Signifies the failure in format verification of files in its target.
         * <p>
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventTarget</dt>
         * <dd>id of the deliverable unit being updated</dd>
         * </dl>
         * </p>
         */
        public static final String FORMAT_VERIFICATION_FAILED = "format.verification.fail";

        /**
         * Signifies that an ingest phase has started.
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>outcome</dt>
         * <dd>phase number</dd>
         * <dt>target</dt>
         * <dd>class names of the services to be executed</dd>
         * </dl>
         */
        public static final String INGEST_PHASE_START = "phase.start";

        /**
         * Signifies that an ingest phase has completed.
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>outcome</dt>
         * <dd>phase number</dd>
         * <dt>target</dt>
         * <dd>class names of the services that were executed</dd>
         * </dl>
         */
        public static final String INGEST_PHASE_COMPLETE = "phase.complete";
        
        /**
         * Signifies that a BagIt Package is validated
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>outcome</dt>
         * <dd>phase number</dd>
         * <dt>target</dt>
         * </dl>
         */
        public static final String PACKAGE_VALIDATED = "package.validated";

        /**
         * Signifies that BusinessObjects specified in the Package have been built and assigned businessIds.
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>outcome</dt>
         * <dd>phase number</dd>
         * <dt>target</dt>
         * </dl>
         */
        public static final String BUSINESS_OBJECT_BUILT = "businessobject.built";
        
        /**
         * Signifies that BusinessObjects specified in the Package meet minimum requirements.
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>outcome</dt>
         * <dd>phase number</dd>
         * <dt>target</dt>
         * </dl>
         */
        public static final String BUSINESS_OBJECTS_VALIDATED = "businessobjects.validated";
        
        /**
         * Signifies and unsupported file aggregation.
         * <dl>
         * <dt>eventType</dt>
         * <dd>{@value}</dd>
         * <dt>eventDetail</dt>
         * <dd>Reason phrase explaining why the aggregated bytestream was removed.</dd>
         * <dt>eventOutcome</dt>
         * <dd>(The id of the aggregated bytestream) has been removed from the package and will not be ingested.</dd>
         * <dt>eventTarget</dt>
         * <dd>the id of the aggregated bytestream that's being removed</dd>
         * </dl>
         */
        public static final String UNSUPPORTED_FILE_AGGREGATION = "unsupported.aggregation";
    }

}