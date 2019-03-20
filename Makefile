update_submodule:
	git submodule update --remote --merge

LIB_PATH=java-advanced-2019/lib
ARTIFACTS_PATH=java-advanced-2019/artifacts
INFO_BASE_MODULE_PATH=${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar

define run_test_with_module
	java -cp ${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.$(1).jar -p out/artifacts/ru_ifmo_rain_yatcheniy_$(1)_jar/ru.ifmo.rain.yatcheniy.$(1).jar;${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.$(1).jar;${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${LIB_PATH} --add-modules ru.ifmo.rain.yatcheniy.$(1) -m info.kgeorgiy.java.advanced.$(1) $(2) ru.ifmo.rain.yatcheniy.$(1).$(3)
endef
#01
test_walk:
	$(call run_test_with_module,walk,RecursiveWalk,RecursiveWalk)
#02
test_array:
	$(call run_test_with_module,arrayset,NavigableSet,ArraySet)
#03
test_student:
	$(call run_test_with_module,student,AdvancedStudentGroupQuery,StudentDB)
#04-06
RU_IMPLEMENTOR_SOURCE_PATH=src/modules/ru.ifmo.rain.yatcheniy.implementor
RU_IMPLEMENTOR_PACKAGE_PATH=$(RU_IMPLEMENTOR_SOURCE_PATH)/ru/ifmo/rain/yatcheniy/implementor
INFO_IMPLEMENTOR_MODULE_PATH=${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.implementor.jar;${LIB_PATH}
RU_IMPLEMENTOR_JAR_PATH=out/artifacts/ru_ifmo_rain_yatcheniy_implementor_jar/ru.ifmo.rain.yatcheniy.implementor.jar
JAVADOC_PATH=out/javadoc

build_classes:
	javac -encoding UTF8 -d out/production/ru.ifmo.rain.yatcheniy.implementor -cp ${INFO_IMPLEMENTOR_MODULE_PATH} -p ${INFO_IMPLEMENTOR_MODULE_PATH} src/modules/ru.ifmo.rain.yatcheniy.implementor/src/ru/ifmo/rain/yatcheniy/implementor/*.java
	javac -encoding UTF8 -d out/production/ru.ifmo.rain.yatcheniy.implementor -p ${INFO_IMPLEMENTOR_MODULE_PATH} src/modules/ru.ifmo.rain.yatcheniy.implementor/module-info.java
build_jar_module: build_classes
	jar --create --file ${RU_IMPLEMENTOR_JAR_PATH} --main-class ru.ifmo.rain.yatcheniy.implementor.Implementor --module-version 1.0 -C out/production/ru.ifmo.rain.yatcheniy.implementor .
test_implementor: build_jar_module
	$(call run_test_with_module,implementor,jar-class,Implementor)
java_doc:
	javadoc -d ${JAVADOC_PATH} --source-path ${RU_IMPLEMENTOR_SOURCE_PATH} --module-path ${RU_IMPLEMENTOR_JAR_PATH};${INFO_IMPLEMENTOR_MODULE_PATH} --module ru.ifmo.rain.yatcheniy.implementor

#07
test_concurrent:
	$(call run_test_with_module,concurrent,list,IterativeParallelism)