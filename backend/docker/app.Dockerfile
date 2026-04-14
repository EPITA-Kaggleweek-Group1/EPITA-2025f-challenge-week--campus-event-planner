FROM python:3.14-slim

WORKDIR /app

# dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# clone src
COPY src ./src

# setup python path
ENV PYTHONPATH=/app/src

# run python
CMD ["python", "src/app.py"]
