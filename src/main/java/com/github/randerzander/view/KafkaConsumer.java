package com.github.randerzander.view;

import com.github.randerzander.view.ConsumerTest;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumer {
    //Kafka consumer
    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;

    public KafkaConsumer(String zookeeper, String groupId, String topic) {
      System.err.println("CONSTRUCTOR!");
      Properties props = new Properties();
      props.put("zookeeper.connect", zookeeper);
      props.put("group.id", groupId);
      props.put("zookeeper.session.timeout.ms", "400");
      props.put("zookeeper.sync.time.ms", "200");
      props.put("auto.commit.interval.ms", "1000");
      consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
      this.topic = topic;
    }
 
    public void shutdown() {
      if (consumer != null) consumer.shutdown();
      if (executor != null) executor.shutdown();
      try {
        if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
            System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
        }
      } catch (InterruptedException e) {
        System.out.println("Interrupted during shutdown, exiting uncleanly");
      }
    }

    public void run(int threads) {
      System.err.println("RUNNING!");
      Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
      topicCountMap.put(topic, new Integer(threads));
      Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
      List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

      // launch threads
      executor = Executors.newFixedThreadPool(threads);

      // now create an object to consume the messages
      int threadNumber = 0;
      for (final KafkaStream stream : streams) {
        executor.submit(new ConsumerTest(stream, threadNumber));
        threadNumber++;
      }
    }
}
