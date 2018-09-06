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
	mkdir -p $XDG_CONFIG_HOME
	mkdir -p $XDG_CONFIG_HOME/karabiner
	ls $XDG_CONFIG_HOME
	cp ./resources/configurations/yqrashawn.edn $XDG_CONFIG_HOME/karabiner.edn
	cp ./resources/configurations/empty-karabiner.json $XDG_CONFIG_HOME/karabiner/karabiner.json
	ls $XDG_CONFIG_HOME
	ls $XDG_CONFIG_HOME/karabiner
	chmod +w $XDG_CONFIG_HOME/karabiner/karabiner.json
	./goku
local:
	make -f Makefile.local