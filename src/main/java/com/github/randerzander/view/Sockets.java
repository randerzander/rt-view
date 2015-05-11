package com.github.randerzander.view;

import org.apache.ambari.view.ViewContext;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.EndpointConfig;

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

@ClientEndpoint
@ServerEndpoint(value="/sockets", configurator=ServletAwareConfig.class)
public class Sockets {
    private Properties props = null;
    private Session session;
    private ConsumerConnector consumer;
    private ExecutorService executor;

    public void init(ViewContext context) {
      Properties props = new Properties();
      props.put("zookeeper.connect", "seregiondev01:2181,seregiondev02:2181,seregiondev03:2181");
      props.put("group.id", UUID.randomUUID().toString());
      props.put("zookeeper.session.timeout.ms", "400");
      props.put("zookeeper.sync.time.ms", "200");
      props.put("auto.commit.interval.ms", "1000");
      consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
    }

    @OnMessage
    public void onMessage(String message){
      System.out.println("SOCKET got TEXT message: " + message + ", from " + session);

      Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
      topicCountMap.put(message, new Integer(1));
      Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

      executor = Executors.newFixedThreadPool(1);
      for (final KafkaStream stream : consumerMap.get(message)) executor.submit(new Consumer(stream, session));
    }
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config){ System.out.println("SOCKET Connected: " + session); this.session = session;
      if (props == null){
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        ServletContext servletContext = httpSession.getServletContext();
        init((ViewContext) servletContext.getAttribute(ViewContext.CONTEXT_ATTRIBUTE));
      }
    }
    @OnClose
    public void onClose(CloseReason reason){ System.out.println("SOCKET Closed: " + reason); }
    @OnError
    public void onError(Throwable cause){ cause.printStackTrace(System.err); }

    private class Consumer implements Runnable {
      private KafkaStream stream;
      private Session session;
      public Consumer(KafkaStream stream, Session session) { this.stream = stream; this.session = session; }
   
      public void run() {
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while(true){
          try{
            while (it.hasNext())
              session.getBasicRemote().sendText(new String(it.next().message()));
            Thread.sleep(300);
          }catch (Exception e) { e.printStackTrace(); return; }
        }
      }
    }
}
