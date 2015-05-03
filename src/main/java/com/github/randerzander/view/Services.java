package com.github.randerzander.view;

import com.github.randerzander.view.KafkaConsumer;

import org.apache.ambari.view.ViewContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class Services extends HttpServlet {
    private ViewContext viewContext;
    private KafkaConsumer consumer;

    @Override
    public void init(ServletConfig config) throws ServletException {
      super.init(config);
      ServletContext context = config.getServletContext();
      viewContext = (ViewContext) context.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);

      System.err.println("INITING RT-VIEW!");

      consumer = new KafkaConsumer("seregiondev01:2181", UUID.randomUUID().toString(), "syslog");
      consumer.run(1);
      
      System.err.println("RAN KAFKATHREAD!");

      try { Thread.sleep(10000); }
      catch (Exception e) { e.printStackTrace(); }
      consumer.shutdown();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

}
