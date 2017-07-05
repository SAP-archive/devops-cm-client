package sap.ai.st.cm.plugins.ciintegration.odataclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;

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
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ContentType;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;

public class CMODataClient {

    private String serviceUrl; //REVISIT: uri instead of string?
    private final ODataClient client;

    public CMODataClient(String serviceUrl, String serviceUser, String servicePassword) {

        this.serviceUrl = serviceUrl;

        this.client = ODataClientFactory.getClient();
        this.client.getConfiguration().setHttpClientFactory(new CMOdataHTTPFactory(serviceUser, servicePassword));

    }

    public CMODataChange getChange(String ChangeID) throws Exception {

        URI entityUri = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Changes").appendKeySegment(ChangeID).build();

        ODataEntityRequest<ClientEntity> request = this.client.getRetrieveRequestFactory().getEntityRequest(entityUri);

        request.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntity> response = null;
        try {
            response = request.execute();
            return new CMODataChange(ChangeID, response.getBody().getProperty("Status").getValue().toString());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

    public ArrayList<CMODataTransport> getChangeTransports(String ChangeID) throws Exception {

        URI entityUri = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Changes"
        ).appendKeySegment(ChangeID).appendNavigationSegment("Transports").build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(entityUri);

        request.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = null;

        try {
            response = request.execute();

            ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = response.getBody();

            ArrayList<CMODataTransport> transportList = new ArrayList<>();

            while (iterator.hasNext()) {

                ClientEntity transport = iterator.next();

                transportList.add(new CMODataTransport(transport.getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(transport.getProperty("IsModifiable").getValue().toString())));
            }

            return transportList;
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

    public void uploadFileToTransport(String TransportID, String filePath, String ApplicationID) throws IOException {

        File file = new File(filePath);

        URIBuilder uribuilder = this.client.newURIBuilder(serviceUrl).appendEntitySetSegment("Files");

        URI fileStreamUri = uribuilder.build();

        fileStreamUri = URI.create(fileStreamUri.toString() + "(TransportID='" + TransportID + "',FileID='" + file.getName() + "',ApplicationID='" + ApplicationID + "')");

        ODataResponse createMediaResponse = null;
        try (FileInputStream fileStream = new FileInputStream(file)) {

            ODataMediaEntityUpdateRequest createMediaRequest = this.client.getCUDRequestFactory().getMediaEntityUpdateRequest(fileStreamUri, fileStream);

            createMediaRequest.addCustomHeader("x-csrf-token", getCSRFToken());
            createMediaRequest.setFormat(ContentType.APPLICATION_ATOM_XML);

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());

            if (! Strings.isNullOrEmpty(mimeType)) {

                createMediaRequest.setContentType(mimeType);

            }

            ODataPayloadManager streamManager = createMediaRequest.payloadManager();

            createMediaResponse = streamManager.getResponse();
            checkStatus(createMediaResponse, 204);

        } finally {
            if(createMediaResponse != null) {
                createMediaResponse.close();
            }
        }
    }

    public void releaseDevelopmentTransport(String ChangeID, String TransportID) throws Exception {

        URI functionUri = getFunctionURI("releaseTransport", "?ChangeID='" + ChangeID + "'" + "&TransportID='" + TransportID + "'");

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        ODataInvokeResponse<ClientEntity> response = null;

        try {
            response = executeRequest(functionInvokeRequest, 200);
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

    public CMODataTransport createDevelopmentTransport(String ChangeID) throws Exception {
        return _createDevelopmentTransport("createTransport", "?ChangeID='" + ChangeID + "'");
    }

    public CMODataTransport createDevelopmentTransportAdvanced(String ChangeID, String Description, String Owner) throws Exception {
        return _createDevelopmentTransport("createTransportAdvanced", "?ChangeID='" + ChangeID + "'" + "&Description='" + Description + "'" + "&Owner='" + Owner + "'");
    }

    private CMODataTransport _createDevelopmentTransport(String segment, String query) throws IOException {

        URI functionUri = getFunctionURI(segment, query);

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        ODataInvokeResponse<ClientEntity> response = null;
        try {
            response = executeRequest(functionInvokeRequest, 200);
            return new CMODataTransport(response.getBody().getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(response.getBody().getProperty("IsModifiable").getValue().toString()));
        } finally {
            if(response != null) {
                response.close();
            }
        }

    }

    private ODataInvokeResponse<ClientEntity> executeRequest(ODataInvokeRequest<ClientEntity> functionInvokeRequest, int returnCode) throws IOException {
        functionInvokeRequest.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());
        ODataInvokeResponse<ClientEntity> response = functionInvokeRequest.execute();
        checkStatus(response, 200);
        return response;
    }

    private String getCSRFToken() {

        URI metadataUri = this.client.newURIBuilder(serviceUrl).appendMetadataSegment().build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(metadataUri);

        request.addCustomHeader("X-CSRF-Token", "Fetch");

        request.setAccept(ContentType.APPLICATION_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        return response.getHeader("X-CSRF-Token").iterator().next();

    }

    private URI getFunctionURI(String segment, String query) {
        URI functionUri = this.client.newURIBuilder(serviceUrl).appendActionCallSegment(segment).build();
        return URI.create(functionUri.toString() + UrlEscapers.urlFragmentEscaper().escape(query));
    }

    private void checkStatus(ODataResponse response, int expectedStatusCode) throws IOException {
        if (response.getStatusCode() != expectedStatusCode) {
            throw new IOException(response.getRawResponse().toString());
        }
    }
}
