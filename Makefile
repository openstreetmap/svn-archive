.PHONY: clean

deploy: install_testserver java/dist/OSMApplet.jar
	@./install_testserver
	@echo
	@echo Build finished.
	@echo Now point your Apache to deploy/ subdirectory
	@echo An example config files is other/apache-conf.sites-available

java/dist/OSMApplet.jar:
	@cd java && ant dist

clean:
	@rm -rf deploy
	@cd java && ant clean

