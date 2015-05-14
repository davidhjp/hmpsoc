ifeq ($(WINDIR),)
	S=:
else
	S=\;
endif

all:
	javac -cp lib/\*$Ssrc/ src/HMPSoC.java
	jar -cvfm hmpsoc.jar manifest/manifest -C src .
	rm -rfv bin
	mkdir bin
	mv hmpsoc.jar bin
	cp lib/jdom.jar lib/commons-cli-1.3.jar bin
	echo -e '#!/bin/bash\njava -jar $$(dirname $$0)/hmpsoc.jar $$@'> bin/hmpsoc
	chmod u+x bin/hmpsoc

clean:
	rm -rfv bin
	find src/ -type f -iname '*.class' -delete

