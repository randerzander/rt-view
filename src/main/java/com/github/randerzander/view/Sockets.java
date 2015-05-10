package com.github.randerzander.view;

import org.apache.ambari.view.ViewContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
//import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

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

//@WebServlet(name = "WebSocket Servlet", urlPatterns = { "/sockets" })
public class Sockets extends WebSocketServlet {
    private ViewContext viewContext;
    private Properties props;

    private ConsumerConnector consumer;
    private ExecutorService executor;
    private Session session;

    @Override
    public void configure(WebSocketServletFactory factory) {
        ServletContext context = super.getServletContext();
        Properties props = new Properties();
        props.put("zookeeper.connect", "seregiondev01:2181,seregiondev02:2181,seregiondev03:2181");
        props.put("group.id", UUID.randomUUID().toString());
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

        factory.getPolicy().setIdleTimeout(10000);
        factory.register(EventSocket.class);
    }

    //@WebSocket
    @ClientEndpoint
    @ServerEndpoint(value="/sockets")
    public class EventSocket {
      private Session session;
      //@OnWebSocketMessage
      @OnMessage
      //public void onWebSocketMessage(Session session, String message){
      public void onWebSocketMessage(String message){
        System.out.println("SOCKET got TEXT message: " + message + ", from " + session);
        run(this.session, message);
      }
      
      //@OnWebSocketConnect
      @OnOpen
      public void onWebSocketConnect(Session session){ System.out.println("SOCKET Connected: " + session); this.session = session; }
      //@OnWebSocketClose
      @OnClose
      //public void onWebSocketClose(Session session, int code, String reason){ System.out.println("SOCKET Closed: " + reason); }
      public void onWebSocketClose(CloseReason reason){ System.out.println("SOCKET Closed: " + reason); }
      //@OnWebSocketError
      @OnError
      public void onWebSocketError(Throwable cause){ cause.printStackTrace(System.err); }
    }

    public void run(Session session, String topic){
      Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
      topicCountMap.put(topic, new Integer(1));
      Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

      executor = Executors.newFixedThreadPool(1);
      for (final KafkaStream stream : consumerMap.get(topic)) executor.submit(new Consumer(stream, session));
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
              //session.getRemote().sendString(new String(it.next().message()));
              session.getBasicRemote().sendText(new String(it.next().message()));
            Thread.sleep(300);
          }catch (Exception e) { e.printStackTrace(); return; }
        }
      }
    }
}
