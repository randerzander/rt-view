package com.github.randerzander.view;

import com.github.randerzander.view.KafkaConsumer;

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

public class Sockets extends WebSocketServlet {
    private ViewContext viewContext;
    private KafkaConsumer consumer;

    @Override
    public void configure(WebSocketServletFactory factory){
      ServletContext context = super.getServletContext();
      viewContext = (ViewContext) context.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);

      /*
      consumer = new KafkaConsumer("seregiondev01:2181,seregiondev02:2181,seregiondev03:2181", UUID.randomUUID().toString(), "syslog");
      consumer.run(1);
      try { Thread.sleep(10000); }
      catch (Exception e) { e.printStackTrace(); System.exit(-1); }
      consumer.shutdown();
      */

      factory.getPolicy().setIdleTimeout(10000);
      factory.register(EventSocket.class);
    }

    @ClientEndpoint
    @ServerEndpoint(value="/events/")
    public class EventSocket
    {
        @OnOpen
        public void onWebSocketConnect(Session sess){
          System.out.println("SOCKET Connected: " + sess);
        }
        
        @OnMessage
        public void onWebSocketText(String message){
          System.out.println("SOCKET got TEXT message: " + message);
        }
        
        @OnClose
        public void onWebSocketClose(CloseReason reason){
          System.out.println("SOCKET Closed: " + reason);
        }
        
        @OnError
        public void onWebSocketError(Throwable cause){
          cause.printStackTrace(System.err);
        }
    }

}
