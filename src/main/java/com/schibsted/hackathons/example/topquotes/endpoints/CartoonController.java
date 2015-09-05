package com.schibsted.hackathons.example.topquotes.endpoints;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.ribbon.RibbonRequest;
import com.schibsted.hackathons.example.topquotes.ribbon.GotCartoonRibbonClient;
import com.schibsted.hackathons.example.topquotes.ribbon.GotDilbertRibbonClientImpl;
import com.schibsted.hackathons.example.topquotes.ribbon.GotGarfieldRibbonClientImpl;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.channel.StringTransformer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;

import javax.ws.rs.HttpMethod;
import java.nio.charset.Charset;

/**
 * The controller uses the RibbonClient exposed methods to retrieve the other service content.
 * Once received, it transforms it and returns.
 */
@Singleton
@Endpoint
public class CartoonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartoonController.class);
    private GotCartoonRibbonClient gotCartoonRibbonClient = null;

/*
    @Inject
    public CartoonController(GotCartoonRibbonClient gotCartoonRibbonClient) {
        this.gotCartoonRibbonClient = gotCartoonRibbonClient;
    }
*/



    // This endpoint shouldn't be removed since will be always needed
    // Healthcheck endpoint needed by Asgard to validate the service is working
    @Path(value = "/healthcheck", method = HttpMethod.GET)
    public Observable<Void> healthcheck(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        response.setStatus(HttpResponseStatus.OK);
        response.write("{\"status\":\"ok\"}", StringTransformer.DEFAULT_INSTANCE);
        return response.close();
    }

    // This endpoint shouldn't be removed since will be always needed
    // Healthcheck endpoint needed by Prana to validate the service is working
    @Path(value = "/Status", method = HttpMethod.GET)
    public Observable<Void> status(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        response.setStatus(HttpResponseStatus.OK);
        response.write("Eureka new!", StringTransformer.DEFAULT_INSTANCE);
        return response.close();
    }



    @Path(value = "/api/cartoon", method = HttpMethod.GET)
    public Observable<Void> getCartoon(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        double value = Math.random();

        GotCartoonRibbonClient client;
        if (value > 0.5d) {
            gotCartoonRibbonClient = new GotDilbertRibbonClientImpl();
        } else {
            gotCartoonRibbonClient = new GotGarfieldRibbonClientImpl();
        }

        return gotCartoonRibbonClient.triggerGetCartoon()
                .toObservable()
                .flatMap(originContent -> {
                    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

                    try {
                        JSONObject content = new JSONObject(originContent.toString(Charset.defaultCharset()));
                        JSONObject resultContent = new JSONObject();
                        resultContent.put("img", content.get("img"));
                        return response.writeAndFlush(resultContent.toString(), StringTransformer.DEFAULT_INSTANCE);
                    } catch (JSONException e) {
                        return response.writeAndFlush("{\"error\": \"No trending quote yet!\"}",
                                StringTransformer.DEFAULT_INSTANCE);
                    }
                })
                .doOnCompleted(() -> response.close(true));
    }

}