SOURCES := $(wildcard src/*.java lib/*.java) # 源文件：src lib
LIB := $(wildcard lib/*.jar) # jar 包
empty :=
space :=$(empty) $(empty)
OUTDIR := target
TARGET := pvzscript.jar
CONFIG := MANIFEST.MF
LIBS := ".\lib\amf-serializer-1.5.0.jar;.\lib\commons-beanutils-1.7.0.jar;.\lib\commons-logging-1.1.1.jar"

.PHONY : target

target : clean javac extract jar

run :
	java -jar $(TARGET)

javac :
	javac -nowarn -encoding utf-8 -classpath $(LIBS) -d $(OUTDIR) $(SOURCES)

jar :
	jar -cvfm $(TARGET) $(CONFIG) -C $(OUTDIR) . static $(LIB)

clean :
	if exist $(OUTDIR) rmdir /q /s $(OUTDIR)
	if exist $(TARGET) del /q $(TARGET)

extract : 
	cd $(OUTDIR) && jar -xf ../lib/amf-serializer-1.5.0.jar && cd .. \
	&& cd $(OUTDIR) && jar -xf ../lib/commons-beanutils-1.7.0.jar && cd .. \
	&& cd $(OUTDIR) && jar -xf ../lib/commons-logging-1.1.1.jar && cd .. \
	&& rmdir /q /s "target/META-INF"

update :
	powershell ./build.sh
