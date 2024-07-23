SOURCES := $(wildcard src/**/*.java lib/*.java) # 源文件：src lib
LIB := $(wildcard lib/*.jar) # jar 包

empty :=
space :=$(empty) $(empty)
OUTDIR := target
TARGET := pvzscript.jar
CONFIG := MANIFEST.MF

LIBSTR0 := "$(patsubst %.jar,%.jar;,$(LIB))"
LIBSTR := $(subst $(space),$(empty),$(LIBSTR0))


MODULES := java.base,java.sql,java.desktop
JAVA_HOME := "D:\Program Files\Java\jdk-17.0.2"
CUSTOM_JRE_DIR := jre

.PHONY : target

target : clean javac extract jar

jre : 
	jlink --module-path $(JAVA_HOME)/jmods --add-modules $(MODULES) --output $(CUSTOM_JRE_DIR)

run :
	java -jar $(TARGET)

javac :
	javac -nowarn -encoding utf-8 -classpath $(LIBSTR) -d $(OUTDIR) $(SOURCES)

jar :
	jar -cvfm $(TARGET) $(CONFIG) -C $(OUTDIR) . static $(LIB)

clean :
	if exist $(OUTDIR) rmdir /q /s $(OUTDIR)
	if exist $(TARGET) del /q $(TARGET)

extract : 
	$(foreach lib, $(LIB),cd $(OUTDIR) && jar -xf ../$(lib) && cd .. &&) rmdir /q /s "target/META-INF"

update :
	powershell ./build.sh

exe :
	D:\TOOLS\launch4j\launch4jc.exe launchconfig.xml
