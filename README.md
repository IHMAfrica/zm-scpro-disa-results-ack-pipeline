# SCPro DISA Results ACK Pipeline

An Apache Flink streaming job that consumes HL7 ACK^R01^ACK messages from Kafka and writes parsed acknowledgment data to PostgreSQL. The job is packaged as a fat jar and designed to run on a Flink session cluster managed by the Flink Kubernetes Operator. CI builds the fat jar and publishes it as a GitHub Release asset.

## Features
- Kafka -> Flink -> PostgreSQL streaming pipeline for HL7 ACK messages
- Parses MSA segment data (acknowledgment code, message control ID, text message)
- Externalized configuration via environment variables and/or command-line arguments
- Deployable via Flink Kubernetes Operator (FlinkSessionJob)

## Build
Requirements:
- JDK 17
- Gradle Wrapper included

Build fat jar:
```bash
./gradlew clean shadowJar
```

Output: `build/libs/zm-scpro-disa-results-ack-pipeline-all.jar`

## Run locally (example)
```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=lab-results-ack
export KAFKA_GROUP_ID=flink-local
export JDBC_URL=jdbc:postgresql://localhost:5432/hie_manager
export JDBC_USER=hie_manager_user
export JDBC_PASSWORD=password

java -jar build/libs/zm-scpro-disa-results-ack-pipeline-*-all.jar \
  --kafka.security.protocol=SASL_PLAINTEXT \
  --kafka.sasl.mechanism=SCRAM-SHA-256 \
  --kafka.sasl.username=admin \
  --kafka.sasl.password=secret
```

## Configuration

Configuration supports three levels of precedence (highest to lowest):
1. **CLI Arguments**: `--key=value`
2. **Environment Variables**: `KAFKA_TOPIC=...`
3. **Built-in Defaults**

### Kafka Configuration
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka brokers
- `KAFKA_TOPIC` - Topic to consume (default: `lab-results-ack`)
- `KAFKA_GROUP_ID` - Consumer group (default: `scpro-disa-results-ack-pipeline`)
- `KAFKA_SECURITY_PROTOCOL` - `SASL_PLAINTEXT` or `PLAINTEXT`
- `KAFKA_SASL_MECHANISM` - `SCRAM-SHA-256`
- `KAFKA_SASL_USERNAME` - Kafka username
- `KAFKA_SASL_PASSWORD` - Kafka password

### Database Configuration
- `JDBC_URL` - PostgreSQL connection URL
- `JDBC_USER` - Database user
- `JDBC_PASSWORD` - Database password

## Kubernetes Deployment

Deploy using FlinkSessionJob:
```bash
kubectl apply -f k8s/fleet/flink-sessionjob.yaml
```

See `k8s/fleet/README.md` for details.
