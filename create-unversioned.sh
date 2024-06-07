#!/bin/bash

zip -r biobank_unversioned_v3.10.5.zip \
    lib \
    docker/apache-ant-1.9.0 \
    docker/jdk1.6.0_45 \
    docker/jboss-4.0.5.GA \
    eclipse_ws/biobank.common/lib \
    eclipse_ws/biobank2.tools/lib \
    eclipse_ws/biobank2.tests/lib \
    eclipse_ws/biobank.mvp/lib \
    eclipse_ws/biobank2/lib \
    eclipse_ws/biobank.gui.common/lib
