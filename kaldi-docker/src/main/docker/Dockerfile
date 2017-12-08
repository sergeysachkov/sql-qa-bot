FROM ubuntu:16.04

RUN apt-get update && apt-get install -y  --allow-unauthenticated \
                autoconf \
                automake \
                bzip2 \
                g++ \
                git \
                gstreamer1.0-plugins-good \
                gstreamer1.0-tools \
                gstreamer1.0-pulseaudio \
                gstreamer1.0-plugins-bad \
                gstreamer1.0-plugins-base \
                gstreamer1.0-plugins-ugly  \
                libatlas3-base \
                libgstreamer1.0-dev \
                libtool-bin \
                make \
                python2.7 \
                python3 \
                python-pip \
                python-yaml \
                python-simplejson \
                python-gi \
                subversion \
                wget \
                zlib1g-dev && \
                apt-get clean autoclean && \
                apt-get autoremove -y && \
                pip install ws4py==0.3.2 && \
                pip install tornado && \
                ln -sfn /usr/bin/python2.7 /usr/bin/python && \
                ln -s -f bash /bin/sh

ENV GST_PLUGIN_PATH=/opt/kaldi/src/gst-plugin


WORKDIR /opt

RUN git clone https://github.com/kaldi-asr/kaldi && \
    cd /opt/kaldi/tools && \
    make && \
    ./install_portaudio.sh && \
    cd /opt/kaldi/src && ./configure --shared && \
    make depend && make && \
    cd /opt/kaldi/src/online && make depend && make && \
    cd /opt/kaldi/src/gst-plugin && make depend && make && \
    cd /opt && git clone https://github.com/alumae/kaldi-gstreamer-server && \
    rm -rf /opt/kaldi-gstreamer-server/.git/ && \
    rm -rf /opt/kaldi-gstreamer-server/sample_worker.yaml

ADD start_kaldi.sh /opt/
ADD stop_kaldi.sh /opt/
COPY .provision/sample_worker.yaml /opt/kaldi-gstreamer-server/

RUN chmod +x /opt/start_kaldi.sh && \
    chmod +x /opt/stop_kaldi.sh

EXPOSE 8888