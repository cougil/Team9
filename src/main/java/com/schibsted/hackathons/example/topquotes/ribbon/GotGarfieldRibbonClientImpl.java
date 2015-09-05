package com.schibsted.hackathons.example.topquotes.ribbon;

import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.RibbonRequest;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;
import io.netty.buffer.ByteBuf;

/**
 * Ribbon client which creates the client balancer,
 * specify the endpoint from the remote service to be accessed and wraps the calls to that service.
 */
public class GotGarfieldRibbonClientImpl implements GotCartoonRibbonClient {

    private final HttpResourceGroup httpResourceGroup;
    private final HttpRequestTemplate<ByteBuf> garfieldTemplate;

    public GotGarfieldRibbonClientImpl() {

        // Creation of the client balancer
        // ClientOptions properties are set using the ones defined in AppServer.properties
        httpResourceGroup = Ribbon.createHttpResourceGroup("got-dilbert-service-client");


        garfieldTemplate = httpResourceGroup.newTemplateBuilder("garfieldTemplate", ByteBuf.class)
                .withMethod("GET")
                .withUriTemplate("/api/garfield/{date}")
                .withFallbackProvider(new GotQuotesFallbackHandler()).build();

    }


    public RibbonRequest<ByteBuf>  triggerGetCartoon() {
        return garfieldTemplate.requestBuilder()
                .withRequestProperty("date", "2015-09-05")
                .build();
    }
}


