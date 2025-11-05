build:
	docker build -t inventory-aging-app .

run:
	docker run -d -p 8080:80 --name inventory-app inventory-aging-app

stop:
	docker stop inventory-app && docker rm inventory-app

restart: stop run

logs:
	docker logs inventory-app

clean:
	docker rmi inventory-aging-app

.PHONY: build run stop restart logs clean