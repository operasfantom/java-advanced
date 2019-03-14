submodule_update:
	git submodule update --remote --merge
#01
test_walk:
	java -cp "out\production\walk" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.yatcheniy.walk.RecursiveWalk
#02
test_array:
	java -cp "out\production\arrayset" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.yatcheniy.arrayset.ArraySet
#03
test_student:
	java -cp "out\production\student" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.student AdvancedStudentGroupQuery ru.ifmo.rain.yatcheniy.student.StudentDB
#04-06
build_classes:
	javac -d "out\production\implementor" -cp "java-advanced-2019\artifacts\info.kgeorgiy.java.advanced.implementor.jar" src\modules\ru.ifmo.rain.yatcheniy.implementor\ru\ifmo\rain\yatcheniy\implementor\*.java
build_jar_module: build_classes
	jar --create --file "out\artifacts\ru_ifmo_rain_yatcheniy_implementor_jar\ru.ifmo.rain.yatcheniy.implementor.jar" --main-class ru.ifmo.rain.yatcheniy.implementor.Implementor --module-version 1.0 -C "out\production\ru.ifmo.rain.yatcheniy.implementor" .
test_implementor:
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor_classes: build_jar_module
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor: build_jar_module
	java -p "java-advanced-2019\lib;java-advanced-2019\artifacts\;out\artifacts\ru_ifmo_rain_yatcheniy_implementor_jar" --add-modules ru.ifmo.rain.yatcheniy.implementor -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor