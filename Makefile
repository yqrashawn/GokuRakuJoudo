GRAALVM_ARG=-H:+ReportUnsupportedElementsAtRuntime --no-server
JAR_NAME=karabiner-configurator-0.1.0-SNAPSHOT-standalone
TARGET_JAR=target/$(JAR_NAME).jar
all:
	$(MAKE) clean
	$(MAKE) compile
	$(MAKE) bin
clean:
	lein clean
compile:
	lein compile && lein uberjar
bin:
	native-image $(GRAALVM_ARG) -jar $(TARGET_JAR)
	mv $(JAR_NAME) goku
test-binary:
	mkdir -p ~/.config/karabiner
	cp ./resources/configurations/edn/yqrashawn.edn ~/.config/karabiner.edn
	cp ./resources/configurations/json/empty-karabiner.json ~/.config/karabiner/karabiner.json
	chmod +w ~/.config/karabiner/karabiner.json
	./goku
local:
	make -f Makefile.local