package sap.ai.st.cm.plugins.ciintegration.odataclient;

import com.google.common.net.UrlEscapers;
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
import sap.ai.st.cm.plugins.ciintegration.CIIntegrationGlobalConfiguration;

public class CMODataClient {

    private final ODataClient client;
    private final CIIntegrationGlobalConfiguration configuration;

    public CMODataClient(CIIntegrationGlobalConfiguration configuration) {

        this.configuration = configuration;

        this.client = ODataClientFactory.getClient();
        this.client.getConfiguration().setHttpClientFactory(new CMOdataHTTPFactory(this.configuration.getServiceUser(), this.configuration.getServicePassword()));

    }

    public CMODataChange getChange(String ChangeID) throws Exception {

        URI entityUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendEntitySetSegment("Changes").appendKeySegment(ChangeID).build();

        ODataEntityRequest<ClientEntity> request = this.client.getRetrieveRequestFactory().getEntityRequest(entityUri);

        request.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntity> response = request.execute();

        return new CMODataChange(ChangeID, response.getBody().getProperty("Status").getValue().toString());

    }

    public ArrayList<CMODataTransport> getChangeTransports(String ChangeID) throws Exception {

        URI entityUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendEntitySetSegment("Changes"
        ).appendKeySegment(ChangeID).appendNavigationSegment("Transports").build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(entityUri);

        request.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> iterator = response.getBody();

        ArrayList<CMODataTransport> transportList = new ArrayList<>();

        while (iterator.hasNext()) {

            ClientEntity transport = iterator.next();

            transportList.add(new CMODataTransport(transport.getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(transport.getProperty("IsModifiable").getValue().toString())));
        }

        return transportList;
    }

    public void uploadFileToTransport(String TransportID, String filePath, String ApplicationID) throws IOException {

        File file = new File(filePath);

        URIBuilder uribuilder = this.client.newURIBuilder(this.configuration.getServiceURL()).appendEntitySetSegment("Files");

        URI fileStreamUri = uribuilder.build();

        fileStreamUri = URI.create(fileStreamUri.toString() + "(TransportID='" + TransportID + "',FileID='" + file.getName() + "',ApplicationID='" + ApplicationID + "')");

        try (FileInputStream fileStream = new FileInputStream(file)) {

            ODataMediaEntityUpdateRequest createMediaRequest = this.client.getCUDRequestFactory().getMediaEntityUpdateRequest(fileStreamUri, fileStream);

            createMediaRequest.addCustomHeader("x-csrf-token", getCSRFToken());
            createMediaRequest.setFormat(ContentType.APPLICATION_ATOM_XML);

            String mimeType = URLConnection.guessContentTypeFromName(file.getName());

            if (!mimeType.isEmpty()) {

                createMediaRequest.setContentType(mimeType);

            }

            ODataPayloadManager streamManager = createMediaRequest.payloadManager();

            ODataResponse createMediaResponse = streamManager.getResponse();

            if (createMediaResponse.getStatusCode() != 204) {

                throw new IOException(createMediaResponse.getRawResponse().toString());

            }

            createMediaResponse.close();

        }
    }

    public void releaseDevelopmentTransport(String ChangeID, String TransportID) throws Exception {

        URI functionUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendActionCallSegment("releaseTransport").build();

        functionUri = URI.create(functionUri.toString() + "?ChangeID='" + ChangeID + "'" + "&TransportID='" + TransportID + "'");

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        functionInvokeRequest.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataInvokeResponse<ClientEntity> response = functionInvokeRequest.execute();

        if (response.getStatusCode() != 200) {

            throw new IOException(response.getRawResponse().toString());

        }
    }

    public CMODataTransport createDevelopmentTransport(String ChangeID) throws Exception {

        URI functionUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendActionCallSegment("createTransport").build();

        functionUri = URI.create(functionUri.toString() + UrlEscapers.urlFragmentEscaper().escape("?ChangeID='" + ChangeID + "'"));

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        functionInvokeRequest.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataInvokeResponse<ClientEntity> response = functionInvokeRequest.execute();

        if (response.getStatusCode() != 200) {

            throw new IOException(response.getRawResponse().toString());

        }

        return new CMODataTransport(response.getBody().getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(response.getBody().getProperty("IsModifiable").getValue().toString()));
    }

    public CMODataTransport createDevelopmentTransportAdvanced(String ChangeID, String Description, String Owner) throws Exception {

        URI functionUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendActionCallSegment("createTransportAdvanced").build();

        functionUri = URI.create(functionUri.toString() + UrlEscapers.urlFragmentEscaper().escape("?ChangeID='" + ChangeID + "'" + "&Description='" + Description + "'" + "&Owner='" + Owner + "'"));

        ODataInvokeRequest<ClientEntity> functionInvokeRequest = this.client.getInvokeRequestFactory().getFunctionInvokeRequest(functionUri, ClientEntity.class);

        functionInvokeRequest.setAccept(ContentType.APPLICATION_ATOM_XML.toContentTypeString());

        ODataInvokeResponse<ClientEntity> response = functionInvokeRequest.execute();

        if (response.getStatusCode() != 200) {

            throw new IOException(response.getRawResponse().toString());

        }

        return new CMODataTransport(response.getBody().getProperty("TransportID").getValue().toString(), Boolean.parseBoolean(response.getBody().getProperty("IsModifiable").getValue().toString()));
    }

    private String getCSRFToken() {

        URI metadataUri = this.client.newURIBuilder(this.configuration.getServiceURL()).appendMetadataSegment().build();

        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.client.getRetrieveRequestFactory().getEntitySetIteratorRequest(metadataUri);

        request.addCustomHeader("X-CSRF-Token", "Fetch");

        request.setAccept(ContentType.APPLICATION_XML.toContentTypeString());

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();

        return response.getHeader("X-CSRF-Token").iterator().next();

    }
}
