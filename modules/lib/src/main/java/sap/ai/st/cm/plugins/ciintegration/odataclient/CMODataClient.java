package sap.ai.st.cm.plugins.ciintegration.odataclient;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.ODataPayloadManager;
import org.apache.olingo.client.api.communication.request.invoke.ODataInvokeRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.streamed.ODataMediaEntityUpdateRequest;
import org.apache.olingo.client.api.communication.response.ODataInvokeResponse;
import org.apache.olingo.client.api.communication.response.ODataResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.domain.ClientValue;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.serialization.AtomDeserializer;
import org.apache.olingo.commons.api.format.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;

public class CMODataClient implements AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(CMODataClient.class);

    private boolean isClosed = false;

    private final static ContentType contentType = ContentType.APPLICATION_ATOM_XML;

    private String serviceUrl; //REVISIT: uri instead of string?
    private final ODataClient client;

    public CMODataClient(String serviceUrl, String serviceUser, String servicePassword) {

        checkArgument(!isBlank(serviceUrl), "Service url must not be blank.");
        checkArgument(!isBlank(serviceUser), "Service user must not be blank.");
        checkArgument(!isBlank(servicePassword), "Service password must not be blank.");
        this.serviceUrl = serviceUrl;
        this.client = ODataClientFactory.getClient();
        this.client.getConfiguration().setHttpClientFactory(
                new CMOdataHTTPFactory(serviceUser, servicePassword));
        logger.debug(format("CMClient instanciated for host '%s' with service user '%s'.", serviceUrl, serviceUser));
    }

    public CMODataChange getChange(String changeID) {

        logger.trace(format("Entering 'getChange'. changeID: '%s'.", changeID));
        checkClosed();

        URI entityUri = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Changes").appendKeySegment(changeID).build();

        ODataEntityRequest<ClientEntity> request = this.client.getRetrieveRequestFactory().getEntityRequest(entityUri);

        request.setAccept(contentType.toContentTypeString());

        ODataRetrieveResponse<ClientEntity> response = null;
        try {
            response = request.execute();
            ClientEntity body = response.getBody();
            String changeId = body.getProperty("ChangeID").getValue().toString();
            if(!changeID.equals(changeId))
                throw new RuntimeException(
                    format("ChangeId contained in server response ('%s') does not match request change (%s).", changeId, changeID));
            boolean isInDevelopment = Boolean.valueOf(body.getProperty("IsInDevelopment").getValue().toString());
            logger.debug(format("Change '%s' found. isInDevelopment: '%b'", changeID, isInDevelopment));
            return new CMODataChange(changeID, isInDevelopment);
        } catch(RuntimeException e) {
            logger.error(format("%s caught while getting change '%s'.", e.getClass().getName(), changeID), e);
            throw e;
        } finally {
            if(response != null) {
                response.close();
            }
            logger.trace(format("Exiting 'getChange'. changeID: '%s'.", changeID));
        }
    }

    public ArrayList<CMODataTransport> getChangeTransports(String changeID) {

        logger.trace(format("Entering 'getChangeTransports'. changeID: '%s'.", changeID));

        checkClosed();

        checkArgument(!isBlank(changeID), format("changeID was null or empty: '%s'.", changeID));

        URI entityUri = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Changes")
            .appendKeySegment(changeID).appendNavigationSegment("Transports").build();

        logger.debug(format("Entity URI for getting transports for change id '%s': '%s'.", changeID, entityUri.toASCIIString()));
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(entityUri);

        request.setAccept(contentType.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = null;

        try {
            response = request.execute();

            ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = response.getBody();

            ArrayList<CMODataTransport> transportList = new ArrayList<>();

            while (iterator.hasNext()) {
                transportList.add(toTransport(changeID, iterator.next()));
            }

            if(transportList.isEmpty()) {
                logger.debug(format("No transports found for change document '%s'.", changeID));
            } else {
                logger.debug(format("%d transports found for change document '%s'.", transportList.size(), changeID));
            }
            return transportList;
        } catch(RuntimeException e) {
            logger.error(format("%s caught while retrieving transports for change document '%s'.",
                e.getClass().getName(), changeID));
            throw e;
        } finally {
            if(response != null) {
                response.close();
            }
            logger.trace(format("Exiting 'getChangeTransports'. changeID: '%s'.", changeID));
        }
    }

    public void uploadFileToTransport(String changeID, String transportID, String filePath, String applicationID) throws IOException, CMODataClientException {
        logger.trace(format("Entering 'uploadFileToTransport'. changeID: '%s', transportId: '%s', filePath: '%s', applicationId: '%s'.",
                changeID, transportID, filePath, applicationID));
        checkClosed();

        File file = new File(filePath);

        if(!file.canRead()) {
            throw new CMODataClientException(format("Cannot upload file '%s' to transport '%s'. File cannot be read.", file.getAbsolutePath(), transportID));
        }

        URIBuilder uribuilder = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Files");

        URI fileStreamUri = uribuilder.build();

        fileStreamUri = URI.create(fileStreamUri.toString() + "(ChangeID='" + changeID + "',TransportID='" + transportID + "',FileID='" + file.getName() + "',ApplicationID='" + applicationID + "')");

        logger.debug(format("File stream URI for uploading file '%s' to transport '%s' for change id '%s': '%s'.",
            file.getAbsolutePath(), changeID, transportID,  fileStreamUri.toASCIIString()));

        ODataResponse createMediaResponse = null;
        try (FileInputStream fileStream = new FileInputStream(file)) {

            ODataMediaEntityUpdateRequest createMediaRequest = this.client.getCUDRequestFactory().getMediaEntityUpdateRequest(fileStreamUri, fileStream);

            createMediaRequest.addCustomHeader("x-csrf-token", getCSRFToken());
            createMediaRequest.setFormat(contentType);

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());

            if (! Strings.isNullOrEmpty(mimeType)) {

                createMediaRequest.setContentType(mimeType);
                logger.debug(format("Assuming mime type '%s' for file '%s'.", mimeType, file.getName()));

            } else {
                logger.warn(format("Cannot derive mimetype from file name '%s'", file.getName()));
            }

            ODataPayloadManager streamManager = createMediaRequest.payloadManager();

            createMediaResponse = streamManager.getResponse();

            checkStatus(createMediaResponse, 204);

            logger.debug(format("File '%s' uploaded to transport '%s' for change id '%s' with application id '%s'.",
                    filePath, transportID, changeID, applicationID));

        } catch(IOException | CMODataClientException | RuntimeException e) {
            logger.error(format("%s caught while uploading file '%s' to transport '%s' for change id '%s' with application id '%s'.",
                    e.getClass().getName(), filePath, transportID, changeID, applicationID));
            throw e;
        } finally {
            if(createMediaResponse != null) {
                createMediaResponse.close();
            }
            logger.trace(format("Exiting 'uploadFileToTransport'. changeID: '%s', transportId: '%s', filePath: '%s', applicationId: '%s'.",
                    changeID, transportID, filePath, applicationID));

        }
    }

    public void releaseDevelopmentTransport(String changeID, String transportID) throws CMODataClientException {

        logger.trace(format("Entering 'releaseDevelopmentTransport'. changeID: '%s', transportId: '%s'.",
                changeID, transportID));

        checkClosed();

        if(StringUtils.isBlank(changeID)) throw new IllegalArgumentException(format("ChangeID is null or blank: '%s'.", changeID));
        if(StringUtils.isBlank(transportID)) throw new IllegalArgumentException(format("TransportID is null or blank: '%s'.", transportID));

        URI functionUri = getFunctionURI("releaseTransport", "?ChangeID='" + changeID + "'" + "&TransportID='" + transportID + "'");

        logger.debug(format("Function URI for releassing transport '%s' for change id '%s': '%s'.",
           transportID, changeID, functionUri.toASCIIString()));

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        ODataInvokeResponse<ClientEntity> response = null;

        try {
            response = executeRequest(functionInvokeRequest, 200);
            logger.debug(format("Transport request '%s' for change document '%s' released.",
                    transportID, changeID));
        } catch(CMODataClientException | RuntimeException e) {
            logger.error(format("%s caught while releasing transport '%s' for change document '%s'.",
                e.getClass().getName(), transportID, changeID));
            throw e;
        } finally {
            if(response != null) {
                response.close();
                logger.trace(format("Exiting 'releaseDevelopmentTransport'. changeID: '%s', transportId: '%s'.",
                        changeID, transportID));
            }
        }
    }

    /**
     * Creates a transport without description and owner.
     * @param changeID The identifier of the change for that the transport is created.
     * @return An instance representing the transport.
     * @throws CMODataClientException In case the transport could not be created.
     */
    public CMODataTransport createDevelopmentTransport(String changeID) throws CMODataClientException {
        return _createDevelopmentTransport(changeID, "createTransport", "?ChangeID='" + changeID + "'");
    }

    public CMODataTransport createDevelopmentTransportAdvanced(String changeID, String description, String owner) throws Exception {
        return _createDevelopmentTransport(changeID, "createTransportAdvanced", "?ChangeID='" + changeID + "'" + "&Description='" + description + "'" + "&Owner='" + owner + "'");
    }

    /**
     * Releases any resources attached to this client.
     * Was introduced due to issues with hanging threads as some kind of workaround.
     * <b>Should <i>not</i> be used in case of server environments.</b>
     * The synchronization mechanism is not perfect since the executor service might be shutdown
     * while the client is used by other threads. Would be better to synchronize the methods in total.
     * But this would sequentialize all the requests, which is not acceptable in case of server environments.
     */
    public void close() {
        synchronized(this.client) {
            if(!isClosed) {
                this.isClosed = true;
                this.client.getConfiguration().getExecutor().shutdown();
            }
        }
        //not sure if there are other resources ...
    }

    public boolean isClosed() {
        synchronized (this.client) {
            return isClosed;
        }
    }

    static CMODataTransport toTransport(String changeID, ClientEntity transportEntity) {

        String transportId = getValueAsString("TransportID", transportEntity);
        checkState(!isBlank(transportId), format("Transport id found to be null or empty when retrieving transports for change '%s'.", changeID));

        String bModifiable = getValueAsString("IsModifiable", transportEntity);
        checkState(!isBlank(bModifiable), format("Modifiable flag found to be null or empty when retrieving transports for change '%s'.", changeID));

        String description = getValueAsString("Description", transportEntity);
        String owner = getValueAsString("Owner", transportEntity);

        logger.debug(format("Transport '%s' retrieved for change document '%s'. isModifiable: '%s', Owner: '%s' ,Description: '%s'.",
            transportId, changeID, bModifiable, owner, description));

        return new CMODataTransport(
                transportId,
                parseBoolean(bModifiable),
                description,
                owner);
    }

    private void checkClosed() {
        if(isClosed()) {
            IllegalStateException e = new IllegalStateException(format("This instance (%s) has been closed.",
                   getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this))));
            logger.warn("Client has already been closed.", e);
            throw e;
        }
    }

    private CMODataTransport _createDevelopmentTransport(String changeID, String segment, String query) throws CMODataClientException {

        logger.trace(format("Entering '_createDevelopmentTransport'. ChangeID: '%s', Segment: '%s', Query: '%s'.",
                changeID, segment, query));

        checkClosed();

        URI functionUri = getFunctionURI(segment, query);

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        ODataInvokeResponse<ClientEntity> response = null;
        try {

            response = executeRequest(functionInvokeRequest, 200);

            CMODataTransport transport = toTransport("", response.getBody());

            logger.debug(format("Transport '%s' created for change document '%s'. isModifiable: '%b', Description: '%s', Owner: '%s'.",
                    transport.getTransportID(), changeID, transport.isModifiable(), transport.getDescription(), transport.getOwner()));

            return transport;
        } catch(CMODataClientException | RuntimeException e) {
            logger.error(format("%s caught while creating transport for change '%s'.", e.getClass().getName(), changeID));
            throw e;
        } finally {
            if(response != null) {
                response.close();
            }
            logger.trace(format("Exiting '_createDevelopmentTransport'. ChangeID: '%s', Segment: '%s', Query: '%s'.",
                    changeID, segment, query));
        }
    }

    private ODataInvokeResponse<ClientEntity> executeRequest(ODataInvokeRequest<ClientEntity> functionInvokeRequest, int returnCode) throws CMODataClientException {
        functionInvokeRequest.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());
        ODataInvokeResponse<ClientEntity> response = functionInvokeRequest.execute();
        checkStatus(response, returnCode);
        return response;
    }

    private String getCSRFToken() {

        URI metadataUri = this.client.newURIBuilder(serviceUrl).appendMetadataSegment().build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(metadataUri);

        request.addCustomHeader("X-CSRF-Token", "Fetch");

        // here we have in fact xml rather than atom+xml
        request.setAccept(ContentType.APPLICATION_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        return response.getHeader("X-CSRF-Token").iterator().next();

    }

    private URI getFunctionURI(String segment, String query) {
        URI functionUri = this.client.newURIBuilder(serviceUrl).appendActionCallSegment(segment).build();
        return URI.create(functionUri.toString() + UrlEscapers.urlFragmentEscaper().escape(query));
    }

    private void checkStatus(ODataResponse response, int expectedStatusCode) throws CMODataClientException {

        if (response.getStatusCode() != expectedStatusCode) {

            IOException suspressed = null;

            String bodyContent = "<n/a>";

            try {
                /*
                 *  Does not work for http-4xx and http.5xx since Olingo prefers to answer
                 *  with an exception in this case rather with an response entity. But it might
                 *  be helpful also for inexpected http responses outside that range. 
                 */
                bodyContent = IOUtils.toString(response.getRawResponse());
            } catch(IOException e) {
                suspressed = e;
                logger.warn("Cannot read response body content.", e);
            }

            CMODataClientException e = new CMODataClientException(
                format("Response status code '%d' does not match expected status code '%d'. Response body: '%s'.",
                    response.getStatusCode(), expectedStatusCode, bodyContent));
            if(suspressed != null) e.addSuppressed(suspressed);
            throw e;
        }
    }

    private static String getValueAsString(String key, ClientEntity transport) {
        ClientProperty property = transport.getProperty(key);
        if(property == null) return null;
        ClientValue value = property.getValue();
        if(value == null) return null;
        return value.toString();
    }

    public static String getShortVersion() {
        Properties vProps = getVersionProperties();
        return (vProps == null) ? "<n/a>" : vProps.getProperty("mvnProjectVersion", "<n/a>");
    }

    public static String getLongVersion() {
        Properties vProps = getVersionProperties();
        return (vProps == null) ? "<n/a>" : format("%s : %s",
                                              vProps.getProperty("mvnProjectVersion", "<n/a>"),
                                              vProps.getProperty("gitCommitId", "<n/a>"));
    }

    private static Properties getVersionProperties() {
        try(InputStream version = CMODataClient.class.getResourceAsStream("/VERSION")) {
            Properties vProps = new Properties();
            vProps.load(version);
            return vProps;
        } catch(IOException e) {
            logger.warn("Cannot retrieve version.", e);
            return null;
        }
    }

    private static void BAD_HACK_setErrorMessageNameSpace() {

        /*
         * [Q] Why do we have that bad hack?
         * [A] Without that hack we do not get the error message issued by the server.
         *     Within the AtomDeserializer the error response is parsed and marshaled
         *     into an ODataError instance. The 'message' tag which encapsulates the error
         *     message is exptected to have a certain namespace. For whatever reason the
         *     server provides the 'message' tag with a different namespace. Hence the
         *     namespace is updated below accordingly.
         *
         *     Other option would be to switch from xml response format to json response format.
         *     But that also does not work. The json response for getting the transports is expected
         *     to have a 'value' node. But the server provides the transport inside a node named
         *     'd'. Hence nothing is marshaled from json into the corresponding odata like
         *     data transfer entities.
         */
        String ourNamespace = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";

        logger.debug(format("Setting namespace of message qname to '%s'.", ourNamespace));

        try {

            Exception caught = null;

            String errorMessageQNameFieldName = "errorMessageQName",
                   nameSpaceURIFieldName = "namespaceURI";

            final Field errorMessageQNameField = AtomDeserializer.class.getDeclaredField(errorMessageQNameFieldName),
                        namespaceURI = QName.class.getDeclaredField(nameSpaceURIFieldName);

            try {
                namespaceURI.setAccessible(true);
                errorMessageQNameField.setAccessible(true);
                namespaceURI.set(errorMessageQNameField.get(null), ourNamespace);
                logger.info(format("Namespace of message qname set to '%s'.", ourNamespace));

            } catch(Exception e) {
                caught = e;
            } finally {
                try {
                    errorMessageQNameField.setAccessible(false);
                } catch(RuntimeException e) {
                    logger.error(format("Cannot reset accessibility flag for '%s'.", errorMessageQNameFieldName), e);
                    if(caught == null) throw e; else caught.addSuppressed(e);
                }
                try {
                    namespaceURI.setAccessible(false);
                } catch(RuntimeException e) {
                    logger.error(format("Cannot reset accessibility flag for '%s'.", nameSpaceURIFieldName), e);
                    if(caught == null) throw e; else caught.addSuppressed(e);
                }
            }

            if(caught != null) throw caught;

        } catch(Exception e) {

            logger.warn(format("%s caught while setting name space of message qname to '%s'. "
                    + "Retrieving error messages from server is not possible.", e.getClass().getName(), ourNamespace), e);
        }
    }

    static {
        BAD_HACK_setErrorMessageNameSpace();
    }

}
