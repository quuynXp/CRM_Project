# ==========================================
# STAGE 1: BUILDER
# ==========================================
FROM python:3.10-slim-bullseye AS builder

WORKDIR /opt/venv
RUN python -m venv .
ENV PATH="/opt/venv/bin:$PATH"

RUN apt-get update && apt-get install -y \
    build-essential \
    libsndfile1-dev \
    libjpeg-dev \
    zlib1g-dev \
    libfreetype6-dev \
    libpq-dev \
    libssl-dev \
    libffi-dev \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip setuptools wheel

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt \
    --index-url https://download.pytorch.org/whl/cpu || \
    pip install --no-cache-dir -r requirements.txt

# ==========================================
# STAGE 2: FINAL RUNTIME
# ==========================================
FROM python:3.10-slim-bullseye

RUN apt-get update && apt-get install -y --no-install-recommends \
    libsndfile1 \
    libjpeg62-turbo \
    libfreetype6 \
    zlib1g \
    libpq5 \
    libssl-dev \
    ca-certificates \
    libasound2 \
    libasound2-plugins \
    wget \
    && rm -rf /var/lib/apt/lists/*

RUN update-ca-certificates

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app

COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
ENV PYTHONPATH="/app"

COPY . ./

RUN chmod +x ./start_services_local.sh

EXPOSE 8001 50051

CMD ["./start_services_local.sh"]