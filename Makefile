JCC = javac
JVM = java
JFLAGS = -classpath jsoup-1.8.1.jar
JVMFLAGS = -classpath "jsoup-1.8.1.jar:src" -Xmx512m
.SUFFIXES: .java .class

.java.class:
	$(JCC) $(JFLAGS) $*.java

CLASSES = \
	src/edu/nyu/cs/cs2580/DocumentFull.java \
	src/edu/nyu/cs/cs2580/IndexerInvertedDoconly.java \
	src/edu/nyu/cs/cs2580/RankerFavorite.java \
	src/edu/nyu/cs/cs2580/DocumentIndexed.java \
	src/edu/nyu/cs/cs2580/IndexerInvertedOccurrence.java \
	src/edu/nyu/cs/cs2580/RankerFullScan.java \
	src/edu/nyu/cs/cs2580/Document.java \
	src/edu/nyu/cs/cs2580/Indexer.java \
	src/edu/nyu/cs/cs2580/Ranker.java \
	src/edu/nyu/cs/cs2580/Evaluator.java \
	src/edu/nyu/cs/cs2580/QueryHandler.java \
	src/edu/nyu/cs/cs2580/ReadCorpus.java \
	src/edu/nyu/cs/cs2580/Grader.java \
	src/edu/nyu/cs/cs2580/Query.java \
	src/edu/nyu/cs/cs2580/ScoredDocument.java \
	src/edu/nyu/cs/cs2580/IndexerFullScan.java \
	src/edu/nyu/cs/cs2580/QueryPhrase.java \
	src/edu/nyu/cs/cs2580/SearchEngine.java \
	src/edu/nyu/cs/cs2580/IndexerInvertedCompressed.java \
	src/edu/nyu/cs/cs2580/RankerConjunctive.java \
	src/edu/nyu/cs/cs2580/Stemmer.java \


default:
	$(JCC) $(JFLAGS) src/edu/nyu/cs/cs2580/*.java

index:
	$(JVM) $(JVMFLAGS) edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf

run:
	$(JVM) $(JVMFLAGS) edu.nyu.cs.cs2580.SearchEngine --mode=serve --port=25808 --options=conf/engine.conf 

clean:
	find . -name '*.class' -exec rm -rf {} \;
	find . -name '*~' -exec rm -rf {} \;




