package meeuwer.kalah;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ClassIntrospecter;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import meeuwer.kalah.transport.WsGameEndpoint;

import javax.servlet.ServletException;

public class App {

    private static final String HOSTNAME = System.getProperty("hostname", "localhost");
    private static final int PORT = Integer.getInteger("port", 8080);

    private static final int WEBSOCKET_BYTEBUFFER_POOL_SIZE = 256;
    private static final String DEPLOYMENT_NAME = "kalah.war";

    public static void main(String[] args) throws ServletException {
        String contextPath = "/";
        Injector injector = Guice.createInjector(new AppModule());

        WebSocketDeploymentInfo deploymentInfo = new WebSocketDeploymentInfo()
                .setBuffers(new DefaultByteBufferPool(true, WEBSOCKET_BYTEBUFFER_POOL_SIZE))
                .addEndpoint(WsGameEndpoint.class);

        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(App.class.getClassLoader())
                .setContextPath(contextPath)
                .setDeploymentName(DEPLOYMENT_NAME)
                .setClassIntrospecter(new GuiceClassIntrospector(injector))
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, deploymentInfo);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        PathHandler pathHandler = Handlers.path()
                .addPrefixPath("/", manager.start())
                .addPrefixPath("/static", Handlers.resource(
                        new ClassPathResourceManager(App.class.getClassLoader(), "static")
                ))
                .addExactPath("/", Handlers.redirect("/static/index.html"));

        Undertow server = Undertow.builder()
                .addHttpListener(PORT, HOSTNAME)
                .setHandler(pathHandler)
                .build();

        server.start();
    }

    private static class GuiceClassIntrospector implements ClassIntrospecter {

        private Injector injector;

        public GuiceClassIntrospector(Injector injector) {
            this.injector = injector;
        }

        @Override
        public <T> InstanceFactory<T> createInstanceFactory(Class<T> klass) throws NoSuchMethodException {
            return new ImmediateInstanceFactory<>(injector.getInstance(klass));
        }

    }

}
