package com.github.randerzander.view;

import com.github.randerzander.view.KafkaConsumer;
import com.github.randerzander.view.EventSocket;

import org.apache.ambari.view.ViewContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

      System.err.println("INITING RT-VIEW!");

      consumer = new KafkaConsumer("seregiondev01:2181", UUID.randomUUID().toString(), "syslog");
      consumer.run(1);
      
      System.err.println("RAN KAFKATHREAD!");

      try { Thread.sleep(10000); }
      catch (Exception e) { e.printStackTrace(); System.exit(-1); }
      consumer.shutdown();

      // set a 10 second idle timeout
      factory.getPolicy().setIdleTimeout(10000);
      // register my socket
      factory.register(EventSocket.class);
    }
}
