FROM python:3.11-slim AS development

WORKDIR /workspace
ENV PYTHONPATH=/workspace
COPY services/document-ocr/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt
EXPOSE 8087
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087", "--reload"]

FROM python:3.11-slim AS runtime

WORKDIR /app
ENV PYTHONPATH=/app
COPY services/document-ocr/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt
COPY services/document-ocr/app /app/app
EXPOSE 8087
ENTRYPOINT ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087"]
