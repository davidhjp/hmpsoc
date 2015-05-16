ifeq ($(WINDIR),)
	S=:
else
	S=\;
endif

all: clean
	mkdir bin
	javac -cp lib/\*$Ssrc/ src/HMPSoC.java -d bin

compile:
	java -cp bin\;lib/\* JavaPrettyPrinter --ir $(T) | java -cp bin/\;lib/\* HMPSoC -v

jar: clean
	javac -cp lib/\*$Ssrc/ src/HMPSoC.java
	jar -cvfm hmpsoc.jar manifest/manifest -C src .
	mkdir bin
	mv hmpsoc.jar bin
	cp lib/jdom.jar lib/commons-cli-1.3.jar bin
	echo -e '#!/bin/bash\njava -jar $$(dirname $$0)/hmpsoc.jar $$@'> bin/hmpsoc
	chmod u+x bin/hmpsoc

clean:
	rm -rfv bin
	rm -rfv hmpsoc
	find src/ -type f -iname '*.class' -delete

