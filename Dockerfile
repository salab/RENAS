FROM python:3.11
USER root

RUN apt-get update && apt-get install -y \
    maven \
    openjdk-17-jre
RUN pip install --upgrade pip

COPY requirements.txt /root
COPY downloadNLTK.py /root
RUN pip install --no-cache-dir -r /root/requirements.txt
RUN python /root/downloadNLTK.py