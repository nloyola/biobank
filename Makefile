.PHONY: all build-biobank-docker run-biobank-docker-local

DOCKER_RELEASE_TAG=v1.0
ME=$(shell whoami)

all: build-biobank-docker

build-biobank-docker:
	docker build --build-arg myuser=$(shell whoami) -t biobank-$(ME) -f docker/Dockerfile .

run-biobank-docker: build-biobank-docker
	docker run -ti --rm biobank-$(ME)

run-biobank-docker-local: build-biobank-docker
	docker run -ti --rm \
        -e HOME=${HOME} \
        -v "${PWD}:${HOME}/src/biobank" \
        -v /etc/group:/etc/group:ro \
        -v /etc/passwd:/etc/passwd:ro \
        --security-opt seccomp=unconfined \
        -e DISPLAY=${DISPLAY} \
        -v /tmp/.X11-unix:/tmp/.X11-unix \
        -u $(shell id -u ${USER} ):$(shell id -g ${USER} ) \
        biobank-$(ME)

help:
	@echo 'Available targets:'
	@echo ''
	@echo '  all (default):            Build the biobank docker iamge'
	@echo '  build-biobank-docker:     Build the biobank docker iamge'
	@echo '  run-biobank-docker:       Runs the biobank docker image as root'
	@echo '  run-biobank-docker-local: Runs the biobank docker iamge as you'
