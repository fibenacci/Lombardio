FROM python:3.10-slim AS development

WORKDIR /workspace
ENV PYTHONPATH=/workspace
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgomp1 \
        strace \
        gdb \
        build-essential \
        cmake \
        git \
        libopenblas-dev \
        liblapack-dev \
        libatlas-base-dev \
        python3-dev \
        libjpeg-dev \
        libpng-dev \
        pkg-config && \
    rm -rf /var/lib/apt/lists/*
COPY services/document-ocr/requirements.txt /tmp/requirements.txt
RUN PADDLE_WITH_GLOO=1 PADDLE_WITH_CUDA=0 PADDLE_WITH_DISTRIBUTE=0 PADDLE_BUILD=ON \
    pip install --no-cache-dir --no-binary paddlepaddle -r /tmp/requirements.txt
EXPOSE 8087
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087", "--reload"]

FROM python:3.10-slim AS runtime

WORKDIR /app
ENV PYTHONPATH=/app
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgomp1 \
        strace \
        gdb \
        build-essential \
        cmake \
        git \
        libopenblas-dev \
        liblapack-dev \
        libatlas-base-dev \
        python3-dev \
        libjpeg-dev \
        libpng-dev \
        pkg-config && \
    rm -rf /var/lib/apt/lists/*
COPY services/document-ocr/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt
COPY services/document-ocr/app /app/app
EXPOSE 8087
ENTRYPOINT ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087"]
