#!/bin/bash

python /opt/kaldi-gstreamer-server/kaldigstserver/master_server.py --port=8888 2>> /opt/master.log &

python /opt/kaldi-gstreamer-server/kaldigstserver/worker.py -u ws://localhost:8888/worker/ws/speech -c /opt/kaldi-gstreamer-server/sample_worker.yaml 2>> /opt/worker.log &

