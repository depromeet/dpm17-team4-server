SHELL := /bin/sh

PORT ?= 8080
SPRING_PROFILES ?= dev # pg
EXTRA_ARGS ?=

GRADLE := ./gradlew
PID_FILE := .server.pid
LOG_DIR := logs
LOG_FILE := $(LOG_DIR)/server.log

.PHONY: help build build-no-test jar run start stop restart status logs test clean curl format format-check clear-h2

help:
	@echo "Available targets:"
	@echo "  make build           - Clean and build the project (runs tests)"
	@echo "  make build-no-test   - Clean and build the project (skip tests)"
	@echo "  make jar             - Build executable Spring Boot JAR"
	@echo "  make run             - Run app in foreground via Gradle (CTRL+C to stop)"
	@echo "  make start           - Start app in background from built JAR"
	@echo "  make stop            - Stop background app (via PID file)"
	@echo "  make restart         - Restart background app"
	@echo "  make status          - Show background app status"
	@echo "  make logs            - Tail background logs"
	@echo "  make test            - Run tests"
	@echo "  make clean           - Clean build artifacts"
	@echo "  make curl            - Curl sample endpoint (/api/hello)"
	@echo "  make format          - Auto-format code (Spotless)"
	@echo "  make format-check    - Check formatting only (fails if changes needed)"
	@echo "  make clear-h2        - Remove local H2 files (.h2/)"
	@echo ""
	@echo "Variables:"
	@echo "  PORT=<int>                 (default: 8080)"
	@echo "  SPRING_PROFILES=<profiles> (default: dev; e.g., dev,local)"
	@echo "  EXTRA_ARGS=\"--key=val\"    (extra Spring Boot args)"

build:
	$(GRADLE) clean build

build-no-test:
	$(GRADLE) clean build -x test

jar:
	$(GRADLE) bootJar

# Foreground run using Gradle (good for development)
run:
	$(GRADLE) bootRun --args="--server.port=$(PORT) $(if $(SPRING_PROFILES),--spring.profiles.active=$(SPRING_PROFILES)) $(EXTRA_ARGS)"

# Background run using the built JAR
start: jar
	@mkdir -p $(LOG_DIR)
	@JAR_FILE="$$(ls -1t build/libs/*SNAPSHOT*.jar 2>/dev/null | grep -v -- '-plain\\.jar' | head -n1)"; \
	if [ -z "$$JAR_FILE" ]; then \
		echo "No JAR found under build/libs. Run 'make jar' first."; \
		exit 1; \
	fi; \
	if [ -f "$(PID_FILE)" ] && kill -0 $$(cat "$(PID_FILE)") 2>/dev/null; then \
		echo "App already running with PID $$(cat $(PID_FILE)). Use 'make restart' or 'make stop'."; \
		exit 0; \
	fi; \
	echo "Starting $$JAR_FILE on port $(PORT)..."; \
	nohup java -jar "$$JAR_FILE" --server.port=$(PORT) $(if $(SPRING_PROFILES),--spring.profiles.active=$(SPRING_PROFILES)) $(EXTRA_ARGS) > /dev/null 2>&1 & echo $$! > "$(PID_FILE)"; \
	echo "Started with PID $$(cat $(PID_FILE)). Logs: logs/server.log (app), logs/access*.log (access)"

stop:
	@if [ -f "$(PID_FILE)" ]; then \
		PID=$$(cat "$(PID_FILE)"); \
		if kill -0 $$PID 2>/dev/null; then \
			echo "Stopping PID $$PID..."; \
			kill $$PID; \
			sleep 1; \
			if kill -0 $$PID 2>/dev/null; then \
				echo "Force killing PID $$PID..."; \
				kill -9 $$PID || true; \
			fi; \
		else \
			echo "No running process for PID $$PID."; \
		fi; \
		rm -f "$(PID_FILE)"; \
	else \
		echo "No PID file found ($(PID_FILE))."; \
	fi

restart: stop start

status:
	@if [ -f "$(PID_FILE)" ]; then \
		PID=$$(cat "$(PID_FILE)"); \
		if kill -0 $$PID 2>/dev/null; then \
			echo "Running (PID $$PID)"; \
		else \
			echo "Not running (stale PID file: $$PID)"; \
		fi; \
	else \
		echo "Not running"; \
	fi

logs:
	@echo "Tailing logs at $(LOG_FILE) (CTRL+C to stop)"; \
		test -f "$(LOG_FILE)" || { echo "Log file does not exist yet. Start the app first."; exit 1; }; \
		tail -f "$(LOG_FILE)"

test:
	$(GRADLE) test

format:
	$(GRADLE) spotlessApply

format-check:
	$(GRADLE) spotlessCheck

clean:
	$(GRADLE) clean
	@rm -f "$(PID_FILE)"

clear-h2:
	@echo "Removing local H2 files at .h2/ ..."; \
	rm -rf .h2 || true; \
	echo "Done."

curl:
	@echo "GET http://localhost:$(PORT)/api/hello"; \
	curl -sS http://localhost:$(PORT)/api/hello || true; echo
