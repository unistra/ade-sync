package fr.unistra.dnum.ade;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitClient {
    private ConnectionFactory factory;
    private Connection connection;
    private String queue;

    public static Logger logger = LoggerFactory.getLogger("rabbit"); //$NON-NLS-1$

    public RabbitClient(JSONObject rabbitConf) {
        factory = new ConnectionFactory();
        factory.setHost(rabbitConf.getString("server"));
        factory.setUsername(rabbitConf.getString("username"));
        factory.setPassword(rabbitConf.getString("password"));
        factory.setPort(rabbitConf.getInt("port"));
        queue = rabbitConf.getString("queue");
    }

    public void send(JSONObject doc) throws IOException, TimeoutException {
        send(doc.toString());
    }

    public void send(String message) throws IOException, TimeoutException {
        if(connection == null)
            connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);
        channel.basicPublish("", queue, null, message.getBytes());
        logger.info("Sent message to " + queue);
    }

    public void close() throws IOException {
        connection.close();
    }
}
