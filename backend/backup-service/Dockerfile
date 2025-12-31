FROM alpine:edge

RUN apk add --no-cache \
    postgresql-client \
    rclone \
    bash \
    curl \
    ca-certificates

WORKDIR /app

COPY run.sh /app/run.sh
RUN chmod +x /app/run.sh

ENTRYPOINT ["/app/run.sh"]