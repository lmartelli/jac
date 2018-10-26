Class:
	Bench

How to Launch sample:
	Just enter command : 'jac bench.jac <iterations> [r|d]'
	in the bench directory.

	<iterations>: number of iterations for the test
	[r|d]: use 'r' for using reflexive calls in the test, and 'd'
	(or anything else) for using direct calls

Description:
	This sample is a benchmark to test the impact of wrappers
	on performances.
	There are 8 different tests for each iteration, testing
	different calls.

---

Class:
	Visualize

How to launch sample:
	If you want to use this class, just mail laurent@aopsys.com

---

Class:
	Translate

How to launch sample:
	Just type command :
	'java -classpath $CLASSPATH jac.samples.bench.Translate'

Description:
	This sample shows jac's performances in class loading

---

Class:
	RTTI

How to launch sample:
	Just type command :
	'java -classpath $CLASSPATH jac.samples.bench.RTTI'

Description:
	This sample shows jac's performances in RTTI calls

---