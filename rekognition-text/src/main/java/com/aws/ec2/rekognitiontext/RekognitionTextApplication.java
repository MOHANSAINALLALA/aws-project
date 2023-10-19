package com.aws.ec2.rekognitiontext;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.aws.ec2.rekognitiontext.config.AWSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

class QueueListener implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(QueueListener.class);

	private static final String BUCKET_NAME = "object-text-reko";

	@Override
	public void onMessage(Message message) {
		AWSConfig awsConfig = new AWSConfig();

		try {
			AmazonRekognition rekognitionClient = awsConfig.amazonRekognitionClient();
			String messageText = ((TextMessage) message).getText();

			ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(BUCKET_NAME);
			ListObjectsV2Result result = awsConfig.amazonS3Client().listObjectsV2(req);

			for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
				String photo = objectSummary.getKey();

				if (photo.contains(messageText)) {
					DetectTextRequest request = new DetectTextRequest()
							.withImage(new Image()
									.withS3Object(new S3Object()
											.withName(photo)
											.withBucket(BUCKET_NAME)));

					try {
						DetectTextResult result1 = rekognitionClient.detectText(request);
						List<TextDetection> textDetections = result1.getTextDetections();

						if (!textDetections.isEmpty()) {
							logger.info("Text Detected lines and words for {}: ", photo);
							for (TextDetection text : textDetections) {
								logger.info("Text Detected: {}, Confidence: {}", text.getDetectedText(),
										text.getConfidence());
							}
						}
					} catch (AmazonRekognitionException e) {
						logger.error("Error: {}", e.getMessage());
					}
				}
			}
		} catch (JMSException e) {
			logger.error("An error occurred: {}", e.getMessage());
		}
	}
}

@SpringBootApplication
public class RekognitionTextApplication {
	private static final Logger logger = LoggerFactory.getLogger(RekognitionTextApplication.class);
	private static final String QUEUE_NAME = "queue-cars.fifo";

	public static void main(String[] args) throws JMSException {
		SpringApplication.run(RekognitionTextApplication.class, args);

		AWSConfig awsConfig = new AWSConfig();

		try {
			createQueueIfNotExists(awsConfig);

			SQSConnectionFactory connectionFactory = awsConfig.sqsConnectionFactory();
			SQSConnection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(QUEUE_NAME);
			MessageConsumer consumer = session.createConsumer(queue);
			consumer.setMessageListener(new QueueListener());
			connection.start();
			Thread.sleep(10000);

		} catch (AmazonServiceException | InterruptedException e) {
			logger.error("An error occurred: {}", e.getMessage());
		}
	}

	private static void createQueueIfNotExists(AWSConfig awsConfig) {
		try {
			SQSConnectionFactory connectionFactory = awsConfig.sqsConnectionFactory();
			SQSConnection connection = connectionFactory.createConnection();
			AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
			if (!client.queueExists(QUEUE_NAME)) {
				Map<String, String> attributes = new HashMap<>();
				attributes.put("FifoQueue", "true");
				attributes.put("ContentBasedDeduplication", "true");
				client.createQueue(new CreateQueueRequest().withQueueName(QUEUE_NAME).withAttributes(attributes));
			}
			connection.close();
		} catch (QueueNameExistsException e) {
			logger.info("Queue already exists: {}", QUEUE_NAME);
		} catch (JMSException e) {
			logger.error("An error occurred while creating the queue: {}", e.getMessage());
		}
	}
}
