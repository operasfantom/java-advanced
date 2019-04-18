update_submodule:
	git submodule update --remote --merge

SURNAME=yatcheniy

LIB_PATH=java-advanced-2019/lib
INFO_ARTIFACTS_PATH=java-advanced-2019/artifacts
RU_ARTIFACTS_PATH=out/artifacts
MODULES_PATH=java-advanced-2019/modules
INFO_BASE_JAR_PATH=${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar

define run_test_with_module
	java \
	-p ${RU_ARTIFACTS_PATH}/ru_ifmo_rain_yatcheniy_$(1)_jar/ru.ifmo.rain.yatcheniy.$(1).jar;${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.$(1).jar;${LIB_PATH} \
	--add-modules ru.ifmo.rain.yatcheniy.$(1) \
	-m info.kgeorgiy.java.advanced.$(1) $(2) ru.ifmo.rain.yatcheniy.$(1).$(3) $(4)
endef
#01
test_walk:
	$(call run_test_with_module,walk,RecursiveWalk,RecursiveWalk)
#02
test_array:
	$(call run_test_with_module,arrayset,NavigableSet,ArraySet)
#03
test_student:
	$(call run_test_with_module,student,AdvancedStudentGroupQuery,StudentDB,49)
#04-06
RU_IMPLEMENTOR_SOURCE_PATH=src/modules/
INFO_IMPLEMENTOR_SOURCE_PATH=${MODULES_PATH}/info.kgeorgiy.java.advanced.implementor
INFO_IMPLEMENTOR_JAR_PATH=${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.implementor.jar;${LIB_PATH}
RU_IMPLEMENTOR_JAR_PATH=${RU_ARTIFACTS_PATH}/ru_ifmo_rain_yatcheniy_implementor_jar/ru.ifmo.rain.yatcheniy.implementor.jar
JAVADOC_PATH=out/javadoc
JAVADOC_LINK=https://docs.oracle.com/en/java/javase/12/docs/api

build_classes:
	javac \
	-encoding UTF8 \
	-d out/production \
	-p ${INFO_IMPLEMENTOR_JAR_PATH} \
	--module-source-path ${RU_IMPLEMENTOR_SOURCE_PATH} \
	--module ru.ifmo.rain.yatcheniy.implementor
build_jar_module: build_classes
	jar --create \
	--file ${RU_IMPLEMENTOR_JAR_PATH} \
	--main-class ru.ifmo.rain.yatcheniy.implementor.Implementor \
	--module-version 1.0 \
	-C out/production/ru.ifmo.rain.yatcheniy.implementor .
test_jar_implementor: build_jar_module
	$(call run_test_with_module,implementor,jar-class,Implementor)
run_jar_implementor: build_jar_module
	java \
	-cp ${INFO_IMPLEMENTOR_JAR_PATH} \
	-p ${INFO_IMPLEMENTOR_JAR_PATH};${RU_IMPLEMENTOR_JAR_PATH} \
	--add-modules ru.ifmo.rain.yatcheniy.implementor \
	-m ru.ifmo.rain.yatcheniy.implementor \
	-class \
	info.kgeorgiy.java.advanced.implementor.basic.interfaces.InterfaceWithDefaultMethod \
	C:\temp\out.jar
#run_jar_implementor_2:
#	java -jar ${RU_IMPLEMENTOR_JAR_PATH} ru.ifmo.rain.yatcheniy.implementor.Implementor -class java.util.List some.jar
java_doc:
#	rmdir /s "${JAVADOC_PATH}"
	javadoc -html4 -verbose -private -d ${JAVADOC_PATH} \
	-p ${INFO_IMPLEMENTOR_JAR_PATH} --module-source-path ${RU_IMPLEMENTOR_SOURCE_PATH};${MODULES_PATH} \
	--module ru.ifmo.rain.yatcheniy.implementor -link ${JAVADOC_LINK}

#07
test_concurrent:
	$(call run_test_with_module,concurrent,list,IterativeParallelism,94)

#08
test_mapper:
#	info.kgeorgiy.java.advanced.mapper list <ParallelMapperImpl>,<IterativeParallelism>
#	$(call run_test_with_module,mapper,list,IterativeParallelism,94)
	java -p ${RU_ARTIFACTS_PATH}/ru_ifmo_rain_yatcheniy_mapper_jar/ru.ifmo.rain.yatcheniy.mapper.jar;\
	${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.mapper.jar;\
	${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.concurrent.jar;\
	${INFO_ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${LIB_PATH} \
	--add-modules ru.ifmo.rain.yatcheniy.mapper \
	-m info.kgeorgiy.java.advanced.mapper list ru.ifmo.rain.yatcheniy.mapper.ParallelMapperImpl,ru.ifmo.rain.yatcheniy.mapper.IterativeParallelism