package zm.gov.moh.hie.scp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ResultAck implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String resultMessageRefId;
    private String message;
    private String rawMessage;
    private LocalDateTime createdAt;
    private LocalDateTime receivedAt;

    public ResultAck() {}

    public ResultAck(Long id, String code, String resultMessageRefId, String message) {
        this.id = id;
        this.code = code;
        this.resultMessageRefId = resultMessageRefId;
        this.message = message;
    }

    public ResultAck(Long id, String code, String resultMessageRefId, String message, String rawMessage) {
        this.id = id;
        this.code = code;
        this.resultMessageRefId = resultMessageRefId;
        this.message = message;
        this.rawMessage = rawMessage;
    }

    public ResultAck(Long id, String code, String resultMessageRefId, String message, String rawMessage,
                    LocalDateTime createdAt, LocalDateTime receivedAt) {
        this.id = id;
        this.code = code;
        this.resultMessageRefId = resultMessageRefId;
        this.message = message;
        this.rawMessage = rawMessage;
        this.createdAt = createdAt;
        this.receivedAt = receivedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getResultMessageRefId() { return resultMessageRefId; }
    public void setResultMessageRefId(String resultMessageRefId) { this.resultMessageRefId = resultMessageRefId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    @Override
    public String toString() {
        return "ResultAck{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", resultMessageRefId='" + resultMessageRefId + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", receivedAt=" + receivedAt +
                ", rawMessage='" + (rawMessage != null ? rawMessage.substring(0, Math.min(50, rawMessage.length())) + "..." : null) + '\'' +
                '}';
    }
}
