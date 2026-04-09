FROM mariadb:11.2.4-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends libjemalloc2 \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s "$(ldconfig -p | awk '/libjemalloc/{print $NF; exit}')" /usr/local/lib/libjemalloc.so

ENV LD_PRELOAD=/usr/local/lib/libjemalloc.so
