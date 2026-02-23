package zm.gov.moh.hie.scp.sink;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import zm.gov.moh.hie.scp.dto.ResultAck;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResultAckJdbcSink {

    public static SinkFunction<ResultAck> getSinkFunction(String jdbcUrl, String username, String password) {
        return JdbcSink.sink(
                "INSERT INTO crt.result_ack (id, code, result_message_ref_id, message) " +
                "VALUES (nextval('data.message_id_seq'), ?, ?, ?) " +
                "ON CONFLICT (result_message_ref_id) DO UPDATE SET " +
                "code = EXCLUDED.code, " +
                "message = EXCLUDED.message",
                (PreparedStatement statement, ResultAck resultAck) -> {
                    try {
                        statement.setString(1, resultAck.getCode());
                        statement.setString(2, resultAck.getResultMessageRefId());
                        statement.setString(3, resultAck.getMessage());
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to set parameters for ResultAck", e);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(100)
                        .withBatchIntervalMs(5000)
                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(jdbcUrl)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(username)
                        .withPassword(password)
                        .build()
        );
    }
}
