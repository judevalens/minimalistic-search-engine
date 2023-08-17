rebuild:
	mvn package && \
	docker build -t mse .

clusterUp:
	docker compose up