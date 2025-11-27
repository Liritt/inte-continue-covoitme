HOST ?= http://localhost:8080
USERS ?= 20
SPAWN_RATE ?= 5
RUN_TIME ?= 1m

test:
	@docker run --rm --network=host \
		-v "$$(pwd)":/mnt/locust \
		-w /mnt/locust \
		locustio/locust \
		-f locustfile.py \
		--host=$(HOST) \
		--headless \
		--users=$(USERS) \
		--spawn-rate=$(SPAWN_RATE) \
		--run-time=$(RUN_TIME) \
		--html=report.html \
		--csv=results

clean: ## Nettoie les fichiers générés
	@rm -f report*.html results*.csv
