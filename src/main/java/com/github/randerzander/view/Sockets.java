package com.github.randerzander.view;

import org.apache.ambari.view.ViewContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

@WebServlet(name = "WebSocket Servlet", urlPatterns = { "/sockets" })
public class Sockets extends WebSocketServlet {
    private ViewContext viewContext;
    private Properties props;

    private ConsumerConnector consumer;
    private ExecutorService executor;
    private Session session;

    @Override
    public void configure(WebSocketServletFactory factory) {
        ServletContext context = super.getServletContext();
        viewContext = (ViewContext) context.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);
        Map<String, String> viewProps = this.viewContext.getProperties();

        Properties props = new Properties();
        props.put("zookeeper.connect", viewProps.get("zookeeper.connect"));
        props.put("group.id", UUID.randomUUID().toString());
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

        factory.getPolicy().setIdleTimeout(10000);
        factory.register(EventSocket.class);
    }

    @WebSocket
    public class EventSocket {
      private Session session;
      @OnWebSocketMessage
      public void onMessage(Session session, String message){
        System.out.println("SOCKET got TEXT message: " + message + ", from " + session);
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(message, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

        executor = Executors.newFixedThreadPool(1);
        for (final KafkaStream stream : consumerMap.get(message)) executor.submit(new Consumer(stream, session));
      }
      
      @OnWebSocketConnect
      public void onConnect(Session session){ System.out.println("SOCKET Connected: " + session); this.session = session; }
      @OnWebSocketClose
      public void onClose(Session session, int code, String reason){ System.out.println("SOCKET Closed: " + reason); }
      @OnWebSocketError
      public void onError(Throwable cause){ cause.printStackTrace(System.err); }
    }

    private class Consumer implements Runnable {
      private KafkaStream stream;
      private Session session;
      public Consumer(KafkaStream stream, Session session) { this.stream = stream; this.session = session; }
   
      public void run() {
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while(true){
          try{
            while (it.hasNext())
              session.getRemote().sendString(new String(it.next().message()));
            Thread.sleep(300);
          }catch (Exception e) { e.printStackTrace(); return; }
        }
      }
    }
}
