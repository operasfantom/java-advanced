submodule_update:
	git submodule update --remote --merge
test_walk:
	java -cp "out\production\walk" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.yatcheniy.walk.RecursiveWalk
test_array:
	java -cp "out\production\arrayset" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.yatcheniy.arrayset.ArraySet
test_student:
	java -cp "out\production\student" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.student AdvancedStudentGroupQuery ru.ifmo.rain.yatcheniy.student.StudentDB
test_implementor:
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor:
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor