package com.github.randerzander.view;

import org.apache.ambari.view.ViewContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class Sockets extends WebSocketServlet {
    private ViewContext viewContext;
    private Properties props;

    private ConsumerConnector consumer;
    private ExecutorService executor;

    @Override
    public void configure(WebSocketServletFactory factory){
      ServletContext context = super.getServletContext();
      viewContext = (ViewContext) context.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);
      factory.getPolicy().setIdleTimeout(10000);
      factory.register(EventSocket.class);

      Properties props = new Properties();
      props.put("zookeeper.connect", "seregiondev01:2181,seregiondev02:2181,seregiondev03:2181");
      props.put("group.id", UUID.randomUUID().toString());
      props.put("zookeeper.session.timeout.ms", "400");
      props.put("zookeeper.sync.time.ms", "200");
      props.put("auto.commit.interval.ms", "1000");
      consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

      sessions = new HashMap<String, Session>();
      subscribedTopics = new HashMap<String, String>();
    }

    private HashMap<String, Session> sessions;
    private HashMap<String, ArrayList<String>> subscriptions;
    private HashMap<String, String> subscribedTopics;
    @ClientEndpoint
    @ServerEndpoint(value="/ws/")
    public class EventSocket{
      private Session session;

      @OnMessage
      public void onWebSocketText(String message){
        String sessionKey = this.session.getId();
        System.out.println("SOCKET got TEXT message: " + message + ", from " + sessionKey);
        ArrayList<String> sessionSubs;
        if (subscriptions.containsKey(sessionKey)) sessionSubs = (ArrayList<String>)subscriptions.get(sessionKey);
        else sessionSubs = new ArrayList<String>();
        sessionSubs.add(message);
        subscriptions.put(sessionKey, sessionSubs);

        if (!subscribedTopics.containsKey(message)){
          subscribedTopics.put(message, null);
          run(message);
        }
      }
      
      @OnOpen
      public void onWebSocketConnect(Session session){ System.out.println("SOCKET Connected: " + session.getId());
        sessions.put(session.getId(), session);
        this.session=session;
      }
      @OnClose
      public void onWebSocketClose(CloseReason reason){ System.out.println("SOCKET Closed: " + reason);
        sessions.remove(this.session.getId());
      }
      @OnError
      public void onWebSocketError(Throwable cause){ cause.printStackTrace(System.err); }
    }

    public void run(String topic) {
      Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
      topicCountMap.put(topic, new Integer(1));
      Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
      List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

      executor = Executors.newFixedThreadPool(1);
      for (final KafkaStream stream : streams) executor.submit(new Consumer(stream));
    }

    private class Consumer implements Runnable {
    //private class Consumer implements Callable {
      private KafkaStream stream;
      public Consumer(KafkaStream stream) { this.stream = stream; }
   
      public void run() {
      //public ArrayList<String> call() {
        //ArrayList<String> list = new ArrayList<String>();
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()){
          //list.add(new String(it.next().message()));

        }
        //return list;
      }
    }


}
