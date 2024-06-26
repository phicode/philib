# Changelog

Changelog for philib
- F = Feature
- B = Bugfix
- G = General

## next
Maintenance release.

Update dependencies

## 0.14.0
Maintenance release.

- G: switch to java 11
- G: add java module support

## 0.13.0

- G: remove lang/TablePrinter
- G: java: the project has been switched to java 7
- G: removed deprecated class conf.ConfigValueListener (replacement: conf.ConfigListener)
- G: removed deprecated toString utility methods in lang.StringUtil (replacement: lang.ToString.*)
- F: added ClonerNoop to simplify code which supports value cloning
- F: added HashUtil methods for arrays
- F: added SingleFlight for concurrent call deduplication

## 0.12.0
- F: ToString: moved toString helpers from StringUtil to a new ToString class
- F: bench: added a simple micro-benchmarking utility
- F: conf: renamed ConfigValueListener -> ConfigListener
- F: conf: added LoggingListener
- F: conf: added an initializing constructor Config(Map<String,String>) 
- F: validation: return the verified value for shorter code in constructors.
- F: util: new TimeoutMap method: pollAllTimeoutNow

## 0.11.0
- F: added StringUtil.count(String, int):int
- F: added Stringutil.split(String, int):String[]
- F: util.Counter: improve performance under contention

## 0.10.0
- F: generic SafeCloseUtil.close(Object) using reflection
- F: added an osgi manifest
- F: added ThreadUtil.createThreads(Collection<Thread>)
- F: added ThreadUtil.interruptAndJoinThreads(Collection<Thread>)
- F: added ArrayUtil.toArray(Collection<X>)

## 0.9.2
- F: config: add getBool and getDouble

## 0.9.1
- B: config: ignore null urls

## 0.9.0
- F: minor method additions:
  - Config.{getInt, getLong}
  - ArrayUtil.prepend
  - one missing StringUtil method
-  F: ArrayUtil API change: the ctor will no longer clone the provided data

## 0.8.0
- F: configuration management in ch.bind.philib.conf
- F: additional ArrayUtil methods: append, remove

## 0.7.0
- F: NamedSeqThreadFactory can now set new thread's daemon flag and thread-group.
- F: Cache implementations are always thread-safe -> the SyncCache wrapper was removed
- F: Added a new Cache implementation: LineCache
- F: The new package "msg" contains a in-vm pub-sub system
- G: Added many unit tests

## 0.6.0
- F: rework of the counter api
- F: improved TimeoutMap
 
## 0.5.0
- G: Test-coverage increased to over 90%
- G: Many minor enhancements and additions

## 0.4.0
- G: Test-coverage increased from 64.4% to 81.4x%
- F: added util.StaticIntMap and util.IntPair
- F: Extracted StringUtil.matrixOutput(String[][]) to its own class: TableFormatter 

## 0.3.0
- F: added a simple and a staged cache implementation
- F: added util.LruList
- F: added util.ClusteredIndex
- F: added more unit tests

## 0.2.0
- initial public release
