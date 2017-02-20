include ../aegis-docker/bin/Makefile

static:
	@cd src-web && npm install && bower install && gulp build
