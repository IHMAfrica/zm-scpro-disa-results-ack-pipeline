package zm.gov.moh.hie.scp;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zm.gov.moh.hie.scp.deserializer.ResultAckDeserializer;
import zm.gov.moh.hie.scp.dto.ResultAck;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;

public class StreamingJob {
    private static final Logger LOG = LoggerFactory.getLogger(StreamingJob.class);

    public static void main(String[] args) throws Exception {
        // Load effective configuration (CLI > env > defaults)
        final Config cfg = Config.fromEnvAndArgs(args);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Build Kafka source for ACK messages
        KafkaSource<ResultAck> source = KafkaSource.<ResultAck>builder()
                .setBootstrapServers(cfg.kafkaBootstrapServers)
                .setTopics(cfg.kafkaTopic)
                .setGroupId(cfg.kafkaGroupId)
                .setProperty("enable.auto.commit", "true")
                .setProperty("auto.commit.interval.ms", "2000")
                .setProperty("max.poll.interval.ms", "10000")
                .setProperty("max.poll.records", "50")
                .setProperty("request.timeout.ms", "2540000")
                .setProperty("delivery.timeout.ms", "120000")
                .setProperty("default.api.timeout.ms", "2540000")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new ResultAckDeserializer())
                .setProperty("security.protocol", cfg.kafkaSecurityProtocol)
                .setProperty("sasl.mechanism", cfg.kafkaSaslMechanism)
                .setProperty("sasl.jaas.config",
                        "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                                "username=\"" + cfg.kafkaSaslUsername + "\" " +
                                "password=\"" + cfg.kafkaSaslPassword + "\";")
                .build();

        // Create a DataStream from Kafka source
        DataStream<ResultAck> kafkaStream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka ACK Source"
        ).startNewChain();

        // Filter out null values to prevent issues with the sink
        DataStream<ResultAck> filteredStream = kafkaStream
                .filter(resultAck -> {
                    if (resultAck == null) {
                        LOG.warn("Filtered out null ResultAck");
                        return false;
                    }
                    if (resultAck.getResultMessageRefId() != null) {
                        LOG.info("Processing ResultAck with message control ID: {}", resultAck.getResultMessageRefId());
                    } else {
                        LOG.info("Processing ResultAck with no message control ID");
                    }
                    return true;
                })
                .name("Filter Null Values").disableChaining();

        // Add JdbcSink with batch configuration
        // Use CTE to atomically insert into data.message (to get auto-generated id)
        // and then insert into crt.result_ack using that id
        @SuppressWarnings("deprecation")
        var sink = JdbcSink.sink(
                "WITH inserted_message AS (" +
                "  INSERT INTO data.message (message_type, data) VALUES ('ACK', ?) RETURNING id " +
                ") " +
                "INSERT INTO crt.result_ack (id, code, result_message_ref_id, message, created_at, received_at) " +
                "SELECT inserted_message.id, ?, ?, ?, ?, ? FROM inserted_message",
                (PreparedStatement statement, ResultAck resultAck) -> {
                    statement.setString(1, resultAck.getRawMessage());
                    statement.setString(2, resultAck.getCode());
                    statement.setString(3, resultAck.getResultMessageRefId());
                    statement.setString(4, resultAck.getMessage());
                    if (resultAck.getCreatedAt() != null) {
                        statement.setTimestamp(5, Timestamp.valueOf(resultAck.getCreatedAt()));
                    } else {
                        statement.setNull(5, Types.TIMESTAMP);
                    }
                    statement.setTimestamp(6, Timestamp.valueOf(resultAck.getReceivedAt()));
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(cfg.jdbcUrl)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(cfg.jdbcUser)
                        .withPassword(cfg.jdbcPassword)
                        .build()
        );
        filteredStream.addSink(sink).name("Postgres JDBC -> Result ACK Sink");

        // Execute the pipeline
        env.execute("Kafka to Postgres Results ACK Pipeline");
    }
}
